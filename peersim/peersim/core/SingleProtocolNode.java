package peersim.core;

import peersim.config.*;

/**
 * This class represents a node that is also a 
 */
public class SingleProtocolNode 
implements Protocol, Node, Linkable 
{

	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////

	/** 
	 * Prefix of the parameters that defines protocols.
	 */
	public static final String PAR_PROT = "protocol";

	public static final int DEFAULT_INITIAL_CAPACITY=10;

	public static final String PAR_INITCAP = "capacity";

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

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

	////////////////////////////////////////////////////////////////////////////
	// Constructor and initialization
	////////////////////////////////////////////////////////////////////////////

	public SingleProtocolNode(String prefix) {
		neighbors =
			new Node[Configuration.getInt(prefix+"."+PAR_INITCAP,
				DEFAULT_INITIAL_CAPACITY)];
		len = 0;
		// Protocol 0 is the idle protocol; Protocol 1 is the single protocol
		// to be simulated
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

	////////////////////////////////////////////////////////////////////////////
	// Public methods
	////////////////////////////////////////////////////////////////////////////

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

	// -----------------------------------  
	
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

	//------------------------------------------------------------------
/*
	public String toString() {
		return "[index: " + index + ", protocol: " + protocol + "]";
	}
*/
	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////


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

	public String toString() {
		return index + " " + len+" "+neighbors.length + " " + neighbors[0].getIndex();
	}

	//------------------------------------------------------------------

	// Comment inherited from interface
	public int degree(int protocolId) {
		if (protocolId == 0) {
			return degree();
		} else if (protocolId == 1) {
			return ((Linkable) protocol).degree();
		} else {
			throw new IllegalArgumentException("This node does not support more than two protocols");
		}
	}

	//------------------------------------------------------------------

	// Comment inherited from interface
	public Node getNeighbor(int protocolId, int i) {
		if (protocolId == 0) {
			return getNeighbor(i);
		} else if (protocolId == 1) {
			return ((Linkable) protocol).getNeighbor(i);
		} else {
			throw new IllegalArgumentException("This node does not support more than two protocols");
		}
	}

	//------------------------------------------------------------------

	public void setNeighbor(int protocolId, int i, Node node)
	{
		if (protocolId == 0) {
			if (i >= len)
				throw new IllegalArgumentException("No element " + i + " in a node with " + len + " neighbors");
			neighbors[i] = node;
		} else {
			throw new IllegalArgumentException("Not supported yet!");
		}
	}

	//------------------------------------------------------------------

	// Comment inherited from interface
	public boolean addNeighbor(int protocolId, Node neighbor) {
		if (protocolId == 0) {
			return addNeighbor(neighbor);
		} else if (protocolId == 1) {
			return ((Linkable) protocol).addNeighbor(neighbor);
		} else {
			throw new IllegalArgumentException("This node does not support more than two protocols");
		}
	}

	//------------------------------------------------------------------

	// Comment inherited from interface
	public boolean contains(int protocolId, Node neighbor) {
		if (protocolId == 0) {
			return contains(neighbor);
		} else if (protocolId == 1) {
			return ((Linkable) protocol).contains(neighbor);
		} else {
			throw new IllegalArgumentException("This node does not support more than two protocols");
		}
	}

	//------------------------------------------------------------------

	// Comment inherited from interface
	public void pack(int protocolId) {
		if (protocolId == 0) {
			pack();
		} else if (protocolId == 1) {
			((Linkable) protocol).pack();
		} else {
			throw new IllegalArgumentException("This node does not support more than two protocols");
		}
	}

}
