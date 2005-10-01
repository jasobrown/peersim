/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
		
package lpbcast;

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
 * The max view size. If less than 1 then there is no view, only subs. Defaults
 * to -1.
 * @config
 */
private static final String PAR_L = "l";

/**
 * The max subs size
 * @config
 */
private static final String PAR_SUBS = "subs";

/**
 * The max unsubs size
 * @config
 */
private static final String PAR_UNSUBS = "unSubs";

/**
 * This parameter defines the timeout of an unsubscription. If negative, there
 * is no timeout. Defaults to -1.
 * @config
 */
private static final String PAR_UNSUBSTOUT = "unSubsTout";

/**
 * config parameter name for F. Defaults to 1.
 * @config
 */
private static final String PAR_F = "F";

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
	new RandomAccessibleIterator(new ArrayList<Node>());

// =================== fields ==========================================
// =====================================================================


/**
* If null, only subs is used. If not null, then subs will serve only
* as background storage, and the communication graph will be defined by view
*/
protected ArrayList<Node> view = null;

protected ArrayList<Node> subs = null;

protected ArrayList<Node> unSubs = null;

/** timestamps of unsubscriptions. If no timestamps are used, null */
protected ArrayList<Integer> unSubsDates = null;


// ====================== initialization ===============================
// =====================================================================


public SimpleLpbcast(String n) {
	
	SimpleLpbcast.l = Configuration.getInt(n+"."+PAR_L,-1);
	SimpleLpbcast.subsSize = Configuration.getInt(n+"."+PAR_SUBS);
	SimpleLpbcast.unSubsSize = Configuration.getInt(n+"."+PAR_UNSUBS);
	SimpleLpbcast.unSubsTout=Configuration.getInt(n+"."+PAR_UNSUBSTOUT,-1);
	SimpleLpbcast.F = Configuration.getInt(n+"."+PAR_F, 1);
	subs = new ArrayList<Node>();
	unSubs = new ArrayList<Node>();
	if( SimpleLpbcast.unSubsTout > 0 ) unSubsDates = new ArrayList<Integer>();
	if( SimpleLpbcast.l > 0 ) view = new ArrayList<Node>();
}

// ---------------------------------------------------------------------

/**
 * 
 */
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
		Node unsub = unSubs.get(i);
		peer.remove(unsub);
		Integer tstamp =
		   (unSubsDates==null ? null : (Integer)unSubsDates.get(i));
		peer.addUnSub(unsub,tstamp);
	}

	// updating views
	peer.addNeighbor(thisNode);
	for(int i=0; i<subs.size(); ++i)
	{
		Node sub = subs.get(i);
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
		final int pos = CDState.r.nextInt(unSubs.size()+1);
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
		Node peerNode = SimpleLpbcast.rai.next();
		SimpleLpbcast peer = (SimpleLpbcast)
		   	peerNode.getProtocol(protocolID);
		
		// this is necessary to minimize the probability that other
		// unsubscriptions get lost, eg those that have
		// just unsubscribed too in the same round to this node
		lpb.send( n, peerNode, protocolID );
		
		// this implements the effect of adding n to unSubs
		peer.remove(n);
		peer.addUnSub(n,CDState.getCycleObj());
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
		final int pos = CDState.r.nextInt(subs.size()+1);
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
			final int pos = CDState.r.nextInt(view.size()+1);
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

// --------------------------------------------------------------------

public void onKill() {

	view = null;
	subs = null;
	unSubs = null;
	unSubsDates = null;
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
			int tstamp = unSubsDates.get(i).intValue();
			if(CDState.getCycle()-tstamp>SimpleLpbcast.unSubsTout)
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
		send( thisNode, SimpleLpbcast.rai.next(), protocolID );
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

private List<Node> l = null;

/** if not null, next returns this */
private Node _next = null;

// ===================== initialization ===============================
// ====================================================================


/**
* The list has to contain nodes.
*/
public RandomAccessibleIterator(List<Node> l) {
	
	reset(l);
}

// --------------------------------------------------------------------

public void reset(List<Node> l) {
	
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

public Node next() {
	
	if( _next != null )
	{
		Node tmp = _next;
		_next = null;
		return tmp;
	}
	
	int pos = -1;
	Node peer = null;
	while( i < l.size() )
	{
		pos = CDState.r.nextInt(l.size()-i);
		peer = l.get(i+pos);
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



