/*
 * Copyright (c) 2003-2005 The BISON Project
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

package peersim.extras.am.epidemic;

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class CDEpidemicManager implements CDProtocol
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------
/**
 * The identifier of the protocol that implements the Topology interface.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * This parameter describes the percentage of messages that can
 * be lost 
 * @config
 */
private static final String PAR_LOSS = "loss";


// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------
/** Protocol identifier */
private final int[] pid;

/** Loss probability */
private final float loss;

/** True if loss probability is zero */
private final boolean reliable;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

/**
 * Construct a new topology manager instance.
 */
public CDEpidemicManager(String prefix)
{
	// Read protocols
	String protString = Configuration.getString(prefix + "." + PAR_PROT);
	String[] protocols = protString.split(",");
	pid = new int[protocols.length];
	for (int i=0; i < protocols.length; i++) {
		pid[i] = Configuration.lookupPid(protocols[i]);
	}
	
	// Read other parameters
	loss = (float) Configuration.getDouble(prefix + "." + PAR_LOSS, 0);
	reliable = (loss == 0.0);
}

/**
 * For each protocol, there is a single instance of this class. So, clone
 * returns a reference to this single instance.
 */
public Object clone()
{
	return this;
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------
/**
 * @inheritDoc
 * @param protocolID ignored; because it refers to instances of this 
 * protocol, while we are interested in the protocols that actually 
 * do the work.
 */
public void nextCycle(Node lnode, int protocolID)
{
	for (int i=0; i < pid.length; i++) {
		// Select peer; exit if not found
		EpidemicProtocol lpeer = (EpidemicProtocol) lnode.getProtocol(pid[i]);
		Node rnode = lpeer.selectPeer(lnode);
		if (rnode == null ) 
			continue;
		EpidemicProtocol rpeer = (EpidemicProtocol) rnode.getProtocol(pid[i]);
	
		// Prepare request (in any case, even if the dest is crashed or the
		// message is lost)
		Message request = lpeer.prepareRequest(lnode, rnode);
	  if (request == null)
			continue;
	
		// If node is crashed, stop
		if (!rnode.isUp())
			continue;
		
		// If message is lost, stop
		if (!reliable && CommonState.r.nextFloat() < loss) 
			continue;
	
		// Prepare reply (in any case, even if the dest is crashed or the
		// message is lost)
		Message response = rpeer.prepareResponse(rnode, lnode, request);

		// Merge the request to the remote node
		rpeer.merge(rnode, lnode, request);
		
		if (response == null)
			continue;
	
		// If message is lost, stop
		if (!reliable && CommonState.r.nextFloat() < loss)
			continue;
		
		// Merge the response to the local node
		lpeer.merge(lnode, rnode, response);
	}
}
// ---------------------------------------------------------------------
}
