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
import peersim.dynamics.Dynamics;
import peersim.util.CommonRandom;

/**
*/
public class PeakDistribution implements Dynamics {

////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////

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
 * current network size. Note that using this mechanism it is not 
 * possible to create a network where 100% of the nodes are peaks,
 * unless you specify the exact size of the network. 
 * Default to 1. Parameter read has the full name
 * <tt>prefix+"."+PAR_PEAKS</tt>
 */
public static final String PAR_PEAKS = "peaks";


/** 
 * String name of the parameter that defines the protocol to initialize.
 * Parameter read will has the full name
 * <tt>prefix+"."+PAR_PROT</tt>
 */
public static final String PAR_PROT = "protocol";

////////////////////////////////////////////////////////////////////////////
// Fields
////////////////////////////////////////////////////////////////////////////

/** Total load */
private final double value;

/** Number of peaks */
private final double peaks;

/** Protocol identifier */
private final int pid;

////////////////////////////////////////////////////////////////////////////
// Initialization
////////////////////////////////////////////////////////////////////////////

public PeakDistribution(String prefix)
{
	value = Configuration.getDouble(prefix+"."+PAR_VALUE);
	pid = Configuration.getInt(prefix+"."+PAR_PROT);
	peaks = Configuration.getDouble(prefix+"."+PAR_PEAKS, 1);
}

////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////


// Comment inherited from interface
public void modify() 
{
  int pn = (peaks < 1 ? (int) (peaks*Network.size()) : (int) peaks);
  double vl = value/pn;
  for (int i=0; i < Network.size(); i++) {
		((Aggregation)Network.get(i).getProtocol(pid)).setValue(0.0);
  }
  for (int i=0; i < pn; i++) {
  	boolean found = false;
  	do {
  		int r = CommonRandom.r.nextInt(Network.size());
  		Aggregation agg = (Aggregation) Network.get(r).getProtocol(pid);
  		if (agg.getValue() == 0) {
  			agg.setValue(vl);
  			found = true;
  		}
  	} while (!found);
  }
}

}
