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
		
package peersim.reports;

import peersim.core.*;
import peersim.config.*;
import peersim.util.IncrementalStats;

/**
* Print statistics over the network assuming the nodes implement
* {@link SingleValue}.
* Statistics printed are: standard deviation, standard 
* deviation reduction, average/maximum/minimum of averages,
* and actual size.
*/
public class SingleValueObserver implements Observer {


////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////

/** 
 *  String name of the parameter used to determine the accuracy
 *  for standard deviation before stopping the simulation. If not 
 *  defined, a negative value is used which makes sure the observer 
 *  does not stop the simulation
 */
public static final String PAR_ACCURACY = "accuracy";

/** 
 *  String name of the parameter used to select the protocol to operate on
 */
public static final String PAR_PROT = "protocol";

////////////////////////////////////////////////////////////////////////////
// Fields
////////////////////////////////////////////////////////////////////////////

/** The name of this observer in the configuration */
private final String name;

/** Accuracy for standard deviation used to stop the simulation */
private final double accuracy;

/** Protocol identifier */
private final int pid;

////////////////////////////////////////////////////////////////////////////
// Constructor
////////////////////////////////////////////////////////////////////////////

/**
 *  Creates a new observer using clear()
 */
public SingleValueObserver(String name)
{
	this.name = name;
	accuracy = Configuration.getDouble(name + "." + PAR_ACCURACY, -1);
	pid = Configuration.getPid(name + "." + PAR_PROT);
}

////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////

// Comment inherited from interface
public boolean analyze()
{
	IncrementalStats stats = new IncrementalStats();
	
	/* Compute max, min, average */
	for (int i = 0; i < Network.size(); i++)
	{
		// XXX take care of a getValue() interface
		SingleValue v = (SingleValue)Network.get(i).getProtocol(pid);
		stats.add( v.getValue() );
	}

	/* Printing statistics */
	System.out.println(name+": "+stats);

	/* Terminate if accuracy target is reached */
	return (stats.getStD()<=accuracy && CommonState.getTime()>0);
}

}
