package lpbcast;

import peersim.util.*;
import peersim.core.*;
import peersim.config.*;
import peersim.cdsim.*;
import java.util.*;

/**
*  This class represents the information stored by a node in
*  the simplified lpbcast system (i.e., used just as a topology
*  manager)
*/
public class SimpleLpbcast implements CDProtocol, Linkable {


// =============== static fields =======================================
// =====================================================================


/**
* config parameter name for the max view size. If less than 1 then there
* is no view, only subs. Defaults to -1. 
*/
public static final String PAR_L = "l";

/** config parameter name for the max subs size */
public static final String PAR_SUBS = "subs";

/** config parameter name for the max unsubs size */
public static final String PAR_UNSUBS = "unSubs";

/**
* config parameter name for the timeout of an unsubscription.
* If negative, there is no timeout. Defaults to -1.
*/
public static final String PAR_UNSUBSTOUT = "unSubsTout";

/** config parameter name for F. Defaults to 1. */
public static final String PAR_F = "F";

private static int l;

private static int subsSize;

private static int unSubsSize;

private static int unSubsTout;

private static int F;

/**
* to avoid memory allocation, we have this object which can be reset
* when necessary
*/
private final static RandomAccessibleIterator rai =
	new RandomAccessibleIterator(new ArrayList());

// =================== fields ==========================================
// =====================================================================


/**
* If null, only subs is used. If not null, then subs will serve only
* as background storage, and the communication graph will be defined by view
*/
protected ArrayList view = null;

protected ArrayList subs = null;

protected ArrayList unSubs = null;

/** timestamps of unsubscriptions. If no timestamps are used, null */
protected ArrayList unSubsDates = null;


// ====================== initialization ===============================
// =====================================================================


public SimpleLpbcast(String n) {
	
	SimpleLpbcast.l = Configuration.getInt(n+"."+PAR_L,-1);
	SimpleLpbcast.subsSize = Configuration.getInt(n+"."+PAR_SUBS);
	SimpleLpbcast.unSubsSize = Configuration.getInt(n+"."+PAR_UNSUBS);
	SimpleLpbcast.unSubsTout=Configuration.getInt(n+"."+PAR_UNSUBSTOUT,-1);
	SimpleLpbcast.F = Configuration.getInt(n+"."+PAR_F, 1);
	subs = new ArrayList();
	unSubs = new ArrayList();
	if( SimpleLpbcast.unSubsTout > 0 ) unSubsDates = new ArrayList();
	if( SimpleLpbcast.l > 0 ) view = new ArrayList();
}

// ---------------------------------------------------------------------

public Object clone() throws CloneNotSupportedException {

	SimpleLpbcast sn = (SimpleLpbcast)super.clone();
	sn.subs = (ArrayList)subs.clone();
	sn.unSubs = (ArrayList)unSubs.clone();
	if( SimpleLpbcast.unSubsTout > 0 )
		sn.unSubsDates = (ArrayList)unSubsDates.clone();
	if( SimpleLpbcast.l > 0 ) sn.view = (ArrayList)view.clone();
	return sn;
}


// ====================== helper and protocol methods =================
// ====================================================================


protected void send( Node thisNode, Node peerNode, int protocolID ) {

	if( thisNode == peerNode ) System.err.println(
		"lpbcast.send: something is not ok, loop edge detected");
	
	SimpleLpbcast peer = (SimpleLpbcast)peerNode.getProtocol(protocolID);

	// handling unsubscriptions
	for(int i=0; i<unSubs.size(); ++i)
	{
		Node unsub = (Node)unSubs.get(i);
		peer.remove(unsub);
		Integer tstamp =
		   (unSubsDates==null ? null : (Integer)unSubsDates.get(i));
		peer.addUnSub(unsub,tstamp);
	}

	// updating views
	peer.addNeighbor(thisNode);
	for(int i=0; i<subs.size(); ++i)
	{
		Node sub = (Node)subs.get(i);
		if( sub != peerNode ) peer.addNeighbor(sub);
	}
}

// --------------------------------------------------------------------

/**
* The node is added to the list of unsibscriptions and if the list becomes
* larger than the maximal allowed size, a random element is removed.
* @param node the node to add to unSubs
* @param tstamp the timestamp of the unsubscrition in question. Null if
* no timestamp is provided..
*/
protected boolean addUnSub(Node node, Integer tstamp) {

	if( unSubs.contains(node) ) return false; // XXX timestamp handling
	
	if( unSubs.size() >= SimpleLpbcast.unSubsSize )
	{
		final int pos = CommonRandom.r.nextInt(unSubs.size()+1);
		if( pos < unSubs.size() )
		{
			unSubs.set(pos,node);
			if( unSubsDates != null ) unSubsDates.set(pos,tstamp);
			return true;
		}
		else return false;
	}
	else
	{
		if( unSubsDates != null ) unSubsDates.add(tstamp);
		return unSubs.add(node);
	}
}

// --------------------------------------------------------------------

/** removes the given node from the views (view and subs) */
protected void remove(Node unsub) {

	// removing from view
	int ind = -1;
	if( view != null ) ind = view.indexOf(unsub);
	if( ind >= 0 )
	{
		// removing unsub avoiding arraycopy
		view.set( ind, view.get(view.size()-1) );
		view.remove( view.size()-1 );
	}
	
	// removing from subs
	ind = subs.indexOf(unsub);
	if( ind >= 0 )
	{
		// removing unsub avoiding arraycopy
		subs.set( ind, subs.get(subs.size()-1) );
		subs.remove( subs.size()-1 );
	}
}

// ----------------------------------------------------------------------

/**
* Run the unsubscribe protocol for the given node.
* @param n not to unsubscribe
*/
public static void unsubscribe( Node n, int protocolID ) {

	SimpleLpbcast lpb = (SimpleLpbcast)n.getProtocol(protocolID);
	rai.reset( lpb.view==null ? lpb.subs : lpb.view );
	int i=0;
	for(; i<F && rai.hasNext(); ++i)
	{
		Node peerNode = (Node)SimpleLpbcast.rai.next();
		SimpleLpbcast peer = (SimpleLpbcast)
		   	peerNode.getProtocol(protocolID);
		
		// this is necessary to minimize the probability that other
		// unsubscriptions get lost, eg those that have
		// just unsubscribed too in the same round to this node
		lpb.send( n, peerNode, protocolID );
		
		// this implements the effect of adding n to unSubs
		peer.remove(n);
		peer.addUnSub(n,CommonState.getTimeObj());
	}
	
	if( i < F )
	{
		System.err.println("Lpbcast.unsubscribe: only "+
			i+" accessible peers");
	}
}

// ====================== Linkable implementation =====================
// ====================================================================
 

/**
*/
public Node getNeighbor(int i) {
	
	return ( view == null ? (Node)subs.get(i) : (Node)view.get(i) );
}

// --------------------------------------------------------------------

/** Might be less than cache size. */
public int degree() {
	
	return ( view == null ? subs.size() : view.size() );
}

// --------------------------------------------------------------------

/**
* The node is added to the list and if the list becomes larger than
* the maximal allowed size, an element is removed. If view is maintained,
* then the return value will indicate if the node was added to the view.
* Otherwise it will indicate if it was added to subs.
*/
public boolean addNeighbor(Node node) {
	
	if( view != null && view.contains(node) ) return false;
	
	boolean ret;
	
	// XXX this "contains" slows the simulation down significantly
	// to be fixed somehow, also at other places. Maybe sets should be used
	if( subs.contains(node) ) ret = false;
	else if( subs.size() >= SimpleLpbcast.subsSize )
	{
		final int pos = CommonRandom.r.nextInt(subs.size()+1);
		if( pos < subs.size() )
		{
			subs.set(pos,node);
			ret = true;
		}
		else ret = false;
	}
	else ret = subs.add(node);
		
	if( view != null ) // here we know that !view.contains(node)
	{
		if( view.size() >= SimpleLpbcast.l )
		{
			final int pos = CommonRandom.r.nextInt(view.size()+1);
			if( pos < view.size() )
			{
				view.set(pos,node);
				ret = true;
			}
			else ret = false;
		}
		else ret = view.add(node);
	}

	return ret;
}

// --------------------------------------------------------------------

public void pack() {}

// --------------------------------------------------------------------

public boolean contains(Node n) {
	
	return ( view == null ? subs.contains(n) : view.contains(n) );
}


// ===================== CDProtocol implementations ===================
// ====================================================================


public void nextCycle( Node thisNode, int protocolID ) {

	// remove expired unsubscriptions
	if( unSubsDates != null )
	{
		int i=0;
		while( i<unSubsDates.size() )
		{
			int tstamp = ((Integer)unSubsDates.get(i)).intValue();
			if(CommonState.getT()-tstamp>SimpleLpbcast.unSubsTout)
			{
				unSubs.remove(i);
				unSubsDates.remove(i);
			}
			else ++i;
		}
	}
	
	// send gossip to F nodes
	SimpleLpbcast.rai.reset( view==null ? subs : view );
	int i=0;
	for(; i<SimpleLpbcast.F && SimpleLpbcast.rai.hasNext(); ++i)
	{
		send( thisNode, (Node)SimpleLpbcast.rai.next(), protocolID );
	}
	
	if( i < SimpleLpbcast.F )
	{
		System.err.println("Lpbcast: only "+i+" accessible peers");
	}
}


// ===================== other public methods =========================
// ====================================================================


public String toString() {

	return ""+subs+" "+view;
}

}



