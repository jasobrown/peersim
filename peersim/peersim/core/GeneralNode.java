package peersim.core;

import peersim.config.*;
import java.util.*;

/**
* Class that represents one node with a nework address.
* An {@link OverlayNetwork} is made of a set of nodes.
* The functionality of this class is thin: it must be able to represent
* failure states and store a list of protocols.
* It is the protocols that do the interesting job.
*/
public class GeneralNode implements Node, Fallible, Cloneable {


// ================= fields ========================================
// =================================================================

/** 
* prefix of the parameters that defines protocols.
*/
public static final String PAR_PROT = "protocol";

/**
* The protocols composing this node.
*/
private Protocol[] protocol = null;

/**
* This package private field tells the index of this node in the node
* list of the {@link OverlayNetwork}. This is necessary to allow
* the implementation of efficient graph algorithms.
*/
private int index;

// ================ constructor and initialization =================
// =================================================================

public GeneralNode() {
	
	ArrayList list = new ArrayList();
	int i = 0;
	String protpar = PAR_PROT + "." + i;
	while( Configuration.contains(protpar) )
	{
		Protocol protocol = (Protocol) 
			Configuration.getInstance(PAR_PROT + "." + i,
				new Integer(i));
		list.add(protocol);
		i++;
		protpar = PAR_PROT + "." + i;
	}
	protocol = new Protocol[list.size()];
	list.toArray(protocol);
}		


// -----------------------------------------------------------------

public Object clone() throws CloneNotSupportedException {
	
	GeneralNode result = (GeneralNode)super.clone();
	result.protocol = new Protocol[protocol.length];
	for(int i=0; i<protocol.length; ++i)
		result.protocol[i] = (Protocol)protocol[i].clone();
	return result;
}


// =============== public methods ==================================
// =================================================================


public void setFailState(int failState) {
	
	switch(failState)
	{
		case OK:
			throw new IllegalStateException(
				"Cannot set OK when already DEAD");
		case DEAD:
			protocol = null;
			index = -1;
			break;
		default:
			throw new IllegalArgumentException(
				"failState="+failState);
	}
}

// -----------------------------------------------------------------

public int getFailState() { return (protocol==null ? DEAD : OK); }

// ------------------------------------------------------------------

public boolean isUp() { return getFailState()==OK; }

// -----------------------------------------------------------------

public Protocol getProtocol(int i) { return protocol[i]; }

//------------------------------------------------------------------

public int protocolSize() { return protocol.length; }

//------------------------------------------------------------------

public int getIndex() { return index; }

//------------------------------------------------------------------

public void setIndex(int index) { this.index = index; }
	
//------------------------------------------------------------------

public String toString() 
{
	StringBuffer buffer = new StringBuffer();
	buffer.append("index: "+index+"\n");
	for(int i=0; i<protocol.length; ++i)
	{
		buffer.append("protocol["+i+"]="+protocol[i]+"\n");
	}
	return buffer.toString();
}

//------------------------------------------------------------------

// Comment inherited from interface
public int degree(int protocolId)
{
	return ((Linkable) protocol[protocolId]).degree();
}

//------------------------------------------------------------------

// Comment inherited from interface
public Node getNeighbor(int protocolId, int i)
{
	return ((Linkable) protocol[protocolId]).getNeighbor(i);
}

//------------------------------------------------------------------

public void setNeighbor(int protocolId, int i, Node node)
{
	throw new IllegalArgumentException("Not supported yet!");
}

//------------------------------------------------------------------

// Comment inherited from interface
public boolean addNeighbor(int protocolId, Node neighbor)
{
	return ((Linkable) protocol[protocolId]).addNeighbor(neighbor);
}

//------------------------------------------------------------------

// Comment inherited from interface
public boolean contains(int protocolId, Node neighbor)
{
	return ((Linkable) protocol[protocolId]).contains(neighbor);
}

//------------------------------------------------------------------

// Comment inherited from interface
public void pack(int protocolId)
{
	((Linkable) protocol[protocolId]).pack();
}

}

