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

/**
* Class that represents one node with a network address.
* An {@link Network} is made of a set of nodes.
* The functionality of this class is thin: it must be able to represent
* failure states and store a list of protocols.
* It is the protocols that do the interesting job.
*/
public interface Node extends Fallible, Cloneable {
// XXX ugly design with the indexes but efficiency is first

	/**
	* Returns the i-th protocol in this node.
	*/
	public Protocol getProtocol(int i);

  /**
   *  Substitutes the i-th protocol of this node with the specified
   *  protocol.
   */
  public void setProtocol(int i, Protocol protocol);

	/**
	* Returns the number of protocols included in this node.
	*/
	public int protocolSize();

	/**
	* Sets an integer identifier for this node. Applications should
	* not use this method. It is provided for the core system.
	* It is public only because in a java interface it is not possible
	* to define non-public methods, and other solutions, like adapters
	* supporting indexing increase memory consumption which is a great
	* problem.
	*/
	public void setIndex(int index);
	
	/**
	* Returns an integer identifier for this node. Applications
	* will not need this method. It is provided for the core system.
	* It is public only becuase in a java interface it is not possible
	* to define non-public methods, and other solutions, like adapters
	* supporting indexing increase memory consumption which is a great
	* problem.
	*/
	public int getIndex();
  
	/**
	* We have to include this to change the access right to public.
	*/
	public Object clone() throws CloneNotSupportedException;

// XXX Additional methods to simplify coding. It is somewhat ugly
// because if Linkable changes, we have to change this too, but
// it saves some typing. Any better idea to save typing?
  
	/**
	* Returns the size of the neighbor list of the specified protocol.
	* 
	* @param protocolId the protocol to be used for this method
	*/
	public int degree(int protocolId);

	/**
	* Returns the neighbor with the given index. The contract is that
	* listing the elements from index 0 to index degree()-1 should list
	* each element exactly once if this object is not modified in the
	* meantime.
	* 
	* @param protocolId the protocol to be used for this method
	*/
	public Node getNeighbor(int protocolId, int i);

	public void setNeighbor(int protocolId, int i, Node node);

	/**
	*  Add a neighbor to the current set of neighbors.
	* 
	*  @return true if the neighbor has been inserted; false if the 
	*    node is already a neighbor of this node
	*/
	public boolean addNeighbor(int protocolId, Node neighbour);

	/**
	* Returns true if the given node is a member of the neighbor set.
	*/
	public boolean contains(int protocolId, Node neighbor);
	
	/**
	* A possibility for optimization. An implementation should try to
	* compress its internal representation. Normally this should be called
	* when no increase in the expected size of the neighborhood can be
	* expected.
	*/
	public void pack(int protocolId);
}

