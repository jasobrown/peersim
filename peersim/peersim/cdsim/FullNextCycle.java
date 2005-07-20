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
		
package peersim.cdsim;

import peersim.config.*;
import peersim.core.*;

/**
*/
public class FullNextCycle implements Control {


// ============== fields ===============================================
// =====================================================================


/**
* The type of the getPair function.
* @config
*/
private static final String PAR_GETPAIR = "getpair";

// --------------------------------------------------------------------

protected boolean getpair_rand;

/** Holds the protocol schedulers of this simulation */
protected Scheduler[] protSchedules = null;


// =============== initialization ======================================
// =====================================================================


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
 * Execute all the protocols on all nodes. It sets the {@link CDState}
 * appropriately.
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


