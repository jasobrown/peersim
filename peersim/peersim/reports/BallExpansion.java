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

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.util.IncrementalStats;
import peersim.util.RandPermutation;

/**
* Observer to analyse the ball expansion, it the number of nodes that
* are accessable from a given node in at most 1, 2, etc steps.
* It works only after the simulation.
*/
public class BallExpansion extends GraphObserver {


// ===================== fields =======================================
// ====================================================================

/** 
* Name for the parameter maxd, which defines the maximal distance which
* we print. Defaults to the network size. Note that this default is
* normally way too much (for random graphs with a low diameter).
* Also note that the <em>initial</em> network size is used if no value is
* given which might not be what you want if eg the network is growing.
*/
public static final String PAR_MAXD = "maxd";

/** 
* Name for the parameter which defines the number of nodes to print info about.
* Defaults to 1000.
*/
public static final String PAR_N = "n";

/** 
* If defines, statistics are printed instead over the minimal path lengths.
* Not defined by default;
*/
public static final String PAR_STATS = "stats";
  
private final int maxd;

private final int n;

private final boolean stats;

/** working variable */
private final int[] b;

private final RandPermutation rp = new RandPermutation();


// ===================== initialization ================================
// =====================================================================


public BallExpansion(String name) {

	super(name);
	maxd = Configuration.getInt(name+"."+PAR_MAXD,Network.size());
	n = Configuration.getInt(name+"."+PAR_N,1000);
	stats = Configuration.contains(name+"."+PAR_STATS);
	b = new int[maxd];
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	updateGraph();
	
	System.out.print(name+": ");
	
	rp.reset(g.size());
	if(stats)
	{
		IncrementalStats is = new IncrementalStats();
		for(int i=0; i<n && i<g.size(); ++i)
		{
			ga.flooding( g, b, rp.next() );
			int j=1;
			while(j<b.length && b[j]>0)
			{
				is.add(j,b[j++]);
			}
		}
		System.out.println(is);
	}
	else
	{
		System.out.println();
		for(int i=0; i<n && i<g.size(); ++i)
		{
			ga.flooding( g, b, rp.next() );
			int j=0;
			while(j<b.length && b[j]>0)
			{
				System.out.print(b[j++]+" ");
			}
			System.out.println();
		}
	}
	
	return false;
}

}


