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

/**
* Takes a {@link Linkable} protocol and adds connection which for a star
* topology. No 
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class WireStar implements Initializer, NodeInitializer {


// ========================= fields =================================
// ==================================================================


/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/**
* The protocol we want to wire
*/
private final int protocolID;

/**
* Used as center in the {@link NodeInitializer} implementation.
*/
private Node center=null;

// ==================== initialization ==============================
//===================================================================


public WireStar(String prefix) {

	protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireStar} if size is larger than 0.*/
public void initialize() {
	
	if( Network.size() == 0 ) return;
	
	GraphFactory.wireStar(new OverlayGraph(protocolID));
	
	center = Network.get(0);
}

// -------------------------------------------------------------------

/**
* Adds a link to a fixed node, the center. This fixed node remains the
* same throughout consequitive calls to this method. If the center
* fails in the meantime, a new one is chosen so care should be taken.
* The center is the 0th index node at the time of the
* first call to the function.
*/
public void initialize(Node n) {
	
	if( Network.size() == 0 ) return;
	
	if( center == null || !center.isUp() ) center = Network.get(0);

	((Linkable)n.getProtocol(protocolID)).addNeighbor(center);
}

}

