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
 * This class implements an aggregation protocol that performs average
 * aggregation and defend itself against malicious behavior using 
 * histories and blacklists. 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class SecureAverageAP 
extends AbstractGeneralAP
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to identify the blacklist protocol.
 */
public static final String PAR_BLID = "blacklistID";

/** This is not really nice, because we are extending a protocol that
 * is already using protocol 0... */
public static final int POS_BLACKLIST = 1;

/** 
 * String name of the parameter used to identify the history protocol.
 */
public static final String PAR_HID = "historyID";

/** This is not really nice, because we are extending a protocol that
 * is already using protocol 0... */
public static final int POS_HISTORY = 2;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * @param prefix
 * @param obj
 */
public SecureAverageAP(String prefix, Object obj)
{
	super(prefix, obj);
	int pid = ((Integer) obj).intValue();
	int blacklistID = Configuration.getPid(prefix+"."+PAR_BLID);
	Protocols.setLink(pid, POS_BLACKLIST, blacklistID);
	int historyID = Configuration.getPid(prefix+"."+PAR_HID);
	Protocols.setLink(pid, POS_HISTORY, historyID);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void nextCycle(Node node, int pid)
{
	/* Nodes that have been created during the current epoch do not
	 * partecipate in the aggregation protocol
	 */
	if (isNew)
		return;

	int blacklistID = 
	  Protocols.getLink(CommonState.getPid(), POS_BLACKLIST);
	int historyID = 
	  Protocols.getLink(pid, POS_HISTORY);

	/* Selects a random node */
	Blacklist blacklist = (Blacklist) node.getProtocol(blacklistID);
	Node receiver = selectNeighbor(node, blacklist);
	if (receiver == null)
		return;

	/* In any case, add the exchange to the local tables; we cannot be
	 * sure whether the message will be delivered 
	 */
	History history = (History) node.getProtocol(historyID);
	/*
	Node[] nodes = history.checkRandomNode(node, historyID);
	if (nodes != null) {
		blacklist.add(blacklistID, node, nodes[0], nodes[1]);
		return;
	}
	*/
	history.addInitiated(receiver, value, CommonState.getT());

	if (canDeliverRequest(receiver)) {
		/* Send request */
		GeneralAggregation rdst = 
		  (GeneralAggregation) receiver.getProtocol(pid);
		rdst.deliverRequest(node, receiver, value);
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverRequest(Node initiator, Node receiver, float rvalue)
{
	/* Nodes that have been created during the current epoch do not
	 * partecipate in the aggregation protocol
	 */
	if (isNew)
		return;

	/* Identifiers */
	int blacklistID = 
	  Protocols.getLink(CommonState.getPid(), POS_BLACKLIST);
	int historyID =	
	  Protocols.getLink(CommonState.getPid(), POS_HISTORY);

	/* Update history */
	History hreceiver = (History) receiver.getProtocol(historyID);
	hreceiver.addReceived(initiator, rvalue, CommonState.getT());

	/* Check whether the node is contained in the protocol */
	Blacklist bl = (Blacklist) receiver.getProtocol(blacklistID);
	if (bl.contains(initiator))
		return;

	/* 
	 * We check here whether the remote node is malicious. If so,
	 * we assume that the malicious node respond with an empty history...
	 * so the actual history check is not performed. Otherwise, we
	 * perform the check.
	 */
	if (!(initiator.getProtocol(CommonState.getPid()) 
	   instanceof MaliciousProtocol)) {
		/* 
		 * Check the history.
		 */
		History hinitiator = (History) initiator.getProtocol(historyID);
		Node[] nodes = hinitiator.checkRandomNode(initiator, historyID);
		if (nodes != null) {
			bl.add(blacklistID, receiver, nodes[0], nodes[1]);
			return;
		}
	}

	/* Update the value */
	float lvalue = this.value;
	this.value = (lvalue + rvalue)/2;
	
	/* Deliver the response, if possible */
	if (canDeliverResponse(initiator)) { 
		GeneralAggregation rsrc = 
			(GeneralAggregation) initiator.getProtocol(CommonState.getPid());
		rsrc.deliverResponse(initiator, receiver, lvalue);
	}

}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverResponse(Node initiator, Node receiver, float rvalue)
{
	// Update value
	value = (value+rvalue)/2;
}
//--------------------------------------------------------------------------

/** 
 * Singleton array used to temporary copy the nodes that are not blacklisted
 * and select randomly among them.
 */
private static Node[] array = null;

/**
 * Picks a random neighbors among those that are not blacklisted.
 * 
 * @param node 
 *  the node containing the linkable object from which the 
 *  random neighbor should be selected
 * @param blacklist 
 *  the blacklist containing the nodes that should not be
 *  selected
 * 	
 * @return
 */
protected Node selectNeighbor(Node node, Blacklist blacklist)
{
	int pid = CommonState.getPid();
	int linkableID = Protocols.getLink(pid, POS_LINKABLE);
	Linkable linkable = (Linkable) node.getProtocol(linkableID);
	if (array == null || array.length < linkable.degree()) {
		array = new Node[linkable.degree()];
	}
	int index = 0;
	for (int i=0; i < linkable.degree(); i++) {
		Node ni = linkable.getNeighbor(i);
		if (!blacklist.contains(ni))
		  array[index++] = ni;
	}
	if (index > 0) 
		return array[CommonRandom.r.nextInt(index)];
	else
	  return null;
}

//--------------------------------------------------------------------------

}
