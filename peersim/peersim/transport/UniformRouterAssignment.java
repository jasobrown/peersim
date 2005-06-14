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

package peersim.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;
import peersim.util.*;


/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class UniformRouterAssignment implements Dynamics
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** 
 * Parameter name used to configure the protocol that should be initialized 
 */
private static final String PAR_PROTOCOL = "protocol"; 
	
//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

/** Protocol identifier */
private int pid;	
	

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads parameters.
 */
public UniformRouterAssignment(String prefix)
{
	pid = Configuration.getPid(prefix+"."+PAR_PROTOCOL);
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

// Comment inherited from interface
public void modify()
{
	int nsize = Network.size();
	int nrouters = E2ENetwork.getSize();
	for (int i=0; i < nsize; i++) {
		Node node = Network.get(i);
		RouterInfo t = (RouterInfo) node.getProtocol(pid);
		int r = CommonRandom.r.nextInt(nrouters);
		t.setRouter(r);
	}
}

}
