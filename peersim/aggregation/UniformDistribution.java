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

import peersim.config.*;
import peersim.core.*;
import peersim.util.CommonRandom;
import peersim.dynamics.Dynamics;

/**
 * Initializes the values to be aggregated based on a linear distribution.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class UniformDistribution 
implements Dynamics 
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to determine the upper bound of the
 * uniform random variable.
 */
public static final String PAR_MAX = "max";

/** 
 * String name of the parameter used to determine the lower bound of the
 * uniform random variable. Defaults to -max.
 */
public static final String PAR_MIN = "min";

/** 
 * String name of the parameter that defines the protocol to initialize.
 * Parameter read will has the full name
 * <tt>prefix+"."+PAR_PROT</tt>
 */
public static final String PAR_PROTID = "protocolID";

//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Max value */
private final double max;

/** Min value */
private final double min;

/** Protocol identifier */
private final int pid;

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Read configuration parameters.
 */
public UniformDistribution(String prefix)
{
	max = Configuration.getDouble(prefix+"."+PAR_MAX);
	min = Configuration.getDouble(prefix+"."+PAR_MIN,-max);
	pid = Configuration.getInt(prefix+"."+PAR_PROTID);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------


// Comment inherited from interface
public void modify()
{
	double d = max-min;
	double tmp;
	for(int i=0; i<Network.size(); ++i)
	{
		tmp = CommonRandom.r.nextDouble()*d+min;
		((Aggregation)Network.get(i).getProtocol(pid)
			).setValue(tmp);
	}
}

//--------------------------------------------------------------------------

}
