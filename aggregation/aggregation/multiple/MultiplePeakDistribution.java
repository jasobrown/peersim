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

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.Dynamics;

/**
 * Initializes the values to be aggregated using a multi-peak distribution.
 * The total value is subdivided equally among the specified number of nodes, 
 * while all the other values have value 0. This version is specialized for
 * multiple concurrent instances of aggregation.
 */
public class MultiplePeakDistribution 
implements Dynamics 
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to determine the total load in the 
 * system, to be distributed between peak nodes. Parameter read has the full 
 * name <tt>prefix+"."+PAR_VALUE</tt>
 */
public static final String PAR_VALUE = "value";


/** 
 * String name of the parameter used to determine the number of peaks in
 * the system. If this value is greater or equal than 1, it is 
 * interpreted as the actual number of peaks. If it is included in the
 * range [0, 1[ it is interpreted as a percentage with respect to the
 * current network size. Defaults to 1. 
 */
public static final String PAR_PEAKS = "peaks";


/** 
 * String name of the parameter that defines the protocol to initialize.
 */
public static final String PAR_PROTOCOL = "protocol";


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Total load */
private final double value;

/** Number of peaks */
private final double peaks;

/** Protocol identifier */
private final int pid;


//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Read the configuration values.
 */
public MultiplePeakDistribution(String prefix)
{
	value = Configuration.getDouble(prefix+"."+PAR_VALUE);
	pid = Configuration.getPid(prefix+"."+PAR_PROTOCOL);
	peaks = Configuration.getDouble(prefix+"."+PAR_PEAKS, 1);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void modify() 
{
	/* Obtain the number of concurrent instances of aggregation */
  int nvalues = ((MultipleValues) Network.get(0).getProtocol(pid)
	  ).size();


	/* Set the values */
	for (int j=0; j<nvalues; j++) {
		int pn = (peaks < 1 ? (int) (peaks*Network.size()) : (int) peaks);
		/* Compute the number of peaks and values at peaks */
		double vl = value/pn;
		for (int i=0; i < Network.size(); i++) {
			if (pn > 0 && Network.get(i).isUp()) {
				((MultipleValues) Network.get(i).getProtocol(pid)
				).setValue(j, vl);
				pn--;
			} else {
				((MultipleValues) Network.get(i).getProtocol(pid)
				).setValue(j, 0.0);
			}
		}
		Network.shuffle();
	}
}

//--------------------------------------------------------------------------

}
