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

package aggregation;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.cdsim.CDProtocol;

/**
 * Abstract class to be implemented by aggregation functions. Its task
 * is to maintain a single value, to provide methods to access it, and
 * to maintain the relationship between this protocol and the Linkable
 * protocol used for communication.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public abstract class AbstractFunction 
implements CDProtocol, Aggregation
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/**
 * String name of the parameter used to select the linkable protocol 
 * used to obtain information about neighbors.
 */
public static final String PAR_CONN = "linkableID";


//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** Value to be aggregated */
protected double value;


//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/** Protected default constructor for extending classes */
protected AbstractFunction() {}

/**
 * Set up the relation between this protocol and the Linkable protocol
 * used for communication.
 * 
 * @param prefix string prefix for config properties
 * @param obj configuration object, containing the protocol identifier 
 *  for this protocol.
 */
public AbstractFunction(String prefix, Object obj) {

	int pid = ((Integer) obj).intValue();
	int link = Configuration.getInt(prefix+"."+PAR_CONN);
	Protocols.setLink(pid, link);
}

//--------------------------------------------------------------------------

/**
 * Clones the object, by copying the value.
 */
public Object clone() throws CloneNotSupportedException {

	AbstractFunction af = (AbstractFunction) super.clone();
	af.value = value;
	return af;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * Returns the value to be aggregated.
 */
public double getValue()
{
	return value;
}
 
//--------------------------------------------------------------------------

/**
 * Sets the value to be aggregated.
 */
public void setValue(double value)
{
	this.value = value;
}

//--------------------------------------------------------------------------

/**
 * Returns the value as a string.
 */
public String toString() { return ""+value; }

//--------------------------------------------------------------------------

}
