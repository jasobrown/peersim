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
public class WireRegularRandom implements Initializer, NodeInitializer {


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

	protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
	degree = Configuration.getInt(prefix+"."+PAR_DEGREE);
}


// ===================== public methods ==============================
// ===================================================================


/** calls {@link GraphFactory#wireRegularRandom}.*/
public void initialize() {
	
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

	if( OverlayNetwork.size() == 0 ) return;
	
	for(int j=0; j<degree; ++j)
	{
		((Linkable)n.getProtocol(protocolID)).addNeighbor(
		    OverlayNetwork.get(
			CommonRandom.r.nextInt(OverlayNetwork.size())));
	}
}

}

