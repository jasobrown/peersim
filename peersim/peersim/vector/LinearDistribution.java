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

package peersim.vector;

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;

/**
 * Initializes the values of nodes from {@link #PAR_MIN} to {@link #PAR_MAX}
 * linearly increasing.
 * Assumes nodes implement {@link SingleValue}.
 */
public class LinearDistribution 
implements Dynamics, NodeInitializer
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to determine the upper bound of the
 * values. Defaults to Network.size()-1
 */
public static final String PAR_MAX = "max";

/** 
 * String name of the parameter used to determine the lower bound of the
 * values. Defaults to -max.
 */
public static final String PAR_MIN = "min";

/** 
 * String name of the parameter that defines the protocol to initialize.
 * Parameter read will has the full name <tt>prefix+"."+PAR_PROT</tt>
 */
public static final String PAR_PROT = "protocol";

//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Max value */
private final double max;

/** Min value */
private final double min;

/** Protocol identifier */
private final int protocolID;

/** Last value assigned to a node */
private double lastval;


//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Reads configuration parameters.
 */
public LinearDistribution(String prefix)
{
	max = Configuration.getDouble(prefix+"."+PAR_MAX, Network.size()-1);
	min = Configuration.getDouble(prefix+"."+PAR_MIN,-max);
	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------


// Comment inherited from interface
public void modify()
{
	double step = (max-min)/(Network.size()-1);
	for(int i=0; i<Network.size(); ++i)
	{
		lastval = i*step+min;
		((SingleValue)Network.get(i).getProtocol(protocolID)
			).setValue(lastval);
	}
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void initialize(Node n)
{
	lastval += (max-min)/(Network.size()-1);
	if (lastval > max)
		lastval = min;
	((SingleValue) n.getProtocol(protocolID)).setValue(lastval);
}

//--------------------------------------------------------------------------

}
