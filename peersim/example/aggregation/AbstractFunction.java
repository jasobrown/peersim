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

package example.aggregation;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.cdsim.CDProtocol;

/**
 * 
 *
 *  @author Alberto Montresor
 *  @version $Revision$
 */
public abstract class AbstractFunction implements CDProtocol, Aggregation
{


////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////

/**
* the suffix for the protocol connection parameter. The parameter read
* from configuration will be prefix+"."+PAR_CONN
*/
public static final String PAR_CONN = "linkable";


////////////////////////////////////////////////////////////////////////////
//Fields
////////////////////////////////////////////////////////////////////////////

/** Value to be averaged */
protected double value;


////////////////////////////////////////////////////////////////////////////
// Constructor
////////////////////////////////////////////////////////////////////////////


protected AbstractFunction() {}

public AbstractFunction(String prefix, Object obj) {}

public Object clone() throws CloneNotSupportedException {

	return super.clone();
}


////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////

/**
 *  Get the value to be aggregated.
 */
public double getValue()
{
	return value;
}
 
/**
 *  Set the value to be aggregated.
 */
public void setValue(double value)
{
	this.value = value;
}

public String toString() { return ""+value; }

}
