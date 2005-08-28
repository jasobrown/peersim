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


/**
 * This transport protocol can be combined with other transports
 * to simulate message losses. Its behavior is the following: each message
 * can be dropped based on the configured probability, or it will be sent
 * using the support transport protocol. 
 * <p>
 * The memory requirements are minimal, as a single instance is created and 
 * inserted in the protocol array of all nodes. 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class UnreliableTransport implements Transport
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** 
 * String name of the parameter used to configure the protocol identifier
 * for the support transport protocol.
 */
private static String PAR_TRANSPORT = "transport";
	
/** 
 * String name of the parameter used to configure the probability that a 
 * message sent through this transport is lost.
 */
private static String PAR_DROP = "drop";


//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Protocol identifier for the support transport protocol */
private int pid;

/** Probability of dropping messages */
private float loss;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameter.
 */
public UnreliableTransport(String prefix)
{
	pid = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
	loss = (float) Configuration.getDouble(prefix+"."+PAR_DROP);
}

//---------------------------------------------------------------------

/**
 * Cloning method. This implementation does not require instance data,
 * so we can use a singleton instance for all the nodes.
 */
public Object clone()
{
	return this;
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

// Comment inherited from interface
public void send(Node src, Node dest, Object msg, int pid)
{
	if (CommonState.r.nextFloat() >= loss) {
		// Message is not lost
		Transport t = (Transport) src.getProtocol(pid);
		t.send(src, dest, msg, pid);
	}
}

//Comment inherited from interface
public long getLatency(Node src, Node dest)
{
	Transport t = (Transport) src.getProtocol(pid);
	return t.getLatency(src, dest);
}

}
