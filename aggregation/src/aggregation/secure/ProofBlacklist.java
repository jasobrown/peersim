/*
 * Copyright (c) 2003 The BISON Project
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

package aggregation.secure;

import peersim.cdsim.*;
import peersim.core.*;
import peersim.util.*;
import peersim.config.*;
import java.util.*;

/**
 * This class implements the blacklist interface assuming the existance of
 * misbehavior proofs (i.e., by assuming that this 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ProofBlacklist 
implements CDProtocol, Blacklist
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/**
 * String name of the parameter used to select the linkable protocol 
 * used to obtain information about neighbors.
 */
public static final String PAR_CONN = "linkableID";

/**
 * String name of the parameter that determines the maximum life of
 * a blacklist notification in the system.
 */
public static final String PAR_MAXTTL = "ttl";

/**
 * String name of the parameter that determines the maximum size of
 * the blacklist data structure.
 */
public static final String PAR_SIZE = "size";

/**
 * String name of the parameter that determines the maximum number
 * of blacklist notification that can be forwarded per cycle.
 */
public static final String PAR_FORWARDED = "forwarded";


//--------------------------------------------------------------------------
// Static fields
//--------------------------------------------------------------------------

/** Maximum ttl*/
private static byte maxttl;

/** Maximum fordarde*/
private static int maxforwarded;

/** Size */
private static int size;  

/** Integer objects created only once */
private static Integer[] ttls;

/** Message to forward */
private static Node[] forward;

/** Singleton blacklist iterator */
private static BlacklistIterator iterator;

//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Blacklist */
private Map map;

/** Set corresponding to the blacklist */
private Set set;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * Construct a new blacklist instance by reading configuration parameters
 * and creating appropriate data structures.
 */
public ProofBlacklist(String prefix, Object obj)
{
	/* Store info about the linkable value */
	int pid = ((Integer) obj).intValue();
	int link = Configuration.getInt(prefix+"."+PAR_CONN);
	Protocols.setLink(pid, link);

	/* Read parameters */
  maxttl = (byte) Configuration.getInt(prefix+"."+PAR_MAXTTL);
  ttls = new Integer[maxttl];
  for (int i=0; i < maxttl; i++) {
  	ttls[i] = new Integer(i);
  }
  size = Configuration.getInt(prefix+"."+PAR_SIZE);
	maxforwarded = Configuration.getInt(prefix+"."+PAR_FORWARDED, size);
	forward = new Node[size]; // XXX

	/* Generates a number */
	map = new HashMap(size*2, 0.75f);
	set = map.keySet();

	if (iterator == null)
		iterator = new BlacklistIterator();  
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public Object clone() throws CloneNotSupportedException 
{
  ProofBlacklist bl = (ProofBlacklist) super.clone();
	bl.map = new HashMap(size*2, 0.75f);
	bl.set = bl.map.keySet();
	return bl;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void add(int blid, Node src, Node node1, Node node2)
{
	if (isMalicious(node1)) {
		add(node1);
	} else if (isMalicious(node2)) {
		add(node2);
	} else 
	  System.err.print("+");
}

//--------------------------------------------------------------------------

/** Returns true if the node contains a malicious protocol */
private boolean isMalicious(Node node)
{
	for (int i=0; i < node.protocolSize(); i++) {
		if (node.getProtocol(i) instanceof MaliciousProtocol) {
			// System.out.println(node.getProtocol(i).getClass());
			return true;
		}
	}
	return false;
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void nextCycle(Node node, int protocolID)
{
	int linkableID = Protocols.getLink(protocolID);
	Linkable linkable = (Linkable) node.getProtocol( linkableID );
	if (linkable.degree() == 0)
	  return;

	int blid = CommonState.getPid();

	int count = 0;
	set.toArray(forward);
	for (int i=0; i < set.size(); i++) {
		Integer t = (Integer) map.get(forward[i]);
		if (t.intValue() > 0) {
			forward[count] = forward[i];
			count++;		
		}
	}
	int toremove = count - maxforwarded;
	for (int i=0; i < toremove; i++) {
		int k = CommonRandom.r.nextInt(count);
		forward[k] = forward[count-1];
		count--;
	}

	for (int i=0; i < count; i++) {
		Integer t = (Integer) map.get(forward[i]);
		map.put(forward[i], ttls[t.intValue()-1]);
		int rindex = CommonRandom.r.nextInt(linkable.degree());
		Node rnode = linkable.getNeighbor(rindex);
		ProofBlacklist rblacklist = 
			(ProofBlacklist) rnode.getProtocol(blid);
		rblacklist.add(forward[i]);		
	}
}

//--------------------------------------------------------------------------

/**
 * Add a single node to the blacklist
 * @param node the node to be added
 */
private void add(Node node)
{
	Integer t = (Integer) map.get(node);
	if (t == null) {
		map.put(node, ttls[ttls.length-1]);
	} 
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public boolean contains(Node node)
{
	return map.get(node) != null;
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public Iterator iterator()
{
	set.toArray(forward);
	iterator.reset(forward, set.size());
	return iterator;
}

//--------------------------------------------------------------------------

/**
 * Optimized iterator, created as a singleton object, to be used to
 * iterate over the blacklist.
 * 
 *
 */
class BlacklistIterator implements Iterator {

	int index;
	int size;
	Node[] list;

  public void reset(Node[] list, int size)
  {
  	this.list = list;
  	this.size = size;
		index = 0;
  }

	// Comment inherited from interface
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	// Comment inherited from interface
	public boolean hasNext()
	{
		return index < size;
	}

	// Comment inherited from interface
	public Object next()
	{
		return forward[index++];
  }
}

//--------------------------------------------------------------------------


}
