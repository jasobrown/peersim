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
import peersim.util.*;
import peersim.graph.*;

/**
 */
public class DegreeStats implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";
  
/** 
* Name for the parameter which defines the number of nodes to use to sample
* degree.
* Defaults to full size of the graph.
*/
public static final String PAR_N = "n";

/** 
* If defined, the directed version of the graph will be analized, otherwise
* the undirected version.
* Not defined by default.
*/
public static final String PAR_DIR = "directed";

/**
* If defined, then the given number of nodes will be traced. That is,
* it is guaranteed that in each call the same nodes will be picked in the
* same order. If a nodes fails which is being traced, 0 will be its degree.
* Not defined by default.
*/
public static final String PAR_TRACE = "trace";

/**
* Selects a method to use when printing results. Three methods are known:
* "stats" will create and print a {@link IncrementalStats} object.
* "freq" will create and print a {@link IncrementalFreq} object.
* "list" will print the degrees of the samle nodes one by one in one line.
* Default is "stats".
*/
public static final String PAR_METHOD = "method";

/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final int n;

private final boolean dir;

private final boolean trace;

private Node[] traced=null;

private final String method;


// ===================== initialization ================================
// =====================================================================


public DegreeStats(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
	n = Configuration.getInt(name+"."+PAR_N,-1);
	dir = Configuration.contains(name+"."+PAR_DIR);
	trace = Configuration.contains(name+"."+PAR_TRACE);
	method = Configuration.getString(name+"."+PAR_METHOD,"stats");
}


// ====================== methods ======================================
// =====================================================================


private int getNodeId(int i) {
	
	if( trace )
	{
		if( traced == null )
		{
			int nn = (n<0?Network.size():n);
			traced = new Node[nn];
			for(int j=0; j<nn; ++j)
				traced[j]=Network.get(j);
		}
		return traced[i].getIndex();
	}

	return i;
}

// ---------------------------------------------------------------------

public boolean analyze() {
	
	Graph og = new OverlayGraph(protocolID);
	if( !dir ) og = new ConstUndirGraph(og);
	final int nn = (n<0?Network.size():n);
 
	if( method.equals("stats") )
	{
		IncrementalStats stats = new IncrementalStats();
		for(int i=0; i<nn; ++i)
			stats.add(og.getNeighbours(getNodeId(i)).size());
	
		System.out.println(name+": "+stats);
	}
	else if( method.equals("freq") )
	{
		IncrementalFreq stats = new IncrementalFreq();
		for(int i=0; i<nn; ++i)
			stats.add(og.getNeighbours(getNodeId(i)).size());
	
		stats.print(System.out);
		System.out.println("\n\n");
	}
	else if( method.equals("list") )
	{
		System.out.print(name+": ");
		for(int i=0; i<nn; ++i)
			System.out.print(
				og.getNeighbours(getNodeId(i)).size()+" ");
		System.out.println();
	}
	
	return false;
}

}

