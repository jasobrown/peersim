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

import peersim.core.*;
import peersim.util.*;
import peersim.config.*;
import peersim.reports.Observer;
import java.util.*;

/**
 * This is a simple implementation of a blacklist, based on a centralized
 * server. The implementation does not make use of proofs. When a node is
 * added for the first time, a new entry is created for it. Whenever it is
 * added again, a counter is increased in this entry, to keep track of
 * the number of times that that node has been suspected. Based on this
 * value, a node is declared blacklisted only if its counter exceed the
 * average counter for a certain factor. 
 *  
 * <p>
 * This implementation has served only as a first implementation of
 * the blacklist interface. It is left here for historical purposes.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class CentralizedBlacklist
implements Observer, Blacklist, Protocol
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to select the protocol that is
 * controlled by this blacklist.
 */
public static final String PAR_PROTID = "protocol";

/**
 * String name of the parameter that determines the threshold factor
 * beyond which a node is declared blacklisted. Defaults to 2.
 */
private static final String PAR_THRESHOLD = "threshold";


//--------------------------------------------------------------------------
// Static section
//--------------------------------------------------------------------------

/** The identifier of the controlled protocol */
private static int pid;

/** Hashmap representing the blacklist */
private static Map map = new HashMap();

/** Current average */
private static double average = 0;

/** Threshold factor for blacklisting */
private static double threshold;


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** The name of this object in the configuration file */
private final String name;

//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 *  Constructs a new centralized black list by reading configuration
 *  parameters (protocol identifier and threshold).
 */
public CentralizedBlacklist(String name)
{
	this.name = name;
	threshold = Configuration.getDouble(name + "." + PAR_THRESHOLD, 2);
	pid = Configuration.getPid(name+"."+PAR_PROTID);
}

//--------------------------------------------------------------------------

/**
 * In this implementation, there is no actual cloning. We use a little
 * trick, by returning the reference to the original object. In this
 * way, there is only a single object in the system.
 */
public Object clone() 
{
	return this;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------


/**
 * Adds both <code>node1</code> and <code>node2</code> to the blacklist. 
 */
public void add(int blid, Node src, Node node1, Node node2)
{
	add(node1);
	add(node2);
}

//--------------------------------------------------------------------------

/**
 * Adds a single node to the blacklist 
 */
private	void add(Node node)
{
	double sum = average*map.size();
	Entry entry = (Entry) map.get(node);
	if (entry == null) {
		entry = new Entry();
		map.put(node, entry);
	}
	entry.count++;
	sum = sum+1;
	average = sum/map.size();
}

//--------------------------------------------------------------------------

/**
 * Returns true only if the specified node has been suspected for a number
 * of times that is threshold times larger than the average.
 */
public boolean contains(Node node)
{
	Entry entry = (Entry) map.get(node);
	return (entry != null && entry.count > average*threshold);
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public Iterator iterator()
{
	return map.keySet().iterator();
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean analyze()
{
	int correct = 0;
	int falsePositives = 0;

	Iterator it = map.keySet().iterator();
	while (it.hasNext()) {
		Node node = (Node) it.next();
		Entry entry = (Entry) map.get(node);
		//System.out.print("("+ node.getIndex()+","+entry.count+") ");
		if (entry.count > threshold*average) {
			if (node.getProtocol(pid) instanceof MaliciousProtocol)
			  correct++;
			else 
			  falsePositives++;
			}
	}
	// System.out.println("");
	Log.println(name, correct + " " + falsePositives + " " + map.keySet().size());
	return false;
}

//--------------------------------------------------------------------------

/**
 * Utility class used to maintain an incrementable counter to be 
 * used in hash maps.
 */
private class Entry
{

	/** The counter */
	int count;

	/** Create and reset a counter */	
	Entry() { count = 0; }

}

//--------------------------------------------------------------------------
  
}
