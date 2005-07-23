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
	
package peersim.core;

import peersim.config.*;

/**
 * This class represents a node that has a {@link Linkable} protocol
 * wired in with protocol id 0, and a singleton protocol with protocol id
 * 1.
 */
public class SingleProtocolNode 
implements Protocol, Node, Linkable 
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
* Default init capacity
*/
private static final int DEFAULT_INITIAL_CAPACITY = 10;

/**
* Initial capacity. Defaults to {@value #DEFAULT_INITIAL_CAPACITY}.
* @config
*/
private static final String PAR_INITCAP = "capacity";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** Neighbors */
private Node[] neighbors;
  
/** Actual number of neighbors in the array */
private int len;

/**
 * The (singleton) protocol run in this node.
 */
private Protocol protocol = null;

/**
 * This package private field tells the index of this node in the node
 * list of the {@link OverlayNetwork}. This is necessary to allow
 * the implementation of efficient graph algorithms.
 */
private int index;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

public SingleProtocolNode(String prefix) {
	neighbors =
		new Node[Configuration.getInt(prefix+"."+PAR_INITCAP,
			DEFAULT_INITIAL_CAPACITY)];
	len = 0;
	// Protocol 1 is the single protocol to be simulated
	protocol = (Protocol) Configuration.getInstance(PAR_PROT, new Integer(1));
}

// -----------------------------------------------------------------

public Object clone() throws CloneNotSupportedException 
{
	SingleProtocolNode result = (SingleProtocolNode) super.clone();
	result.neighbors = new Node[neighbors.length];
	System.arraycopy(neighbors, 0, result.neighbors, 0, len);
	result.len = len;
	result.protocol = (Protocol) protocol.clone();
	return result;
}

//-----------------------------------------------------------------
// Public methods
//-----------------------------------------------------------------

public void setFailState(int failState) {

	switch (failState) {
		case OK :
			throw new IllegalStateException("Cannot set OK when already DEAD");
		case DEAD :
			protocol = null;
			index = -1;
			break;
		default :
			throw new IllegalArgumentException("failState=" + failState);
	}
}

// -----------------------------------------------------------------

public int getFailState() {
	return (protocol == null ? DEAD : OK);
}

// -----------------------------------------------------------------

public boolean isUp() { return getFailState()==OK; }
  
// ------------------------------------------------------------------

public Protocol getProtocol(int i) {
	if (i == 0)
		return this;
	else if (i == 1)
		return protocol;
	else
		throw new IllegalArgumentException("This node does not support more than two protocols");
}

// ------------------------------------------------------------------

public void setProtocol(int i,Protocol p) {
	if (i == 0)
		throw new IllegalArgumentException("This node does not support changing protocol 0");
	else if (i == 1) { protocol=p; return; }
	else
		throw new IllegalArgumentException("This node does not support more than two protocols");
}

//------------------------------------------------------------------

public int protocolSize() {
	return 2;
}

//------------------------------------------------------------------

public int getIndex() {
	return index;
}

//------------------------------------------------------------------

public void setIndex(int index) {
	this.index = index;
}


//-----------------------------------------------------------------
// Methods
//-----------------------------------------------------------------


public int getCapacity()
{
	if (neighbors == null)
		return 0;
	else
		return neighbors.length;
}

//------------------------------------------------------------------

public void setCapacity(int capacity) 
{
	if (capacity == 0)
		neighbors = null;
	else {
		neighbors =
			new Node[capacity];
	}
	len = 0;
}

//------------------------------------------------------------------

public boolean contains(Node n) 
{	
	if (neighbors != null) {
		for (int i=0; i < len; i++) {
			if (neighbors[i] == n)
				return true;
		}
	}
	return false;
}
  
//------------------------------------------------------------------

public boolean addNeighbor(Node n)
{
	if (contains(n))
	  return false;
	if (len == neighbors.length) {
		Node[] temp = new Node[3*neighbors.length/2];
		System.arraycopy(neighbors,0,temp,0,neighbors.length);
		neighbors = temp;
	}
	neighbors[len] = n;
	len++;
	return true;
}
  
//------------------------------------------------------------------

public Node getNeighbor(int i)
{
	if (i >= len)
	  throw new IllegalArgumentException("No element " + i + 
			" in a node with " + len + " neighbors");
	return neighbors[i];
}

//------------------------------------------------------------------

public void setNeighbor(int i, Node node)
{
	if (i >= len)
		throw new IllegalArgumentException("No element " + i + " in a node with " + len + " neighbors");
	neighbors[i] = node;
}

//------------------------------------------------------------------

public int degree()
{
	return len;
}

//------------------------------------------------------------------

public void pack()
{
	if( neighbors == null && len == neighbors.length ) return;
	Node[] temp = new Node[len];
	System.arraycopy(neighbors,0,temp,0,len);
	neighbors = temp;
}
  
//------------------------------------------------------------------

public void onKill() { neighbors=null; }

//------------------------------------------------------------------

public String toString() {
	
	StringBuffer buffer = new StringBuffer();
	buffer.append(
	   "index="+index+" len="+len+" maxlen="+neighbors.length+" [");
	for(int i=0; i<len; ++i)
	{
		buffer.append(neighbors[i].getIndex()+" ");
	}
	return buffer.append("] "+protocol).toString();
}

//-----------------------------------------------------------------

}
