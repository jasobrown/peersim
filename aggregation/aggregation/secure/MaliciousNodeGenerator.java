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

package aggregation.secure;

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;
import peersim.util.*;

/**
 * This dynamics objects is used to transform nodes that behaves correctly 
 * into nodes that are malign. The number of nodes to be trasnformed and
 * the malign protocol to be used can be specified. 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MaliciousNodeGenerator 
implements Dynamics
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter that defines the protocol to initialize. 
 */
public static final String PAR_PROTID = "protocolID";

/** 
 * String name of the parameter that describes the malicious protocol
 * to be used.
 */
public static final String PAR_PROTOCOL = "protocol";

/** 
 * String name of the parameter used to determine the number
 * of malicious nodes in the network. 
 */
public static final String PAR_SIZE = "size";


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Protocol identifier */
private final int protocolID;

/** Number of malicious nodes */
private final int malicious;

/** The name of this object in the configuration file */
private final String name;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * Constructs a new malicious node generator by reading parameters
 * (protocol identifier and number of malicious instances).
 */
public MaliciousNodeGenerator(String name)
{
	this.name = name;
	protocolID = Configuration.getPid(name + "." + PAR_PROTID);
	malicious = Configuration.getInt(name + "." + PAR_SIZE);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public void initialize()
{
	modify();
}

// Comment inherited from interface
public void modify()
{
	/* Create a prototype */
	Protocol prototype = (Protocol) Configuration.getInstance(
		name + "." + PAR_PROTOCOL, 
		new Integer(protocolID)
	);
	if (!(prototype instanceof MaliciousProtocol))
	  throw new IllegalArgumentException(
		  "Parameter " + name + "." + PAR_PROTOCOL + ": Protocol " +
		  Configuration.getString(name + "." + PAR_PROTOCOL) +
	    " is not malicious");
	    
	/* Substitute benign protocol instances with malicious ones */
	try {
		for (int i=0; i < malicious; i++) {
			boolean stop = false;
			do {
				int k = CommonRandom.r.nextInt(Network.size());
				Node node = Network.get(k);
				Protocol prot = node.getProtocol(protocolID);
				if (!(prot instanceof MaliciousProtocol)) {
					node.setProtocol(protocolID, (Protocol) prototype.clone());
					stop = true;
				}
			} while (!stop);
		}
	} catch (CloneNotSupportedException e)
	{
		// this should never happen unless a node implementation
		// throws it explicitly (but why would it?)
		throw new Error(""+e);
	}
}

//--------------------------------------------------------------------------

}
