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
* Class that provides functionality for observer dealing with graphs.
* It can efficiently create undirected version of the graph, making sure
* it is updated only when the simulation has advanced already, and provides
* some common parameters.
*/
public abstract class GraphObserver implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* If defines, the undirected version of the graph will be analized.
* Not defined by default;
*/
public static final String PAR_UNDIR = "undir";

/** The name of this observer in the configuration */
protected final String name;

protected final int protocolID;

protected final boolean undir;

protected final GraphAlgorithms ga = new GraphAlgorithms();

protected Graph g;

private long time = -1234;

private int phase = -1234;

private int ctime = -1234;

// ===================== initialization ================================
// =====================================================================


protected GraphObserver(String name) {

	this.name = name;
	protocolID = Configuration.getPid(name+"."+PAR_PROT);
	undir = Configuration.contains(name+"."+PAR_UNDIR);
}


// ====================== methods ======================================
// =====================================================================

protected void updateGraph() {
	
	if( CommonState.getTime() == time &&
	    CommonState.getCycleT() == ctime &&
	    CommonState.getPhase() == phase ) return;

	// we need to update the graph
	
	time = CommonState.getTime();
	ctime = CommonState.getCycleT();
	phase = CommonState.getPhase();

	g = new OverlayGraph(protocolID);
//	if( undir ) g = new ConstUndirGraph(g);
	if( undir ) g = new FastUndirGraph(g);
}

}



