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

import peersim.core.*;
import peersim.dynamics.Dynamics;
import peersim.config.Configuration;

/**
* Normalizes the values. The protocol it operates on has to implement
* {@link SingleValue}.
*/
public class Normalizer implements Dynamics {

/** 
* String name of the parameter that defines the protocol to initialize.
*/
public static final String PAR_PROT = "protocol";

/** 
* String name of the parameter that defines the L1 norm (sum of absolute
* values) to normalize to. After the operation the L1 norm will be the
* one given here.
* Defaults to 1.
*/
public static final String PAR_L1 = "l1";

/** Protocol identifier */
private final int protocolID;

private final double l1;

//--------------------------------------------------------------------------

public Normalizer(String prefix)
{
	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
	l1 = Configuration.getDouble(prefix+"."+PAR_L1,1);
}

//--------------------------------------------------------------------------

/**
* Makes the sum of the absolute values (L1 norm) equal to the value given
* in the configuration parameter {@link #PAR_L1}.
*/
public void modify()
{
	double sum = 0.0;
	for(int i=0; i<Network.size(); ++i)
	{
		SingleValue sv =
			(SingleValue)Network.get(i).getProtocol(protocolID);
		sum+=Math.abs(sv.getValue());
	}
	
	
	double factor = l1/sum;
	for(int i=0; i<Network.size(); ++i)
	{
		SingleValue sv =
			(SingleValue)Network.get(i).getProtocol(protocolID);
		sv.setValue(factor*sv.getValue());
	}
}
}


