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
		
package newscast;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.graph.*;
import peersim.reports.Observer;

/**
* An observer class to hack with. 
*/
public class GraphStatsObserver implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final GraphAlgorithms ga;

/** The node from which we measure average path length */
private Node node = null; 


// ===================== initialization ================================
// =====================================================================


public GraphStatsObserver(String name) {

	this.name = name;
	protocolID = Configuration.getPid(name+"."+PAR_PROT);
	ga = new GraphAlgorithms();
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {

	if( node == null || !node.isUp() ) node = Network.get(0);
	OverlayGraph og = new OverlayGraph(protocolID);
	Graph undirg = new ConstUndirGraph(og);

	if(undirg.size()<10)
	{
		System.out.println(name+": graph size is too small, "+
			undirg.size());
		return false;
	}
	
	System.out.print(name+": ");
	
	for(int i=0; i<10; ++i)
		System.out.print(GraphAlgorithms.clustering(undirg,i)+" ");
	
	ga.dist(undirg,node.getIndex());

	double sum = 0.0, max = 0.0; // min is always 1
	for(int i=0; i<undirg.size(); ++i)
	{
		if(i==node.getIndex()) continue;
		if( ga.d[i] > max ) max = ga.d[i];
		sum+=ga.d[i];
	}
	System.out.print(sum/(undirg.size()-1)+" "+max);
	
	System.out.println();
	
	return false;
}

}
