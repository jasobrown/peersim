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
import peersim.graph.*;
import peersim.util.IncrementalStats;
import java.util.Map;
import java.util.Iterator;

/**
* It test the network for sensiticity for random node removal.
* Of course it does not actually remove nodes, it is only an observer.
*/
public class RandRemoval implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on.
*/
public static final String PAR_PROT = "protocol";

// XXX remove side effect
/** 
* String name of the parameter which defines the number of runs of the
* iterative removal procedure to get statistics.
* Look out: if set to larger than 1 then as a side effect the overlay
* will be shuffled. Defaults to 1.
*/
public static final String PAR_N = "n";

/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final int n;

private final GraphAlgorithms ga;


// ===================== initialization ================================
// =====================================================================


public RandRemoval(String name) {

	this.name = name;
	protocolID = Configuration.getPid(name+"."+PAR_PROT);
	n = Configuration.getInt(name+"."+PAR_N,1);
	ga = new GraphAlgorithms();
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	System.out.println(name+":");
	
	if( n == 1 )
	{
		OverlayGraph og = new OverlayGraph(protocolID);
		PrefixSubGraph g = new PrefixSubGraph(og);
		IncrementalStats stats = new IncrementalStats();
	
		for( int i=og.size()/2; i>0; i-=og.size()/100)
		{
			g.setSize(i);
			Map clst = ga.weaklyConnectedClusters(g);
			stats.reset();
			Iterator it = clst.values().iterator();
			while(it.hasNext())
			{
				stats.add(((Integer)it.next()).intValue());
			}

			System.out.println(stats);
		}

		return false;
	}
		
	final int size = Network.size();
	final int steps = 50; // XXX we assume net size is at least 100
	IncrementalStats[] maxClust = new IncrementalStats[steps];
	IncrementalStats[] clustNum = new IncrementalStats[steps];
	for(int i=0; i<steps; ++i)
	{
		maxClust[i] = new IncrementalStats();
		clustNum[i] = new IncrementalStats();
	}
	
	for(int j=0; j<n; ++j)
	{
		Network.shuffle();
		OverlayGraph og = new OverlayGraph(protocolID);
		PrefixSubGraph g = new PrefixSubGraph(og);
		IncrementalStats stats = new IncrementalStats();
		
		for( int i=0; i<steps; i++)
		{
			g.setSize(size/2-i*(size/100));
			Map clst = ga.weaklyConnectedClusters(g);
			stats.reset();
			Iterator it = clst.values().iterator();
			while(it.hasNext())
			{
				stats.add(((Integer)it.next()).intValue());
			}

			maxClust[i].add(stats.getMax());
			clustNum[i].add(clst.size());
		}
	}
	
	for(int i=0; i<steps; ++i)
	{
		System.out.println( maxClust[i].getAverage() + " " +
			clustNum[i].getAverage() );
	}
	
	return false;
}

}
