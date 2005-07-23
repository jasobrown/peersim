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
		
package peersim.dynamics;

import peersim.graph.*;
import peersim.core.*;
import peersim.config.Configuration;

/**
* Takes a {@link Linkable} protocol and adds connections following the
* small-world model of Watts and Strogatz. Note that no
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class WireWS 
implements Control
{

// ========================= fields =================================
// ==================================================================

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * The beta parameter of a Watts-Strogatz graph represents the probability for a
 * node to be re-wired.
 * @config
 */
private static final String PAR_BETA = "beta";

/**
 * The degree of the graph. See {@link GraphFactory#wireRingLattice}.
 * @config
 */
private static final String PAR_DEGREE = "degree";

/**
 * If this config property is defined, method {@link Linkable#pack()} is 
 * invoked on the specified protocol at the end of the wiring phase. 
 * Default to false.
 * @config
 */
private static final String PAR_PACK = "pack";

/**
 * The protocol we want to wire
 */
private final int pid;

/**
 * The degree of the regular graph
 */
private final int degree;

/**
 * The degree of the regular graph
 */
private final double beta;

/** If true, method pack() is invoked on the initialized protocol */
private final boolean pack;


// ==================== initialization ==============================
//===================================================================


/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public WireWS(String prefix) {

	pid = Configuration.getPid(prefix+"."+PAR_PROT);
	degree = Configuration.getInt(prefix+"."+PAR_DEGREE);
	beta = Configuration.getDouble(prefix+"."+PAR_BETA);
	pack = Configuration.contains(prefix+"."+PAR_PACK);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireWS}.*/
public boolean execute()  {

	GraphFactory.wireWS(new OverlayGraph(pid),degree,beta,CommonState.r);
	
	if (pack) {
		int size = Network.size();
		for (int i=0; i < size; i++) {
			Linkable link=(Linkable)Network.get(i).getProtocol(pid);
			link.pack();
		}
	}

	return false;
}

}
