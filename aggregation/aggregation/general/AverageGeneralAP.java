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

package aggregation.general;

import peersim.core.*;
import peersim.util.*;

/**
 * Implements the average function. By extending AbstractGeneralAP,
 * this implementation is capable to deal with message losses and
 * broken links.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class AverageGeneralAP 
extends AbstractGeneralAP
{

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Default constructor for configurable objects.
 */
public AverageGeneralAP(String prefix)
{
	super(prefix);
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

	/* Select the neighbor and verify that communication is possible */
	Node receiver = selectNeighbor(node, pid);
	if (receiver != null && canDeliverRequest(receiver)) { 
		/* Send request */
		GeneralAggregation preceiver = 
		  (GeneralAggregation) receiver.getProtocol(pid);
		preceiver.deliverRequest(node, receiver, value);
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

	/* Update the value */
	double lvalue = this.value;
	this.value = (lvalue + rvalue)/2;

	if (canDeliverResponse(initiator)) {
		GeneralAggregation rsrc = (GeneralAggregation)
			initiator.getProtocol(CommonState.getPid());
		rsrc.deliverResponse(initiator, receiver, lvalue);
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverResponse(Node initiator, Node receiver, double rvalue)
{
	// Update value
	this.value = (this.value+rvalue)/2;
}

//--------------------------------------------------------------------------

/**
 * Selects a random neighbor amongs those stored in the Linkable protocol
 * used by this protocol.
 */
protected Node selectNeighbor(Node node, int pid)
{
	Linkable linkable = (Linkable) node.getProtocol(p.lid);
	Node rnode = null;
	if (linkable.degree() > 0) 
		return linkable.getNeighbor(
				CommonRandom.r.nextInt(linkable.degree()));
	else
		return null;
}

//--------------------------------------------------------------------------

}
