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

import aggregation.general.*;
import peersim.core.*;
import peersim.config.*;
import peersim.cdsim.CDState;

/**
 * This class represents a malicious implementation of the secure 
 * aggregation protocol: instead of partecipating correctly to the 
 * protocol, this node continously reports the same configurable value.
 * Furthermore, it is also possible that more than one exchange is
 * initiated by this protocol instance at each cycle. 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MaliciousSecureAverageAP 
extends AbstractGeneralAP
implements MaliciousProtocol
{

//--------------------------------------------------------------------------
//Protocol class data
//--------------------------------------------------------------------------

private class ProtocolData
{

	/** This protocol identifier */
	int pid;
	
	/** History identifier */
	int hid;

	/**
	 * Number of exchanges started by a malicious node.
	 */
	private int exchanges;

	/**
	 * Fixed value exchanged by a malicious node.
	 */
	private double fixed;

}
	
//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter containing the number of exchanges 
 * started by a single malicious node at each cycle. Defaults to 1.
 */
public static final String PAR_EXCHANGES = "exchanges";

/**
 * String name of the parameter containing the fixed value that is
 * exchanged by malicious nodes. Defaults to 0.
 */
public static final String PAR_FIXED = "value";

/**
 * String name of the parameter used to select the history protocol. 
 */
public static final String PAR_HID = "historyID";


//--------------------------------------------------------------------------
// Static fields
//--------------------------------------------------------------------------

/**
 * This protocol data
 */
private ProtocolData p;

//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * @param prefix
 */
public MaliciousSecureAverageAP(String prefix)
{
	super(prefix);
	p = new ProtocolData();
	p.pid = CDState.getPid();
	p.hid = Configuration.getPid(prefix+"."+PAR_HID);
	p.exchanges = Configuration.getInt(prefix+"."+PAR_EXCHANGES, 1);
	p.fixed = (double) Configuration.getDouble(prefix+"."+PAR_FIXED, 0);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void nextCycle(Node node, int pid)
{
	for (int i=0; i < p.exchanges; i++) {
		
		/* Select a non malicious neighbor */
		Node receiver = selectNeighbor(node, pid);

		/* In any case, add the exchange to the local tables; we cannot be
		 * sure whether the message has been delivered 
		 */
		History history = 
			(History) node.getProtocol(p.hid);
		history.addInitiated(receiver, value, CDState.getCycle());

		if (canDeliverRequest(receiver)) {
			/* Send request */
			GeneralAggregation rdst = 
			  (GeneralAggregation) receiver.getProtocol(pid);
			rdst.deliverRequest(node, receiver, p.fixed);
		}
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverRequest(Node initiator, Node receiver, double rvalue)
{
	/* Update history */
	History history = (History) receiver.getProtocol(p.hid);
	history.addReceived(initiator, rvalue, CDState.getCycle());

	/* Update the value */ // XXX Is this actually needed?
	double lvalue = this.value;
	this.value = (lvalue + rvalue)/2;

	/* Deliver the response, if possible */
  if (canDeliverResponse(initiator)) { 
		GeneralAggregation rsrc = 
			(GeneralAggregation) initiator.getProtocol(p.pid);
    rsrc.deliverResponse(initiator, receiver, p.fixed);
	}
	
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverResponse(Node initiator, Node receiver, double value)
{
	// System.err.print("+");
	// Update value
	this.value = (this.value+ value)/2;
}

//--------------------------------------------------------------------------

private Node selectNeighbor(Node node, int pid)
{
	Node receiver;
	boolean found = false;
	do {
		int k = CDState.r.nextInt(Network.size());
		receiver = Network.get(k);
	} while (receiver.getProtocol(pid) instanceof MaliciousProtocol);
	return receiver;
}

//--------------------------------------------------------------------------

/**
 * This method has been added because the check whether the node
 * can partecipate in the protocol is done locally at the specified
 * node; so, since in this version of the protocol we do not perform
 * any check, this has no problem at all.
 * But in this way, the counting observers does not count this node,
 * in order to obtain the average of the correct nodes.
 */
public boolean isNew()
{
	return true;
}

//--------------------------------------------------------------------------

public double getValue()
{
	throw new UnsupportedOperationException();
}

//--------------------------------------------------------------------------

}
