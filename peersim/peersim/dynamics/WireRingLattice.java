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

/**
* Takes a {@link Linkable} protocol and adds edges that define a ring lattice.
* Note that no
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class WireRingLattice implements Dynamics {


// ========================= fields =================================
// ==================================================================


/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
*  String name of the parameter which sets defines the degree of the graph,
* see {@link GraphFactory#wireRingLattice}.
*/
public static final String PAR_K = "k";

/**
 * If this parameter is defined, method pack() is invoked on the specified
 * protocol at the end of the wiring phase. Default to false.
 */
public static final String PAR_PACK = "pack";

/**
* The protocol we want to wire
*/
private final int protocolID;

/**
* The degree of the regular graph
*/
private final int k;

/** If true, method pack() is invoked on the initialized protocol */
private final boolean pack;

// ==================== initialization ==============================
//===================================================================


public WireRingLattice(String prefix) {

	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
	k = Configuration.getInt(prefix+"."+PAR_K);
	pack = Configuration.contains(prefix+"."+PAR_PACK);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireRingLattice}.*/
public void modify() {
	
	GraphFactory.wireRingLattice( new OverlayGraph(protocolID), k );
	if (pack) {
		int size = Network.size();
		for (int i=0; i < size; i++) {
			Linkable link = (Linkable) Network.get(i).getProtocol(protocolID);
			link.pack();
		}
	}
}


}

