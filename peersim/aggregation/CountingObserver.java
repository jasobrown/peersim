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
import peersim.reports.*;
import peersim.util.Log;
import peersim.config.*;

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
  
  /** Initial standard deviation */
  private double initsd = -1.0;
  
  ////////////////////////////////////////////////////////////////////////////
  // Constructor
  ////////////////////////////////////////////////////////////////////////////

  /**
   *  Creates a new observer using clear()
   */
  public CountingObserver(String name)
  {
  	this.name = name;
    accuracy = Configuration.getDouble(name+"."+PAR_ACCURACY,-1);
    pid = Configuration.getInt(name+"."+PAR_PROT);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  // Methods
  ////////////////////////////////////////////////////////////////////////////

  // Comment inherited from interface
  public boolean analyze()
  {
	int time = peersim.core.CommonState.getT();
  	Node node;
  	Aggregation protocol;
  	
  	/* Initialization */
		final int len = OverlayNetwork.size();
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		double sum = 0.0;
		double sqrsum = 0.0;
		int count = 0;

		/* Compute max, min, average */
		for (int i=0; i < len; i++) {
			node = OverlayNetwork.get(i);
			protocol = (Aggregation) node.getProtocol(pid);
			double value = protocol.getValue();
			if (value > 0) {
				if (value > max) max = value; 
				if (value < min) min = value;
				sum += value;
				sqrsum += value*value;
				count++;
			}
		}
		double average = sum / count;
		double sd = Math.sqrt(
			( ((double)count) / (count-1) ) * (sqrsum/count - average*average) );
		if (sd > initsd) {
			initsd=sd;
		}
    
    /* Printing statistics */
    Log.println(name, 
    	time + " " +             // cycle identifier
    	sd + " " +               // standard deviation
    	sd/initsd + " " +        // standard deviation reduction
      (int) 1/average + " " +  // average size
      (int) 1/min + " " +      // maximum size
      (int) 1/max + " " +      // minimum size
      count + " " +						 // Nodes with a value different from 0
      len                      // actual size
     );
    
    /* Terminate if accuracy target is reached */
		if (sd/initsd <= accuracy) {
			return true;
		} else {
			return false;
		}
  }

}

