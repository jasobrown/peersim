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
* Class that represents one node with a network address.
* An {@link Network} is made of a set of nodes.
* The functionality of this class is thin: it must be able to represent
* failure states and store a list of protocols.
* It is the protocols that do the interesting job.
*/
public class GeneralNode implements Node {


// ================= fields ========================================
// =================================================================

/**
* The protocols composing this node.
*/
protected Protocol[] protocol = null;

/**
* This package private field tells the index of this node in the node
* list of the {@link Network}. This is necessary to allow
* the implementation of efficient graph algorithms.
*/
protected int index;

/**
* The fail state of the node.
*/
protected int failstate = Fallible.OK;

// ================ constructor and initialization =================
// =================================================================

public GeneralNode(String prefix) {
	
	String[] names = Configuration.getNames(PAR_PROT);
	CommonState.setNode(this);
	protocol = new Protocol[names.length];
	for (int i=0; i < names.length; i++) {
		CommonState.setPid(i);
		Protocol p = (Protocol) 
			Configuration.getInstance(names[i]);
		protocol[i] = p; 
	}
}		


// -----------------------------------------------------------------

public Object clone() {
	
	GeneralNode result = null;
	try { result=(GeneralNode)super.clone(); }
	catch( CloneNotSupportedException e ) {} // never happens
	result.protocol = new Protocol[protocol.length];
	CommonState.setNode(result);
	for(int i=0; i<protocol.length; ++i) {
		CommonState.setPid(i);
		result.protocol[i] = (Protocol)protocol[i].clone();
	}
	return result;
}


// =============== public methods ==================================
// =================================================================


public void setFailState(int failState) {
	
	switch(failState)
	{
		case OK:
			if(failstate==DEAD) throw new IllegalStateException(
				"Cannot set OK when already DEAD");
			else failstate=OK;
			break;
		case DEAD:
			//protocol = null;
			index = -1;
			failstate = DEAD;
			for(int i=0;i<protocol.length;++i)
				if(protocol[i] instanceof Cleanable)
					((Cleanable)protocol[i]).onKill();
			break;
		case DOWN:
			failstate = DOWN;
			break;
		default:
			throw new IllegalArgumentException(
				"failState="+failState);
	}
}

// -----------------------------------------------------------------

public int getFailState() { return failstate; }

// ------------------------------------------------------------------

public boolean isUp() { return failstate==OK; }

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

}


