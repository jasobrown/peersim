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

/**
 * This class represents a malicious implementation of the aggregation 
 * protocol: instead of partecipating correctly to the protocol, this 
 * node continously reports the same value.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MaliciousFixedAverageAP 
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
private double fixed;


//--------------------------------------------------------------------------
// Constructor and initialization
//--------------------------------------------------------------------------

/**
 * Default constructor for configurable objects.
 */
public MaliciousFixedAverageAP(String prefix)
{
	super(prefix);
	exchanges = Configuration.getInt(prefix+"."+PAR_EXCHANGES, 1);
	fixed = (double) Configuration.getDouble(prefix+"."+PAR_FIXED, 0);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void nextCycle(Node node, int pid)
{
	for (int i=0; i < exchanges; i++) {
		/* Select the neighbor and verify that communication is possible */
		int k = CommonState.r.nextInt(Network.size());
		Node receiver = Network.get(k);
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
public void deliverRequest(Node initiator, Node receiver, double rvalue)
{
	/* Update the value */
	double lvalue = this.value;
	this.value = (lvalue + rvalue)/2;

	if (canDeliverResponse(initiator)) {
		GeneralAggregation rsrc = 
			(GeneralAggregation) initiator.getProtocol(CommonState.getPid());
		rsrc.deliverResponse(initiator, receiver, fixed);
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void deliverResponse(Node initiator, Node receiver, double value)
{
	// Update value
	this.value = (this.value+value)/2;
}

//--------------------------------------------------------------------------

}
