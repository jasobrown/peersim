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
import peersim.util.*;
import peersim.config.*;

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

/** This is not really nice, because we are extending a protocol that
 * is already using protocol 0... */
public static final int POS_HISTORY = 1;


//--------------------------------------------------------------------------
// Static fields
//--------------------------------------------------------------------------

/**
 * Number of exchanges started by a malicious node.
 */
private int exchanges;

/**
 * Fixed value exchanged by a malicious node.
 */
private float fixed;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * @param prefix
 * @param obj
 */
public MaliciousSecureAverageAP(String prefix, Object obj)
{
	super(prefix, obj);
	int pid = ((Integer) obj).intValue();
	int hid = Configuration.getPid(prefix+"."+PAR_HID);
	Protocols.setLink(pid, POS_HISTORY, hid);
	exchanges = Configuration.getInt(prefix+"."+PAR_EXCHANGES, 1);
	fixed = (float) Configuration.getDouble(prefix+"."+PAR_FIXED, 0);
}

public Object clone() throws CloneNotSupportedException
{
	MaliciousSecureAverageAP mha = 
		(MaliciousSecureAverageAP) super.clone();
	return mha;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void nextCycle(Node node, int pid)
{
	for (int i=0; i < exchanges; i++) {
		
		/* Select a non malicious neighbor */
		Node receiver = selectNeighbor(node, pid);

		/* In any case, add the exchange to the local tables; we cannot be
		 * sure whether the message has been delivered 
		 */
		History history = 
			(History) node.getProtocol(Protocols.getLink(pid, POS_HISTORY));
		history.addInitiated(receiver, value, CommonState.getT());

		if (canDeliverRequest(receiver)) {
			/* Send request */
			GeneralAggregation rdst = 
			  (GeneralAggregation) receiver.getProtocol(pid);
			rdst.deliverRequest(node, receiver, fixed);
		}
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverRequest(Node initiator, Node receiver, float rvalue)
{
	/* Update history */
	int hid = Protocols.getLink(CommonState.getPid(), POS_HISTORY);
	History history = (History) receiver.getProtocol(hid);
	history.addReceived(initiator, rvalue, CommonState.getT());

	/* Update the value */ // XXX Is this actually needed?
	float lvalue = this.value;
	this.value = (lvalue + (float) rvalue)/2;

	/* Deliver the response, if possible */
  if (canDeliverResponse(initiator)) { 
		GeneralAggregation rsrc = 
			(GeneralAggregation) initiator.getProtocol(CommonState.getPid());
    rsrc.deliverResponse(initiator, receiver, fixed);
	}
	
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverResponse(Node initiator, Node receiver, float value)
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
		int k = CommonRandom.r.nextInt(Network.size());
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
