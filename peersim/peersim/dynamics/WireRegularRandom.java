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
		
package peersim.dynamics;

import peersim.graph.*;
import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.CommonRandom;

/**
* Takes a {@link Linkable} protocol and adds random connections. Note that no
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class WireRegularRandom implements Dynamics, NodeInitializer {


// ========================= fields =================================
// ==================================================================


/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
*  String name of the parameter to set the out-degree degree of the graph.
*/
public static final String PAR_DEGREE = "degree";

/** 
*  String name of the parameter to set if the graph should be undirected,
* that is, for each link (i,j) a link (j,i) will also be added.
*/
public static final String PAR_UNDIR = "undirected";

/**
 * If this parameter is defined, method pack() is invoked on the specified
 * protocol at the end of the wiring phase. Default to false.
 */
public static final String PAR_PACK = "pack";


/**
* The protocol we want to wire
*/
private final int pid;

/**
* The degree of the regular graph
*/
private final int degree;

/** If true, method pack() is invoked on the initialized protocol */
private final boolean pack;

private final boolean undirected;


// ==================== initialization ==============================
//===================================================================


public WireRegularRandom(String prefix) {

	pid = Configuration.getPid(prefix+"."+PAR_PROT);
	degree = Configuration.getInt(prefix+"."+PAR_DEGREE);
	undirected = Configuration.contains(prefix+"."+PAR_UNDIR);
	pack = Configuration.contains(prefix+"."+PAR_PACK);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireRegularRandom}.*/
public void modify() {
	
	GraphFactory.wireRegularRandom(
		new OverlayGraph(pid,!undirected), 
		degree,
		CommonRandom.r );
		
	if (pack) {
		int size = Network.size();
		for (int i=0; i < size; i++) {
			Linkable link=(Linkable)Network.get(i).getProtocol(pid);
			link.pack();
		}
	}
}

// -------------------------------------------------------------------

/**
* Takes {@link #PAR_DEGREE} random samples with replacement
* from the node of the overlay network.
*/
public void initialize(Node n) {

	if( Network.size() == 0 ) return;
	
	for(int j=0; j<degree; ++j)
	{
		((Linkable)n.getProtocol(pid)).addNeighbor(
		    Network.get(
			CommonRandom.r.nextInt(Network.size())));
	}
	
	if (pack) {
		((Linkable)n.getProtocol(pid)).pack();
	}
}

}

