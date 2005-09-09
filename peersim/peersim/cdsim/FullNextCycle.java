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
		
package peersim.cdsim;

import peersim.config.*;
import peersim.core.*;

/**
* Control to run a cycle of the cycle drive simulation.
* This does not need to be explicitly configured (although you can do it for
* hacking purposes).
*/
public class FullNextCycle implements Control {


// ============== fields ===============================================
// =====================================================================


/**
* The type of the getPair function. This parameter is of historic interest and
* was needed in a publication we wrote. You don't need to care about this.
* But if you wanna know: if set to "rand", then in a cycle the simulator
* does not simply iterate through the nodes, but instead picks a random one
* N times, where N is the network size.
* @config
*/
private static final String PAR_GETPAIR = "getpair";

// --------------------------------------------------------------------

protected final boolean getpair_rand;

/** Holds the protocol schedulers of this simulation */
protected Scheduler[] protSchedules = null;


// =============== initialization ======================================
// =====================================================================

/**
* Reads config parameters and {@link Scheduler}s.
*/
public FullNextCycle(String prefix) {
	
	getpair_rand = Configuration.contains(prefix+"."+PAR_GETPAIR);

	// load protocol schedulers
	String[] names = Configuration.getNames(Node.PAR_PROT);
	protSchedules = new Scheduler[names.length];
	for(int i=0; i<names.length; ++i)
	{
		protSchedules[i] = new Scheduler(names[i]);
	}
}

// =============== methods =============================================
// =====================================================================

/** 
 * Execute all the {@link CDProtocol}s on all nodes that are up.
 * If the node goes down as a result of the execution of a protocol, then
 * the rest of the protocols on that node are not exectuted and we move on
 * to the next node.
 * It sets the {@link CDState}
 * appropriately.
 * @return always false
 */
public boolean execute() {

	int cycle=CDState.getCycle();
	for(int j=0; j<Network.size(); ++j)
	{
		Node node = null;
		if( getpair_rand )
			node = Network.get(
			   CDState.r.nextInt(Network.size()));
		else
			node = Network.get(j);
		if( !node.isUp() ) continue; 
		int len = node.protocolSize();
		CDState.setNode(node);
		CDState.setCycleT(j);
		for(int k=0; k<len; ++k)
		{
			// Check if the protocol should be executed, given the
			// associated scheduler.
			if (!protSchedules[k].active(cycle))
				continue;
				
			CDState.setPid(k);
			Protocol protocol = node.getProtocol(k);
			if( protocol instanceof CDProtocol )
			{
				((CDProtocol)protocol).nextCycle(node, k);
				if( !node.isUp() ) break;
			}
		}
	}

	return false;
}

}


