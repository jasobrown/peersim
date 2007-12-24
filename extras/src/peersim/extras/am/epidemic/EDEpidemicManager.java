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

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.*;

public class EDEpidemicManager implements EDProtocol
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------

/**
 * The identifier of the protocols that implements the EpidemicProtocol
 * interface. If absent, it assumes that this protocol has been extended
 * and implements the EpidemicProtocol interface.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * The identifier of the protocol that implements the Transport interface.
 * @config
 */
private static final String PAR_TRANSPORT = "transport";

/**
 * The length of a cycle
 */
private static final String PAR_PERIOD = "period";

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------

protected class CommonData
{

/** Identifiers of the protocols to be managed */
private int[] pids;

/** Transport protocol identifier */
private int tid;

/** Length of a cycle in the protocol */
private int period;

}

/** Data common to a protocol vector */
protected CommonData c;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

/**
 * Construct a new topology manager instance.
 */
public EDEpidemicManager(String prefix)
{
	c = new CommonData();
	// Read protocols
	String protString = Configuration.getString(prefix + "." + PAR_PROT, null);
	if (protString == null) {
		c.pids = new int[1];
		c.pids[0] = CommonState.getPid();
	} else {
		String[] protocols = protString.split(",");
		c.pids = new int[protocols.length];
		for (int i = 0; i < protocols.length; i++) {
			c.pids[i] = Configuration.lookupPid(protocols[i]);
		}
	}

	// Read other parameters
	c.tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
	c.period = Configuration.getInt(prefix + "." + PAR_PERIOD);
}

/**
 * Copy the content of this object to the final object
 */
protected EDEpidemicManager(EDEpidemicManager e)
{
	this.c = e.c;
	// Set the first alarm
	int r = CommonState.r.nextInt(c.period);
	for (int i = 0; i < c.pids.length; i++) {
		EDSimulator.add(r, c.pids[i], CommonState.getNode(), CommonState.getPid());
	}
}

/**
 * For each protocol, there is a single instance of this class. So, clone
 * returns a reference to this single instance.
 */
public Object clone()
{
	if (c.pids.length == 1 && c.pids[0] == CommonState.getPid()) {
		return new EDEpidemicManager(this);
	} else {
		// Set the first alarm
		int r = CommonState.r.nextInt(c.period);
		for (int i = 0; i < c.pids.length; i++) {
			EDSimulator
					.add(r, c.pids[i], CommonState.getNode(), CommonState.getPid());
		}
		return this;
	}
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------

public void processEvent(Node lnode, int thisPid, Object event)
{
	if (event instanceof Integer) {
		activeThread(lnode, (Integer) event, thisPid);
	} else {
		passiveThread(lnode, (Message) event, thisPid);
	}
}

private void activeThread(Node lnode, Integer pid, int thisPid)
{
	EDSimulator.add(c.period, pid, lnode, thisPid);
	EpidemicProtocol lpeer = (EpidemicProtocol) lnode.getProtocol(pid);
	Node rnode = lpeer.selectPeer(lnode);
	if (rnode == null)
		return;
	Message request = lpeer.prepareRequest(lnode, rnode);
	if (request != null) {
		request.setPid(pid);
		request.setRequest(true);
		request.setSender(lnode);
		Transport tr = (Transport) lnode.getProtocol(c.tid);
		tr.send(lnode, rnode, request, thisPid);
	}
}

private void passiveThread(Node lnode, Message msg, int thisPid)
{
	int pid = msg.getPid();
	EpidemicProtocol lpeer = (EpidemicProtocol) lnode.getProtocol(pid);
	if (msg.isRequest()) {
		Message reply = lpeer.prepareResponse(lnode, msg.getSender(), msg);
		if (reply != null) {
			reply.setPid(pid);
			reply.setRequest(false);
			reply.setSender(lnode);
			Transport tr = (Transport) lnode.getProtocol(c.tid);
			tr.send(lnode, msg.getSender(), reply, thisPid);
		}
	}
	lpeer.merge(lnode, msg.getSender(), msg);
}

}
