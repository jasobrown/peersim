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