// =====================================================================
// =====================================================================
// =====================================================================



/**
* Iterates over accessible nodes in a random order. Look out! It reorders
* the list so the list order will be changed as a side effect.
*/
class RandomAccessibleIterator implements Iterator {


// ===================== fields =======================================
// ====================================================================


/** points to the next possible element to return */
private int i = 0;

private List l = null;

/** if not null, next returns this */
private Object _next = null;

// ===================== initialization ===============================
// ====================================================================


/**
* The list has to contain nodes.
*/
public RandomAccessibleIterator(List l) {
	
	reset(l);
}

// --------------------------------------------------------------------

public void reset(List l) {
	
	i = 0;
	this.l = l;
	_next = null;
}


// ======================== methods ====================================
// =====================================================================


public boolean hasNext() {

	if( _next!=null ) return true;
	
	try
	{
		_next = next();
	}
	catch( NoSuchElementException e ) {}
	
	return _next!=null;
}

// ---------------------------------------------------------------------

public Object next() {
	
	if( _next != null )
	{
		Object tmp = _next;
		_next = null;
		return tmp;
	}
	
	int pos = -1;
	Node peer = null;
	while( i < l.size() )
	{
		pos = CommonRandom.r.nextInt(l.size()-i);
		peer = (Node)l.get(i+pos);
		l.set(i+pos,l.get(i));
		l.set(i,peer);
		++i;
		if( peer.isUp() ) break;
	}
	
	if( peer==null || !peer.isUp() ) throw new NoSuchElementException();
	else return peer;
}

// ---------------------------------------------------------------------

public void remove() { throw new UnsupportedOperationException(); }

}



