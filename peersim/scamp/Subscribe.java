package scamp;

import peersim.init.*;
import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.CommonRandom;

/**
* Adds nodes according to the SCAMP protocol. No 
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class Subscribe implements Initializer, NodeInitializer {


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


// ===================== private methods =============================
// ===================================================================


/**
* Returns a contact node for a newly joining node. The node returned
* depends on the configuration ()
* @param size when selecting the contact, the nodes with index 0,...,size-1
* are considered.
* @return null if no contact could be found, otherwise a contact node.
*/
private Node getContact(int size) {
	
	if( size <= 0 ) return null;
	return OverlayNetwork.get(CommonRandom.r.nextInt(size));
}


// ==================== initialization ==============================
//===================================================================


public Subscribe(String prefix) {

	protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
}


// ===================== public methods ==============================
// ===================================================================


/**
* adds connections according to the SCAMP protocol. Builds network
* adding nodes 0,1,... one by one according to the SCAMP protocol.
*/
public void initialize() {

	Node contact = null;
	
	for(int i=1; i<OverlayNetwork.size(); ++i)
	{
		contact = getContact(i);
		if( contact == null )
		{
			throw new IllegalStateException("No contact found");
		}

		Scamp.subscribe( contact, OverlayNetwork.get(i), protocolID );
	}
}

// -------------------------------------------------------------------

/**
* adds given nodes according to the SCAMP protocol one by one.
*/
public void initialize(Node n) {
	
	Node contact = getContact(OverlayNetwork.size());
	if( contact == null )
	{
		throw new IllegalStateException("No contact found");
	}

	Scamp.subscribe( contact, n, protocolID );
}

}


