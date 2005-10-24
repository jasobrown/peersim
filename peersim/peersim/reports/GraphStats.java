/*
 * Copyright (c) 2003-2005 The BISON Project
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

import peersim.config.*;
import peersim.core.*;
import peersim.graph.*;
import peersim.util.*;

/**
* Prints reports on the graph like average clustering and average path length,
* based on random sampling of the nodes.
* In fact its functionality is a subset of the union of {@link Clustering}
* and {@link BallExpansion}, and therefore is redundant,
* but it is there for historical reasons.
*/
public class GraphStats extends GraphObserver {


// ===================== fields =======================================
// ====================================================================

/** 
* The number of nodes to use for
* sampling average path length.
* If zero is given, then no statistics
* will be printed about path length. If a negative value is given then
* the value is the full size of the graph.
* Defaults to zero.
* @config
*/
private static final String PAR_NL = "nl";

/** 
* The number of nodes to use to sample
* average clustering.
* If zero is given, then no statistics
* will be printed about clustering. If a negative value is given then
* the value is the full size of the graph.
* Defaults to zero.
* @config
*/
private static final String PAR_NC = "nc";

private final int nc;

private final int nl;


// ===================== initialization ================================
// =====================================================================


/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param name the configuration prefix for this class
 */
public GraphStats(String name) {

	super(name);
	nl = Configuration.getInt(name+"."+PAR_NL,0);
	nc = Configuration.getInt(name+"."+PAR_NC,0);
}


// ====================== methods ======================================
// =====================================================================

/**
* Returns staistics over minimal path length and clustering.
* The output is {@link IncrementalStats#toString} over the set of
* clustering coefficients of randomly selected nodes, and the
* set of distances from randomly selected nodes to all the other nodes
* (appended in one line).
* See also the configuration parameters.
* @return always false
*/
public boolean execute() {
	
	Log.print(name, "");
	
	IncrementalStats stats = new IncrementalStats();
	updateGraph();
	
	if( nc != 0 )
	{
		stats.reset();
		final int n = ( nc<0 ? g.size() : nc );
		for(int i=0; i<n && i<g.size(); ++i)
		{
			stats.add(GraphAlgorithms.clustering(g,i));
		}
		Log.print0(name, stats.getAverage()+" ");
	}
	
	if( nl != 0 )
	{
		stats.reset();
		final int n = ( nl<0 ? g.size() : nl );
		for(int i=0; i<n && i<g.size(); ++i)
		{
			ga.dist(g,i);
			for(int j=0; j<g.size(); ++j)
			{
				if( j==i ) continue;
				stats.add(ga.d[j]);
			}
		}
		Log.print0(name, stats.getAverage()+" ");
	}
	
	Log.print0(name, "\n");
	return false;
}

}

