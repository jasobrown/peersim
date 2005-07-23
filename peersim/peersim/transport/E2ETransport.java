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

package peersim.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;


/**
 * This transport protocol is based on the E2ENetwork class. Each instance
 * of this class is assigned to one of the routers contained in E2ENetwork,
 * and that class is used to obtain the latency for messages sending.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class E2ETransport implements Transport, RouterInfo
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/**
 * The latency delay for local connections. Defaults to 0.
 * @config
 */
private static final String PAR_LOCAL = "local";
	
//---------------------------------------------------------------------
//Static fields
//---------------------------------------------------------------------

/** Identifier of this transport protocol */
private static int tid;
	
/** Latency of local connection between nodes */
private static int local;

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Identifier of the internal node */
private int router = -1;
	
//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameters. Actual initialization (i.e.,
 * router assignment) is delegated to initializers.
 */
public E2ETransport(String prefix)
{
	tid = CommonState.getPid();
	local = Configuration.getInt(prefix + "." + PAR_LOCAL, 0);
}

//---------------------------------------------------------------------

/**
 * Clones the object. No actual initialization is performed.
 */
public Object clone() throws CloneNotSupportedException
{
	return super.clone();
}

//---------------------------------------------------------------------
//Methods inherited by Transport
//---------------------------------------------------------------------

// Comment inherited from interface
public void send(Node src, Node dest, Object msg, int pid)
{
	/* Assuming that the sender corresponds to the source node */
	E2ETransport sender = (E2ETransport) src.getProtocol(tid);
	E2ETransport receiver = (E2ETransport) dest.getProtocol(tid);
	int latency =
	   E2ENetwork.getLatency(sender.router, receiver.router) + local*2;
	EDSimulator.add(latency, msg, dest, pid);
}

//---------------------------------------------------------------------

//Comment inherited from interface
public int getLatency(Node src, Node dest)
{
	/* Assuming that the sender corresponds to the source node */
	E2ETransport sender = (E2ETransport) src.getProtocol(tid);
	E2ETransport receiver = (E2ETransport) dest.getProtocol(tid);
	return E2ENetwork.getLatency(sender.router, receiver.router) + local*2;
}


//---------------------------------------------------------------------
//Methods inherited by RouterInfo
//---------------------------------------------------------------------

/**
 * Associates the node hosting this transport protocol instance with
 * a router in the router network.
 * 
 * @param router the numeric index of the router 
 */
public void setRouter(int router)
{
	this.router = router;
}

//---------------------------------------------------------------------

/**
 * @return the router associated to this transport protocol.
 */
public int getRouter()
{
	return router;
}

}
