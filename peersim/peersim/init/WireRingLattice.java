package peersim.init;

import peersim.graph.*;
import peersim.core.*;
import peersim.config.Configuration;

/**
* Takes a {@link Linkable} protocol and adds edges that define a ring lattice.
* Note that no
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class WireRingLattice implements Initializer {


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
* The protocol we want to wire
*/
private final int protocolID;

/**
* The degree of the regular graph
*/
private final int k;


// ==================== initialization ==============================
//===================================================================


public WireRingLattice(String prefix) {

	protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
	k = Configuration.getInt(prefix+"."+PAR_K);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireRingLattice}.*/
public void initialize() {
	
	GraphFactory.wireRingLattice( new OverlayGraph(protocolID), k );
}


}

