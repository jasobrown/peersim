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
		
package peersim.core;

import peersim.config.Configuration;
import peersim.util.CommonRandom;

/**
* This class forms the basic framework of all simulations.
* This is completely static which is based on the assumption that we
* will simulate only one overlay network at a time.
* This allows us to reduce memory usage in many cases by allowing all the
* components to directly reach the fields of this class without having to store
* a reference.
* The overlay network is a set of nodes imlpemented line an array list for the
* sake of efficiency.
* Each node has an array of protocols. The protocols within a node can
* interact directly as defined by their implementation, and can be imagined as
* processes running in a common local environment.
* The set of objects (processes) at a given position of this array form an
* overlay network.
*/
public class Network {


// ========================= fields =================================
// ==================================================================


/**
* This config property defines the node class to be used. If not
* set, then {@link GeneralNode} will be used.
*/
public static final String PAR_NODE = "overlay.node";

/**
* this config property defines the maximal size of the overlay network
* during the whole simulation. Allows for optimalization. If not set
* then {@link #PAR_SIZE} will be used.
*/
public static final String PAR_MAXSIZE = "overlay.maxSize";

/**
* this config property defines the initial size of the overlay network.
* This property is required.
*/
public static final String PAR_SIZE = "overlay.size";

/**
* The node array. This is not a private array which is not nice but
* efficiency has the highest priority here. The main purpose is to allow
* the package quick reading of the contents in a maximally flexible way.
* Nevertheless, methods of this class should be used instead of the array
* when modifiying the contents.
* Because this array is not private,
* it is necessary to know that the actual node set is only the first
* {@link #size()} items of the array.
*/
static Node[] node = null;

/**
* Actual size of the network.
*/
private static int len;

/**
* The prototype node which is used to populate the simulation via cloning.
* Initializers will set the properties of the node if necessary.
*/
public static Node prototype = null;


// ====================== initialization ===========================
// =================================================================


public static void reset() {

	len = Configuration.getInt(PAR_SIZE);

	int maxlen = Configuration.getInt(PAR_MAXSIZE,len);

	if( maxlen < len ) throw new IllegalArgumentException(
			PAR_MAXSIZE+" is less than "+PAR_SIZE);

	node = new Node[maxlen];
	
	// creating prototype node
	Node tmp = null;
	if (!Configuration.contains(PAR_NODE)) {
		System.err.println("Network: no node defined, using GeneralNode");
		tmp = new GeneralNode("");
	} else {
		tmp = (Node) Configuration.getInstance(PAR_NODE);
	}
	prototype = tmp;

	// cloning the nodes
	if(len > 0 )
	try
	{
		for(int i=0; i<len; ++i)
		{
			node[i] = (Node)prototype.clone();
			node[i].setIndex(i);
		}
	}
	catch(CloneNotSupportedException e)
	{
		// this should never happen unless a node implementation
		// throws it explicitly (but why would it?)
		throw new Error(""+e);
	}
}


// =============== public methods ===================================
// ==================================================================


public static int size() { return len; }

// ------------------------------------------------------------------

/**
* Sets the capacity of the internal array storing the nodes.
* The nodes will remain the same in the same order.
* If the new capacity is less than the
* old size of the node list, than the end of the list is cut.
*/
public static void setCapacity(int newSize) {

	if( node == null || newSize != node.length )
	{
		Node[] newnodes = new Node[newSize];
		final int l = Math.min(node.length,newSize);
		System.arraycopy(node,0,newnodes,0,l);
		node = newnodes;
		if( len > newSize ) len = newSize;
	}
}

// ------------------------------------------------------------------

/**
* Returns the maximal number of nodes that can be stored without increasing
* the storage capacity.
*/
public static int getCapacity() { return node.length; }

// ------------------------------------------------------------------

/**
* The node will be appended to the end of the list. If necessary, the
* capacity of the internal array is increased.
*/
public static void add( Node n ) {
	
	if(len==node.length) setCapacity(3*node.length/2);
	node[len] = n;
	n.setIndex(len);
	len++;
}

// ------------------------------------------------------------------

/**
* Returns node with the given index. Note that the same node will normally
* have a different index in different times.
* This can be used as a random access iterator.
* This method does not perfomr range checks to increase efficiency.
* The maximal valid index is {@link #size()}.
*/
public static Node get( int index ) {
	
	return node[index];
}

// ------------------------------------------------------------------

/**
* The node at the end of the list is removed. Returns the removed node.
* It also sets the fail state of the node to {@link Fallible#DEAD}.
* This is because
* the rest of the network should not sense this removed node as alive
* anymore as it is not part of the node set.
*/
public static Node remove() {
	
	Node n = node[len-1]; // if len was zero this throws and exception
	node[len-1]=null;
	len--;
	n.setFailState(Fallible.DEAD);
	return n;
}

// ------------------------------------------------------------------

/**
* The node with the given index is removed. Returns the removed node.
* It also sets the fail state of the node to {@link Fallible#DEAD}.
* This is because
* the rest of the network should not sense this removed node as alive
* anymore as it is not part of the node set.
* <p>Look out: the index of the other nodes will not change (the right
* hand side of the list is not shifted to the left) except that of the last
* node. Only the
* last node is moved to the given position and will get index i.
*/
public static Node remove(int i) {
	
	if( i<0 || i>=len ) throw new IndexOutOfBoundsException(""+i);
	swap(i,len-1);
	return remove();
}

// ------------------------------------------------------------------

/**
* Swaps the two nodes at the given indeces. This is useful as a building
* block for shuffling the array. shuffling is necessary because removal
* of random elements is not supported, only the removal of the last element.
*/
public static void swap(int i, int j) {
	
	Node n = node[i];
	node[i] = node[j];
	node[j] = n;
	node[j].setIndex(j);
	node[i].setIndex(i);
}

// ------------------------------------------------------------------

/**
* Shuffles the node array taking into account the index fields of nodes.
*/
public static void shuffle() {
	
	for(int i=len; i>1; i--) swap(i-1, CommonRandom.r.nextInt(i));
}

// ------------------------------------------------------------------

public static void test() {
	
	System.err.println("len="+len);
	System.err.println("node.length="+node.length);
	for(int i=0; i<len; ++i)
	{
		System.err.println("node["+i+"]");
		System.err.println(node[i].toString());
	}

	peersim.graph.GraphIO.writeUCINET_DL(new OverlayGraph(0),System.err);
	
}

}

