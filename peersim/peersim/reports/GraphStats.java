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
* Prints reports on the graph like average clustering and average path length,
* based on random sampling of the nodes.
*/
public class GraphStats extends GraphObserver {


// ===================== fields =======================================
// ====================================================================


/** 
* Name for the parameter which defines the number of nodes to use fore
* sampling average path length.
* If zero is given, than no statistics
* will be printed about path length. If a negative value is given then
* the value is the full size of the graph.
* Defaults to zero.
*/
public static final String PAR_NL = "nl";

/** 
* Name for the parameter which defines the number of nodes to use to sample
* average clustering.
* If zero is given, than no statistics
* will be printed about clustering. If a negative value is given then
* the value is the full size of the graph.
* Defaults to zero.
*/
public static final String PAR_NC = "nc";

protected final int nc;

protected final int nl;


// ===================== initialization ================================
// =====================================================================


public GraphStats(String name) {

	super(name);
	nl = Configuration.getInt(name+"."+PAR_NL,0);
	nc = Configuration.getInt(name+"."+PAR_NC,0);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	System.out.print(name+": ");
	
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
		System.out.print(stats.getAverage()+" ");
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
		System.out.print(stats.getAverage()+" ");
	}
	
	System.out.println();
	return false;
}

}

