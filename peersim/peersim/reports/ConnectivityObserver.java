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
import peersim.config.Configuration;
import peersim.graph.GraphAlgorithms;
import peersim.util.IncrementalStats;
import java.util.Map;
import java.util.Iterator;

/**
 */
public class ConnectivityObserver implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* String name of the parameter used to request cluster size statistics instead
* of the usual list of clusters. By default not set.
*/
public static final String PAR_SIZESTATS = "sizestats";
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final boolean sizestats;

private final GraphAlgorithms ga;


// ===================== initialization ================================
// =====================================================================


public ConnectivityObserver(String name) {

	this.name = name;
	protocolID = Configuration.getPid(name+"."+PAR_PROT);
	sizestats = Configuration.contains(name+"."+PAR_SIZESTATS);
	ga = new GraphAlgorithms();
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	OverlayGraph og = new OverlayGraph(protocolID);

	if(!sizestats)
	{
		System.out.println(name+": "+ga.weaklyConnectedClusters(og));
	}
	else
	{
		Map clst = ga.weaklyConnectedClusters(og);
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
