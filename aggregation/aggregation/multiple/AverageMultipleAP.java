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

package aggregation.multiple;

import peersim.core.*;
import peersim.util.*;
import peersim.config.*;
import peersim.cdsim.*;


/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class AverageMultipleAP
implements MultipleValues, CDProtocol
{

//--------------------------------------------------------------------------
// Protocol data helper class
//--------------------------------------------------------------------------
	
	
protected class ProtocolData
{
	
/**
 * The probability of symmetric failure in communication. If the failure
 * is symmetric, it means that a node A can communicate with a node B if 
 * and only if B can communicate with A. This failure probability is
 * indipendent from the probability of asymmetric failure.
 * Temporarily, this implementation is based on the assumption that
 * there multiple instances of this protocol share the same probabilities.
 */
protected double symProb;

/**
 * The probability of asymmetric failure in communication. If the failure
 * is asymmetric, it means that at each communication, there is the 
 * probability of losing a single message. This failure probability is
 * indipendent from the probability of symmetric failure.
 * Temporarily, this implementation is based on the assumption that
 * there multiple instances of this protocol share the same probabilities.
 */
protected double asymProb;

/** 
 * Identifier of the linkable protocol. 
 */
protected int lid;	
	
}
	
	
//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/**
 * String name of the parameter determining the number of concurrent 
 * instances of the average aggregation protocol to be executed.
 */
public static final String PAR_VALUES = "values";

/** 
 * String name of the parameter containing the probability of 
 * symmetric failure. 
 * Symmetric failure means that a node A can communicate with a node B if 
 * and only if B can communicate with A. This failure probability is
 * indipendent from the probability of asymmetric failure.
 * Defaults to 0.
 */
private final static String PAR_SYM_FAILUREPROB = "failure.symmetric";

/** 
 * String name of the parameter containing the probability of 
 * asymmetric failure.
 * Asymmetric failure means that in each exchange, there is a possibility
 * of losing any of the messages composing it. This failure probability is
 * indipendent from the probability of symmetric failure.
 * Defaults to 0.
 */
private final static String PAR_ASYM_FAILUREPROB = "failure.asymmetric";


//--------------------------------------------------------------------------
// Static fields
//--------------------------------------------------------------------------

/* Temporary buffer used to sort arrays */
private static float[] buffer;


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Value to be averaged */
protected float[] values;

/** True if the node has just been created */
protected boolean isNew;

/** Information associated to this instance */
protected ProtocolData p;

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Create a new protocol instance. The instance is considered
 * new, so it cannot partecipate in the aggregation protocol.
 */
public AverageMultipleAP(String prefix)
{
	// One-time configuration
	p = new ProtocolData();
	p.symProb = Configuration.getDouble(prefix + "." + PAR_SYM_FAILUREPROB, 0.0);
	p.asymProb = Configuration.getDouble(prefix + "." + PAR_ASYM_FAILUREPROB, 0.0);
	p.lid = FastConfig.getLinkable(CommonState.getPid());
	
	// Instance fields
	values = new float[Configuration.getInt(prefix + "." + PAR_VALUES)];
	isNew = true;
}

//--------------------------------------------------------------------------

/**
 * Clone an existing instance. The clone is considered 
 * new, so it cannot partecipate in the aggregation protocol.
 */
public Object clone() throws CloneNotSupportedException
{
	AverageMultipleAP ap = (AverageMultipleAP) super.clone();
	ap.values = new float[values.length];
	ap.isNew = true;
	ap.p = p;
	return ap;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean isNew()
{
	return isNew;
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public double getValue(int pos)
{
	if (isNew)
		throw new UnsupportedOperationException();
	return values[pos];
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public void setValue(int pos, double value)
{
	isNew = false;
	values[pos] = (float) value;
}

//--------------------------------------------------------------------------

//Comment inherited from interface
public int size()
{
	return values.length;
}

//--------------------------------------------------------------------------

/**
 * Returns true if it possible to deliver a request to the specified node,
 * false otherwise.
 */
protected boolean canDeliverRequest(Node node)
{
	if (node.getFailState() == Fallible.DEAD)
		return false;
	if ((p.symProb > 0 && CommonRandom.r.nextDouble() < p.symProb) ||
			(p.asymProb > 0 && CommonRandom.r.nextDouble() < p.asymProb))
		return false;
	return true;
}

//--------------------------------------------------------------------------

/**
 * Returns true if it possible to deliver a response to the specified node,
 * false otherwise.
 */
protected boolean canDeliverResponse(Node node)
{
	if (node.getFailState() == Fallible.DEAD)
		return false;
	if (p.asymProb > 0 && CommonRandom.r.nextDouble() < p.asymProb)
		return false;
	return true;
}

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
	for (int i=0; i < values.length; i++) {
		Node receiver = selectNeighbor(node, pid);
		if (receiver != null && canDeliverRequest(receiver)) { 
			/* Send request */
			AverageMultipleAP preceiver = 
				(AverageMultipleAP) receiver.getProtocol(pid);
			preceiver.deliverRequest(node, receiver, values[i], i);
		}
	}
}

//--------------------------------------------------------------------------

/**
 * Simulates the sending of a message to initiate an exchange with a peer
 * node. This method is invoked by the initiator of an exchange on the
 * receiver node of the exchange. If the simulated message is 
 * not lost, a well-behaving receiver should send its current estimate
 * to the initiator through method <code>deliverResponse()</code>, and 
 * update its estimate with the corresponding value.
 * 
 * @param initiator the node that initiated the exchange
 * @param receiver the node that received the exchange invitation
 * @param value the value sent by the initiator
 * @param index the identifier of the concurrent aggregation protocol
 */
public void deliverRequest(Node initiator, Node receiver, float rvalue, int index)
{
	/* Nodes that have been created during the current epoch do not
	 * partecipate in the aggregation protocol
	 */
	if (isNew)
		return;

	/* Update the value */
	float lvalue = values[index];
	values[index] = (lvalue + rvalue)/2;
	if (canDeliverResponse(initiator)) {
		AverageMultipleAP rsrc = 
			(AverageMultipleAP) initiator.getProtocol(CommonState.getPid());
		rsrc.deliverResponse(initiator, receiver, lvalue, index);
	}
}

//--------------------------------------------------------------------------

/**
 * Simulates the sending of a message to respond to a message from a
 * peer node initiating an exchange. This method is invoked by the
 * receiver of an exchange on the initiator node of the exchange. If
 * the simulated message is not lost, a well-behaving initiator should
 * update its estimate with the corresponding value.
 * 
 * @param initiator the node that initiated the exchange
 * @param receiver the node that received the exchange invitation
 * @param value the value sent by the receiver
 * @param index the identifier of the concurrent aggregation protocol
 */
public void deliverResponse(Node initiator, Node receiver, float rvalue, int index)
{
	/* Update the value */
	values[index] = (values[index] + rvalue)/2;
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
		return linkable.getNeighbor(CommonRandom.r.nextInt(linkable.degree()));
	else
		return null;
}

//--------------------------------------------------------------------------

}
