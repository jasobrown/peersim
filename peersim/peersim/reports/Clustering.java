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
public class Clustering extends GraphObserver {


// ===================== fields =======================================
// ====================================================================

/** 
* Name for the parameter which defines the number of nodes to print info about.
* Defaults to size of the graph.
*/
public static final String PAR_N = "n";

private final int n;


// ===================== initialization ================================
// =====================================================================


public Clustering(String name) {

	super(name);
	n = Configuration.getInt(name+"."+PAR_N,Integer.MAX_VALUE);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {

	IncrementalStats stats = new IncrementalStats();
	
	updateGraph();
	
	for(int i=0; i<n && i<g.size(); ++i)
	{
		stats.add(GraphAlgorithms.clustering(g,i));
	}
	
	System.out.println(name+": "+stats);

	return false;
}

}


