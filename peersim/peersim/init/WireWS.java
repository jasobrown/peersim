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
		
package peersim.init;

import peersim.graph.*;
import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.CommonRandom;

/**
* Takes a {@link Linkable} protocol and adds random connections. Note that no
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class WireWS 
implements Initializer 
{


// ========================= fields =================================
// ==================================================================


/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
*  String name of the property containing the beta parameter, ie the
*  probability for a node to be re-wired.
*/
public static final String PAR_BETA = "beta";

/** 
*  String name of the parameter which sets defines the degree of the graph,
* see {@link GraphFactory#wireRingLattice}.
*/
public static final String PAR_DEGREE = "degree";

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


// ==================== initialization ==============================
//===================================================================


public WireWS(String prefix) {

	pid = Configuration.getInt(prefix+"."+PAR_PROT);
	degree = Configuration.getInt(prefix+"."+PAR_DEGREE);
	beta = Configuration.getDouble(prefix+"."+PAR_BETA);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireRegularRandom}.*/
public void initialize() 
{
	GraphFactory.wireWS( new OverlayGraph(pid), degree, beta, CommonRandom.r);
}

}
