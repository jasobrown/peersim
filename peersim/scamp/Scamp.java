package scamp;

import peersim.util.*;
import peersim.core.*;
import peersim.config.*;
import peersim.cdsim.*;
import java.util.ArrayList;
import java.util.Collections;

/**
* Implement the SCAMP protocol. The failure model that is adopted assumes
* an UDP like communication where there is no feedback if a given message
* has arrived.
*/
public class Scamp implements CDProtocol, Linkable {


// =================== static fields ==================================
// ====================================================================


/** config parameter name for parameter c. Defaults to 0.  */
public static final String PAR_C = "c";

/**
* config parameter name for the TTL for indirection. If negative, there is
* no indirection. Defaults to -1.
*/
public static final String PAR_INDIRTTL = "indirectionTTL";

/**
* config parameter name for the timeout for lease. If negative, there is
* no lease mechanism. Defaults to -1.
*/
public static final String PAR_LEASE = "leaseTimeout";

private static int c;

/** indirection TTL */
private static int indirTTL;

/** lease timeout */
private static int leaseTimeout;


// =================== fields =========================================
// ====================================================================


/** contains Nodes */
private ArrayList outView = null;

/**
* Contains creation dates of elements in outView, if leasing is used.
* The class must make sure that it stays in sync with outView. 
* If lease is not used, it is simply null.
*/
private ArrayList outViewDates = null;

/** contains Nodes */
private ArrayList inView = null;

/**
* Contains creation dates of elements in inView, if leasing is used.
* The class must make sure that it stays in sync with inView. 
* If lease is not used, it is simply null.
*/
private ArrayList inViewDates = null;

/**
* to support the lease mechanism. with randomised resubmission it would
* not be necessary.
*/
private int birthDate;


// ===================== initialization ================================
// =====================================================================


public Scamp(String n) {

	Scamp.c = Configuration.getInt(n+"."+PAR_C,0);
	Scamp.indirTTL = Configuration.getInt(n+"."+PAR_INDIRTTL,-1);
	Scamp.leaseTimeout = Configuration.getInt(n+"."+PAR_LEASE,-1);
	outView = new ArrayList();
	inView = new ArrayList();
	if(Scamp.leaseTimeout>0)
	{
		outViewDates = new ArrayList();
		inViewDates = new ArrayList();
	}
	birthDate = CommonState.getT();
}

// ---------------------------------------------------------------------

/**
* The birthDate field will denote the birth of the clone, and not the
* original birthDate.
*/
public Object clone() throws CloneNotSupportedException {

	Scamp scamp = (Scamp) super.clone();
	scamp.outView = (ArrayList)outView.clone();
	scamp.inView = (ArrayList)inView.clone();
	if( outViewDates != null )
		scamp.outViewDates = (ArrayList)outViewDates.clone();
	if( inViewDates != null )
		scamp.inViewDates = (ArrayList)inViewDates.clone();
	scamp.birthDate = CommonState.getT();
	return scamp;
}


// ====================== The core Scamp protocols =======================
// =======================================================================


/**
* Performs the forwarding cycle. Applies a little simplification compared
* to the original SCAMP protocol. To avoid infinite cycles, instead of
* counters in each node, we allow only a limited number of steps, ie
* we introduce a TTL instead. It consumes less memory and easier to implement.
* @param n the node which receives the given subscription 
* @param s the subscribing node
*/
private static void doSubscribe( Node n, Node s, int protocolID ) {
	
	boolean added = false;
	int i=0;

	for(; n.isUp() && !added && i<2*OverlayNetwork.size(); ++i)
	{
		Scamp scamp = (Scamp)n.getProtocol(protocolID);
		
		if( CommonRandom.r.nextDouble() < 1.0/(1.0+scamp.degree()) &&
		    !scamp.contains(s) )
		{
			scamp.addNeighbor(s);
			((Scamp)s.getProtocol(protocolID)).addInNeighbor(n);
			added = true;
		}
		else
		{
			if( scamp.degree() > 0 )
				n=scamp.getNeighbor(
				   CommonRandom.r.nextInt(scamp.degree()));
			else break;
		}
	}
	
	if( !added )
		System.err.println("SCAMP: subscription not succesful! ("+
			OverlayNetwork.size()+","+i+","+n.isUp()+")");
}

// ----------------------------------------------------------------------

/**
* Performs the indirection (a random walk) to get a random element from the
* network. If the random walk gets stuck because of a node which is down,
* the node which is down is returned. This models the fact that in the real
* protocol in fact nothing is returned.
*/
private static Node getRandomNode( Node n, int protocolID ) {

	double ttl=indirTTL;
	Scamp l = (Scamp)n.getProtocol(protocolID);
	ttl -= 1.0/l.degree();
	
	while( n.isUp() && ttl > 0.0 )
	{
		if( l.degree() + l.inView.size() > 0 )
		{
			int id = CommonRandom.r.nextInt(
					l.degree()+l.inView.size() );
			if( id<l.degree() ) n = l.getNeighbor(id);
			else n = (Node)l.inView.get(id-l.degree());
		}
		else break;

		l = (Scamp)n.getProtocol(protocolID);
		ttl -= 1.0/l.degree();
	}

	if( ttl > 0.0 ) System.err.println(
		"Scamp: getRandomNode returned with ttl="+ttl);
	return n;
}

// ----------------------------------------------------------------------

/**
* This node will act as a contact node forwarding the subscription to nodes
* from its view and c other random nodes.
* @param n the contact node
* @param s the subscribing node
*/
public static void subscribe( Node n, Node s, int protocolID ) {

	if( indirTTL > 0.0 ) n = getRandomNode(n, protocolID);
	
	if( !n.isUp() ) return; // quietly returning, no feedback
	
	Scamp contact = (Scamp)n.getProtocol(protocolID);
	((Linkable)s.getProtocol(protocolID)).addNeighbor(n);
	
	//I guess this is needed
	contact.addInNeighbor(s);

	if( contact.degree() == 0 )
	{
		System.err.println("SCAMP: zero degree contact node!");
		// I guess this is a good idea
		Scamp.doSubscribe( n, s, protocolID );
		return;
	}
	
	for(int i=0; i<contact.outView.size(); ++i)
	{
		Scamp.doSubscribe( (Node)contact.outView.get(i),s,protocolID );
	}
	
	for(int i=0; i<Scamp.c; ++i)
	{
		Scamp.doSubscribe( (Node)contact.outView.get(
			CommonRandom.r.nextInt(contact.degree())),
		   s, protocolID);
	}
}

// ----------------------------------------------------------------------

/**
* Replace n1 with n2 in the partial view. Helper method to unsubscribe.
* This is done taking care of
* the consistency of the data structure and dates. If n1 is not known,
* prints a warning and exits doing nothing.
* @param n1 the node to replace
* @param n2 the new node. If null, n1 is simply removed.
* @param date the insertion date of n2, or null if lease is not in effect
*/
// XXX could be optimized if turned out to be a performance problem
private void replace(Node n1, Node n2, Integer date) {

	int id = outView.indexOf(n1);
	
	if( id < 0 )
	{
		System.err.println("Scamp.replace: node is not known");
		return;
	}

	// removing n1
	outView.remove(id);
	if( outViewDates != null ) outViewDates.remove(id);

	// inserting n2
	if( n2 != null )
	{
		if( (date != null && outViewDates == null) ||
		    (date == null && outViewDates != null) )
			throw new IllegalStateException(
				"is lease active or not?");
		
		int ins = outView.size();
		if( outViewDates != null )
		{
			ins = Collections.binarySearch( outViewDates, date );
			if( ins < 0 ) ins = -(ins+1);
			outViewDates.add(ins,date);
		}
		outView.add(ins,n2);
	}
}


// ----------------------------------------------------------------------

/**
* Run the unsubscribe protocol for the given node.
* @param n not to unsubscribe
*/
public static void unsubscribe( Node n, int protocolID ) {

	Scamp sn = (Scamp)n.getProtocol(protocolID);
	final int l = sn.degree();
	final int ll = sn.inView.size();
	int i=0;
	
	// replace ll-(c+1) links to sn
	if( l > 0 )
	for(; i<ll-c-1; ++i)
	{
		Node from = (Node)sn.inView.get(i);
		if( from.isUp() ) 
			((Scamp)from.getProtocol(protocolID)).replace(
				n,
				sn.getNeighbor(i%l),
				(sn.outViewDates == null ? null 
					: (Integer)sn.outViewDates.get(i%l)));
	}
	
	// remove the remaining c+1 links to sn
	for(; i<ll; ++i)
	{
		Node from = (Node)sn.inView.get(i);
		if( from.isUp() ) 
			((Scamp)from.getProtocol(protocolID)).replace(
				n, null, null );
	}
}

// ----------------------------------------------------------------------

/**
* Convenience function for adding an element to the inView, taking care of
* the dates too.
*/
private boolean addInNeighbor(Node node) {
	
	if( !inView.contains(node) )
	{
		inView.add( node );
		if( inViewDates != null )
			inViewDates.add(CommonState.getTimeObj());
		return true;
	}
	else
	{
		if( inViewDates != null )
		{
			// we have to change the date of node
			int id = inView.indexOf(node);
			inView.remove(id);
			inViewDates.remove(id);
			inViewDates.add(CommonState.getTimeObj());
			inView.add( node );
		}
		return false; // false's ok though dates might've been changed
	}
}

// ====================== Linkable implementation =====================
// ====================================================================


public Node getNeighbor(int i) {

	return (Node)outView.get(i);
}

// --------------------------------------------------------------------

public int degree() {
	
	return outView.size();
}

// --------------------------------------------------------------------

public boolean addNeighbor(Node node) {
	
	if( !contains(node) )
	{
		outView.add( node );
		if( outViewDates != null )
			outViewDates.add(CommonState.getTimeObj());
		return true;
	}
	else
	{
		if( outViewDates != null )
		{
			// we have to change the date of node
			int id = outView.indexOf(node);
			outView.remove(id);
			outViewDates.remove(id);
			outViewDates.add(CommonState.getTimeObj());
			outView.add( node );
		}
		return false; // false's ok though dates might've been changed
	}
}

// --------------------------------------------------------------------

public void pack() {}

// --------------------------------------------------------------------

public boolean contains(Node node) {
	
	return outView.contains(node);
}


// ===================== CDProtocol implementations ===================
// ====================================================================

public void nextCycle( Node thisNode, int protocolID ){
	
	// heartbeat
	// for now not implemented, does not seem to be important
	
	// lease (re-subscription)
	if( outViewDates != null )
	{
		if((CommonState.getT()-birthDate)%Scamp.leaseTimeout == 0 &&
			degree() > 0 && CommonState.getT() > birthDate )
		{	
			Scamp.subscribe(
				getNeighbor(CommonRandom.r.nextInt(degree())),
				thisNode,
				protocolID );
		}
		
		// remove expired items from _our own_ views.
		// entries are always ordered in increasing time. We remove
		// the first i elements which are expired from both views
		int i=0;
		while( i<degree() && CommonState.getT() - 
				((Integer)outViewDates.get(i)).intValue() >= 
			Scamp.leaseTimeout ) ++i;
		if( i > 0 )
		{
			outView.subList(0,i).clear();
			outViewDates.subList(0,i).clear();
		}
		i = 0;
		while( i<inView.size() && CommonState.getT() - 
				((Integer)inViewDates.get(i)).intValue() >= 
			Scamp.leaseTimeout ) ++i;
		if( i > 0 )
		{
			inView.subList(0,i).clear();
			inViewDates.subList(0,i).clear();
		}
	}
/*
	// XXX this implementation does not use dates at all, so we don't
	// update them properly for inViews. If this implementation becomes
	// the winner, it has to be fixed.
	if( outViewDates != null )
	{
		if((CommonState.getT()-birthDate)%Scamp.leaseTimeout == 0 &&
			degree() > 0 && CommonState.getT() > birthDate )
		{	
			Scamp.subscribe(
				getNeighbor(CommonRandom.r.nextInt(degree())),
				thisNode,
				protocolID );
		
			// remove our items from _others_ views
			for( int i=0; i<degree(); ++i )
			{
				Scamp outsc = (Scamp)
				  getNeighbor(i).getProtocol(protocolID);
				int id = outsc.inView.indexOf(thisNode);
				if( id >= 0 ) // should always be
				{
					outsc.inView.remove(id);
				}
			}
			for( int i=0; i<inView.size(); ++i )
			{
				Scamp insc = (Scamp)
				  ((Node)inView.get(i)).getProtocol(protocolID);
				insc.replace(thisNode,null,null);
			}
		}
	}*/
}

// ===================== Other ========================================
// ====================================================================

/** Method to check the consistency of the state of the network */
public static String test(int protocolID) {
	
	int failOutLinks=0; // out link points to failed node
	int failInLinks=0; // in link points to failed node
	int corruptDates=-1; // outViewDate is wrong size or not ordered
	int corruptInDates=-1; // inViewDate is wrong size or not ordered
	int missingInLinks=0; // no corresponding in view link
	int missingOutLinks=0; // no corresponding out view link
	
	if( Scamp.leaseTimeout >= 0 ) corruptInDates=corruptDates = 0 ;
	
	for(int i=0; i<OverlayNetwork.size(); ++i)
	{
		Node curr = OverlayNetwork.get(i);
		Scamp currsc = (Scamp)(curr.getProtocol(protocolID));
		
		// check out view
		for(int j=0; j<currsc.degree(); ++j)
		{
			Node out = (Node)currsc.outView.get(j);
			if(!out.isUp())
			{
				++failOutLinks;
				++missingInLinks;
			}
			else
			{
				Scamp outsc=(Scamp)out.getProtocol(protocolID);
				if(!outsc.inView.contains(curr))
					++missingInLinks;
			}
		}
		
		// check in view
		for(int j=0; j<currsc.inView.size(); ++j)
		{
			Node in = (Node)currsc.inView.get(j);
			if(!in.isUp())
			{
				++failInLinks;
				++missingOutLinks;
			}
			else
			{
				Scamp insc=(Scamp)in.getProtocol(protocolID);
				if(!insc.outView.contains(curr))
					++missingOutLinks;
			}
		}

		// check dates if any
		if( currsc.outViewDates == null ) continue;
		if( currsc.outViewDates.size() != currsc.degree() )
			corruptDates++;
		else if ( currsc.outViewDates.size() > 0 )
		{
			for(int j=1; j<currsc.degree(); ++j)
			{
				if( ((Integer)currsc.outViewDates.get(j-1)
				  ).compareTo(
				  currsc.outViewDates.get(j)) > 0 )
				{
					corruptDates++;
					break;
				}
			}

		}
		if( currsc.inViewDates.size() != currsc.inView.size() )
			corruptInDates++;
		else if ( currsc.inViewDates.size() > 0 )
		{
			for(int j=1; j<currsc.inView.size(); ++j)
			{
				if( ((Integer)currsc.inViewDates.get(j-1)
				  ).compareTo(
				  currsc.inViewDates.get(j)) > 0 )
				{
					corruptInDates++;
					break;
				}
			}

		}
	}
	
	return ("failOutLinks="+failOutLinks+
		" failInLinks="+failInLinks+
		" missingOutLinks="+missingOutLinks+
		" missingInLinks="+missingInLinks+
		" corruptDates="+corruptDates+
		" corruptInDates="+corruptInDates);
}

}


