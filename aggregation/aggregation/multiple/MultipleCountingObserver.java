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
import peersim.cdsim.CDState;

/**
 * Print statistics for a collection of concurrent aggregation computations.
 * At each node, the t estimates obtained at the end of each epoch
 * are ordered. Subsequently, the t/3 lowest estimates and the t/3 highest 
 * estimates are discarded, and the reported estimate is given by the average 
 * of the remaining results.
 * 
 * Statistics printed are: standard deviation, standard 
 * deviation reduction, average size, maximum size, minimum 
 * size and actual size.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MultipleCountingObserver  implements Control
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to select the protocol to operate on
 */
public static final String PAR_PROTOCOL = "protocol";

/** 
 * String name of the parameter used to determine the accuracy
 * for variance before stopping the simulation. If not 
 * defined, a negative value is used as default which makes sure 
 * that the observer does not stop the simulation.
 */
public static final String PAR_ACCURACY = "accuracy";

/** 
 * String name of the parameter used to describe the length of an epoch.
 * This value is used to periodically reset the computation of the 
 * variance reduction factor, which is computed over an initial variance.
 * Defaults to Integer.MAX_VALUE, meaning that is never resetted by default.
 */
public static final String PAR_STEP = "epoch";

/**
 * String name of the parameter used to describe whether this observer
 * must print the status of the system at every cycle or at every epoch.
 * If this parameter is present, the observer will print the status
 * once every epoch.
 */
public static final String PAR_PARTIAL = "partial";


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** The name of this object in the configuration file */
private final String name;

/** Accuracy for standard deviation used to stop the simulation */
private final double accuracy;

/** True if every cycle must be reported; false otherwise */
private final boolean partial;

/** Protocol identifier */
private final int pid;

/** Length of an epoch */
private final int epoch;

/** Initial variance */
private double initvar = -1.0;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * Creates a new observer and initializes the configuration parameter.
 */
public MultipleCountingObserver(String name)
{
	this.name = name;
	partial = Configuration.contains(name+"."+PAR_PARTIAL);
  accuracy = Configuration.getDouble(name+"."+PAR_ACCURACY,-1);
  pid = Configuration.getPid(name+"."+PAR_PROTOCOL);
  epoch = Configuration.getInt(name+"."+PAR_STEP, Integer.MAX_VALUE);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean execute()
{
	int time = CDState.getCycle();
	
        if (time < 0 )
        {
                // XXX make this class conform to all simulation models
                // panic: the wrong simulation model
                System.err.println("To use MultipleCoutningObserver, "+
                "you need to run a cycle drive simulation.");
                System.exit(1);
        }
	
	/* Initialization */
	final int len = Network.size();
	IncrementalStats stats = new IncrementalStats();

	/* Compute max, min, average */
	int upnodes = 0;
	for (int i=0; i < len; i++) {
		Node node = Network.get(i);
		if (!node.isUp())
			continue;
		
		upnodes++;
		MultipleValues protocol = (MultipleValues) node.getProtocol(pid);
		if (!protocol.isNew()) {
			double sum = 0;
			int count = 0;
			for (int j=0; j < protocol.size(); j++) {
				sum += protocol.getValue(j);
				count++;
			}
			stats.add(sum/count);
		}

	}
	double var = stats.getVar();
	if (time % epoch == 1)
		initvar = -1;
	if (initvar < 0 && !Double.isNaN(var))
		initvar = var;
	double rate = (var == initvar ? 1 : 
		Math.pow(var / initvar, ((double) 1) / ((time-1)%epoch) )
	);
    
  /* Printing statistics */
  if (!partial || ((time % epoch)==0)) {
    Log.println(name, 
    	" TIME " + time +
    	" VAR " + var +
    	" RED " + (var/initvar) +
    	" RATE " + rate +
      " AVG " + 1/stats.getAverage() + 
      " MAX " + (int) (stats.getMin() == 0 ? Integer.MAX_VALUE : 1/stats.getMin()) +
      " MIN " + (int) 1/stats.getMax() +
      " CNT " + stats.getN() +
			" UPNODES " + upnodes +
      " SIZE " + len
     );
	}
  
  /* Terminate if accuracy target is reached */
	if (var/initvar <= accuracy) {
		return true;
	} else {
		return false;
	}
}

//--------------------------------------------------------------------------

}

