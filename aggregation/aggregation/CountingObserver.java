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
import peersim.vector.*;
import peersim.reports.*;
import peersim.util.Log;
import peersim.config.*;
import peersim.util.*;

/**
 * Print statistics for a counting aggregation computation.
 * Statistics printed are: standard deviation, standard 
 * deviation reduction, average size, maximum size, minimum 
 * size and actual size.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class CountingObserver  implements Observer
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to select the protocol to operate on
 */
public static final String PAR_PROTID = "protocol";

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
 * If this parameter is presente, the observer will print the status
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
public CountingObserver(String name)
{
	this.name = name;
	partial = Configuration.contains(name+"."+PAR_PARTIAL);
  accuracy = Configuration.getDouble(name+"."+PAR_ACCURACY,-1);
  pid = Configuration.getPid(name+"."+PAR_PROTID);
  epoch = Configuration.getInt(name+"."+PAR_STEP, Integer.MAX_VALUE);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean analyze()
{
	long time = peersim.core.CommonState.getTime();
	if ((time % epoch) == 0) {
		initvar = -1.0;
	}
	
	/* Initialization */
	final int len = Network.size();
	IncrementalStats stats = new IncrementalStats();

	/* Compute max, min, average */
	for (int i=0; i < len; i++) {
		Node node = Network.get(i);
		SingleValue protocol = (SingleValue) node.getProtocol(pid);

		try {
			stats.add(protocol.getValue());
		} catch (Exception e) {
			/* 
			 * We do nothing; the node should not be counted in the
			 */ 
		}
	}
	double var = stats.getVar();
	if (initvar < 0 || Double.isNaN(initvar))
	{
		initvar = var;
	}
	double rate = Math.pow(var / initvar, ((double) 1) / (time%epoch) );
    
  /* Printing statistics */
  if (!partial || ((time % epoch)==epoch-1)) {
    Log.println(name, 
    	" TIME " + time +
    	" VAR " + var +
    	" RED " + (var/initvar) +
    	" RATE " + rate +
      " AVG " + 1/stats.getAverage() + 
      " MAX " + (int) (stats.getMin() == 0 ? Integer.MAX_VALUE : 1/stats.getMin()) +
      " MIN " + (int) 1/stats.getMax() +
      " CNT " + stats.getN() +
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

