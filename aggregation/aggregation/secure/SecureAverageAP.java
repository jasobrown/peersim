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
//
//--------------------------------------------------------------------------

class SecureAverageData
{
	/** This protocol identifier */
	int pid;
	
	/** History protocol identifier */
	int hid;
	
	/** Blacklist protocol identifier */
	int blid;
	
}
	
	
//--------------------------------------------------------------------------
// Parameter
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to identify the blacklist protocol.
 */
public static final String PAR_BLID = "blacklist";

/** 
 * String name of the parameter used to identify the history protocol.
 */
public static final String PAR_HID = "history";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

protected SecureAverageData sad;

//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * @param prefix
 * @param obj
 */
public SecureAverageAP(String prefix)
{
	super(prefix);
  sad = new SecureAverageData();
  sad.pid  = CommonState.getPid();
  sad.blid = Configuration.getPid(prefix+"."+PAR_BLID);
  sad.hid  = Configuration.getPid(prefix+"."+PAR_HID);
}


public Object clone() throws CloneNotSupportedException
{
	SecureAverageAP clone = (SecureAverageAP) super.clone();
	clone.sad = sad;
	return clone;
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

	/* Selects a random node */
	Blacklist blacklist = (Blacklist) node.getProtocol(sad.blid);
	Node receiver = selectNeighbor(node, blacklist);
	if (receiver == null)
		return;

	/* In any case, add the exchange to the local tables; we cannot be
	 * sure whether the message will be delivered 
	 */
	History history = (History) node.getProtocol(sad.hid);
	history.addInitiated(receiver, value, CommonState.getCycle());

	if (canDeliverRequest(receiver)) {
		/* Send request */
		GeneralAggregation rdst = 
		  (GeneralAggregation) receiver.getProtocol(pid);
		rdst.deliverRequest(node, receiver, value);
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverRequest(Node initiator, Node receiver, double rvalue)
{
	/* Nodes that have been created during the current epoch do not
	 * partecipate in the aggregation protocol
	 */
	if (isNew)
		return;

	/* Update history */
	History hreceiver = (History) receiver.getProtocol(sad.hid);
	hreceiver.addReceived(initiator, rvalue, CommonState.getCycle());

	/* Check whether the node is contained in the protocol */
	Blacklist bl = (Blacklist) receiver.getProtocol(sad.blid);
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
		History hinitiator = (History) initiator.getProtocol(sad.hid);
		Node[] nodes = hinitiator.checkRandomNode(initiator, sad.hid);
		if (nodes != null) {
			bl.add(sad.blid, receiver, nodes[0], nodes[1]);
			return;
		}
	}

	/* Update the value */
	double lvalue = this.value;
	this.value = (lvalue + rvalue)/2;
	
	/* Deliver the response, if possible */
	if (canDeliverResponse(initiator)) { 
		GeneralAggregation rsrc = 
			(GeneralAggregation) initiator.getProtocol(sad.pid);
		rsrc.deliverResponse(initiator, receiver, lvalue);
	}

}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverResponse(Node initiator, Node receiver, double rvalue)
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
	int lid = FastConfig.getLinkable(CommonState.getPid());
	Linkable linkable = (Linkable) node.getProtocol(lid);
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
