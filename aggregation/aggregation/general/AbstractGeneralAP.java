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
// Protocol data helper class
//--------------------------------------------------------------------------
	
public class ProtocolData
{
	
/**
 * Probability of symmetric communication failure.  
 */
protected double symProb;

/**
 * Probability of asymmetric communicaton failure. 
 */
protected double asymProb;

/** Linkable identifier */
protected int lid;

}
	
//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

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
// Fields
//--------------------------------------------------------------------------

/** Value to be averaged */
protected double value;

/** True if the node has just been created */
protected boolean isNew;

protected AbstractGeneralAP.ProtocolData p;

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Create a new protocol instance. The instance is considered
 * new, so it cannot partecipate in the aggregation protocol.
 */
public AbstractGeneralAP(String prefix)
{
	// One-time configuration
	p = new ProtocolData();
	p.symProb = Configuration.getDouble(prefix+"."+PAR_SYM_FAILUREPROB,0.0);
	p.asymProb=Configuration.getDouble(prefix+"."+PAR_ASYM_FAILUREPROB,0.0);
	p.lid = FastConfig.getLinkable(CommonState.getPid());
	
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
	this.value = value;
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

/**
 */
public String toString()
{
	return ""+value;
}
	
}
