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

/**
* Observer to analyse the ball expansion, it the number of nodes that
* are accessable from a given node in at most 1, 2, etc steps.
* It works only after the simulation.
*/
public class BallExpansion implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* Name for the parameter maxd, which defines the maximal distance which
* we print. Defaults to 10.
*/
public static final String PAR_MAXD = "maxd";

/** 
* Name for the parameter which defines the number of nodes to print info about.
* Defaults to 1000.
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

private final int maxd;

private final int n;

private final boolean undir;

private final GraphAlgorithms ga = new GraphAlgorithms();

/** working variable */
private final int[] b;


// ===================== initialization ================================
// =====================================================================


public BallExpansion(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
	maxd = Configuration.getInt(name+"."+PAR_MAXD,10);
	n = Configuration.getInt(name+"."+PAR_N,1000);
	undir = Configuration.contains(name+"."+PAR_UNDIR);
	b = new int[maxd];
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	Graph g = new OverlayGraph(protocolID);
	if( undir ) g = new ConstUndirGraph(g);

	System.out.println(name+":");
	
	for(int i=0; i<n && i<g.size(); ++i)
	{
		ga.flooding( g, b, i );
		for(int j=0; j<b.length; ++j)
		{
			System.out.print(b[j]+" ");
		}
		System.out.println();
	}
	
	return false;
}

}


