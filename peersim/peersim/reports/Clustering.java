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
import peersim.util.IncrementalStats;
import peersim.graph.*;

/**
* Observer to analyse the ball expansion, it the number of nodes that
* are accessable from a given node in at most 1, 2, etc steps.
* It works only after the simulation.
*/
public class Clustering implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* Name for the parameter which defines the number of nodes to print info about.
* Defaults to size of the graph.
*/
public static final String PAR_N = "n";

/** 
* If defines, the undirected version of the graph will be analized.
* Not defined by default;
*/
public static final String PAR_UNDIR = "undir";
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final int n;

private final boolean undir;


// ===================== initialization ================================
// =====================================================================


public Clustering(String name) {

	this.name = name;
	protocolID = Configuration.getPid(name+"."+PAR_PROT);
	n = Configuration.getInt(name+"."+PAR_N,Integer.MAX_VALUE);
	undir = Configuration.contains(name+"."+PAR_UNDIR);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {

	IncrementalStats stats = new IncrementalStats();
	Graph g = new OverlayGraph(protocolID);
	if( undir ) g = new ConstUndirGraph(g);

	for(int i=0; i<n && i<g.size(); ++i)
	{
		stats.add(GraphAlgorithms.clustering(g,i));
	}
	
	System.out.println(name+": "+stats);

	return false;
}

}


