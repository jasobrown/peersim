package scamp;

import peersim.config.Configuration;
import peersim.util.CommonRandom;
import peersim.core.*;
import peersim.dynamics.GrowingNetwork;

/**
* A network dynamics manager which can unsubscribe nodes according to the
* SCAMP protocol. If used for adding nodes then it works like its superclass.
* Since it is not intended to be used for increasing networks, a warning
* is given in that case.
*/
public class Unsubscribe extends GrowingNetwork {


// ========================= fields =================================
// ==================================================================


/** 
* String name of the parameter used to select the protocol to operate on.
* It has to be a scamp protocol.
*/
public static final String PAR_PROT = "protocol";

/**
* The protocol we want to wire
*/
private final int protocolID;


// ====================== initialization ===============================
// =====================================================================


public Unsubscribe( String prefix ) {

	super(prefix);
	if( add > 0 )
		System.err.println("Scamp.Unsubscribe: not supposed to be"+
			" used for growing networks");
	protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
}


// ===================== protected methods ==============================
// ======================================================================


/**
* Removes n random nodes from the network. Before removal, the unsubscription
* protocol is run.
* @param n the number of nodes to remove
*/
protected void remove( int n ) {

	for(int i=0; i<n; ++i)
	{
		Network.swap(
			Network.size()-1,
			CommonRandom.r.nextInt(Network.size()) );
		Scamp.unsubscribe(
			Network.get(Network.size()-1),
			protocolID );
		Network.remove();
	}
}


}

