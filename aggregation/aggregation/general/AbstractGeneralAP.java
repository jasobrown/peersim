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
import peersim.cdsim.*;
import peersim.config.*;
import peersim.util.*;

/**
 * Abstract class to be implemented by aggregation functions. Its task
 * is to maintain a single value, to provide methods to access it, and
 * to maintain the relationship between this protocol and the Linkable
 * protocol used for communication. Furthermore, the class is able
 * to manage problems related to symmetric and asymmetric communication
 * failures.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public abstract class AbstractGeneralAP 
implements GeneralAggregation, CDProtocol
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/**
 * String name of the parameter used to select the linkable protocol 
 * used to obtain information about neighbors.
 */
public static final String PAR_CONN = "linkableID";

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

/** 
 * The position of the linkable protocol in the array of protocols used 
 * by this one.
 */
public static final int POS_LINKABLE = 0;


//--------------------------------------------------------------------------
// Static fields
//--------------------------------------------------------------------------

/**
 * Symmetric failure probability.
 * Temporarily, this implementation is based on the assumption that
 * multiple instances of this protocol share the same probabilities.
 */
private static double symProb;

/**
 * Asymmetric failure probability.
 * Temporarily, this implementation is based on the assumption that
 * multiple instances of this protocol share the same probabilities.
 */
private static double asymProb;


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Value to be averaged */
protected float value;

/** True if the node has just been created */
protected boolean isNew;

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Create a new protocol instance. The instance is considered
 * new, so it cannot partecipate in the aggregation protocol.
 */
public AbstractGeneralAP(String prefix, Object obj)
{
	// One-time configuration
	int pid = ((Integer) obj).intValue();
	int link = Configuration.getPid(prefix + "." + PAR_CONN);
	Protocols.setLink(pid, POS_LINKABLE, link);
	symProb = Configuration.getDouble(prefix + "." + PAR_SYM_FAILUREPROB, 0.0);
	asymProb = Configuration.getDouble(prefix + "." + PAR_ASYM_FAILUREPROB, 0.0);
	
	// Instance fields
	isNew = true;
}

//--------------------------------------------------------------------------

/**
 * Clone an existing instance. The clone is considered 
 * new, so it cannot partecipate in the aggregation protocol.
 */
public Object clone() throws CloneNotSupportedException
{
	AbstractGeneralAP ap = (AbstractGeneralAP) super.clone();
	ap.isNew = true;
	return ap;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * Returns true if this node has just been created, and cannot 
 * partecipate in an aggregation protocol. 
 */
public boolean isNew()
{
	return isNew;
}

//--------------------------------------------------------------------------

/**
 * Get the value to be aggregated. If the instance has not partecipated
 * in the protocol so far, the method throws an exception.
 */
public double getValue()
{
	if (isNew)
	  throw new UnsupportedOperationException();
	return value;
}

//--------------------------------------------------------------------------

/**
 * Set the value to be aggregated. At this point, the node can
 * start to partecipate in the aggregation protocol.
 */
public void setValue(double value)
{
	this.value = (float) value;
	isNew = false;
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
	if ((symProb > 0 && CommonRandom.r.nextDouble() < symProb) ||
	    (asymProb > 0 && CommonRandom.r.nextDouble() < asymProb))
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
	if (asymProb > 0 && CommonRandom.r.nextDouble() < asymProb)
		return false;  	
	return true;
}

//--------------------------------------------------------------------------
	
}
