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
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_DEGREE = "degree";

/**
* The protocol we want to wire
*/
private final int protocolID;

/**
* The degree of the regular graph
*/
private final int degree;


// ==================== initialization ==============================
//===================================================================


public WireRegularRandom(String prefix) {

	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
	degree = Configuration.getInt(prefix+"."+PAR_DEGREE);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireRegularRandom}.*/
public void modify() {
	
	GraphFactory.wireRegularRandom(
		new OverlayGraph(protocolID), 
		degree,
		CommonRandom.r );
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
		((Linkable)n.getProtocol(protocolID)).addNeighbor(
		    Network.get(
			CommonRandom.r.nextInt(Network.size())));
	}
}

}

