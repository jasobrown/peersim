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

import java.util.Iterator;
import java.util.Map;
import peersim.config.Configuration;
import peersim.util.IncrementalStats;

/**
 */
public class ConnectivityObserver extends GraphObserver {


// ===================== fields =======================================
// ====================================================================

/** 
* String name of the parameter used to request cluster size statistics instead
* of the usual list of clusters. By default not set.
*/
public static final String PAR_SIZESTATS = "sizestats";
  
private final boolean sizestats;


// ===================== initialization ================================
// =====================================================================


public ConnectivityObserver(String name) {

	super(name);
	sizestats = Configuration.contains(name+"."+PAR_SIZESTATS);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	updateGraph();
	
	if(!sizestats)
	{
		System.out.println(name+": "+ga.weaklyConnectedClusters(g));
	}
	else
	{
		Map clst = ga.weaklyConnectedClusters(g);
		IncrementalStats stats = new IncrementalStats();
		Iterator it = clst.values().iterator();
		while(it.hasNext())
		{
			stats.add(((Integer)it.next()).intValue());
		}

		System.out.println(name+": "+stats);
	}
	
	return false;
}

}
