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
		
package scamp;

import peersim.dynamics.*;
import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.CommonRandom;

/**
* Adds nodes according to the SCAMP protocol. No 
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
*/
public class Subscribe implements Dynamics, NodeInitializer {


// ========================= fields =================================
// ==================================================================


/** 
* String name of the parameter used to select the protocol to operate on.
* It has to be a scamp protocol.
*/
public static final String PAR_PROT = "protocol";

/** 
* If this parameter is set then all nodes are connected to the same
* center. If that center fails a new one is choosen antil that fails too.
* If not set, a random contact is selected. Not set by default.
*/
public static final String PAR_SINGLE = "singleContact";

/** The protocol we want to wire */
private final int protocolID;

/** true if all nodes connec to the same center */
private final boolean single;

/** the central node to connect to if single is true */
private Node cont = null;

// ===================== private methods =============================
// ===================================================================


/**
* Returns a contact node for a newly joining node. The node returned
* depends on the configuration.
* @param size when selecting the contact, the nodes with index 0,...,size-1
* are considered.
* @return null if no contact could be found, otherwise a contact node.
*/
private Node getContact(int size) {
	
	if( size <= 0 ) return null;
	if( single )
	{
		if( cont == null || !cont.isUp() )
			cont = Network.get(0);
		return cont;
	}
	else return Network.get(CommonRandom.r.nextInt(size));
}


// ==================== initialization ==============================
//===================================================================


public Subscribe(String prefix) {

	protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
	single = Configuration.contains(prefix+"."+PAR_SINGLE);
}


// ===================== public methods ==============================
// ===================================================================


/**
* adds connections according to the SCAMP protocol. Builds network
* adding nodes 0,1,... one by one according to the SCAMP protocol.
*/
public void modify() {

	Node contact = null;
	
	for(int i=1; i<Network.size(); ++i)
	{
		contact = getContact(i);
		if( contact == null )
		{
			throw new IllegalStateException("No contact found");
		}

		Scamp.subscribe( contact, Network.get(i), protocolID );
	}
}

// -------------------------------------------------------------------

/**
* adds given nodes according to the SCAMP protocol one by one.
*/
public void initialize(Node n) {

	if( Network.size() == 0 ) return;

	Node contact = getContact(Network.size());
	if( contact == null )
	{
		throw new IllegalStateException("No contact found");
	}

	Scamp.subscribe( contact, n, protocolID );
}

}


