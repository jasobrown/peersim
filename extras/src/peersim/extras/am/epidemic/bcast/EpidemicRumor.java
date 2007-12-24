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

package peersim.extras.am.epidemic.bcast;

import peersim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.epidemic.*;


public class EpidemicRumor implements EpidemicProtocol, Infectable
{

//---------------------------------------------------------------------
//Constants for messages
//---------------------------------------------------------------------

private static final InfectionMessage TRUE = new InfectionMessage(true);
private static final InfectionMessage FALSE = new InfectionMessage(false);

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/**
 * @config
 */
private static final String PAR_LINKABLE = "linkable";

/**
 * @config
 */
private static final String PAR_PUSHPULL = "pushpull";

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

private final int lid;

private final boolean pushpull;

private boolean status;


//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

public EpidemicRumor(String prefix)
{
	lid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
	pushpull = Configuration.contains(prefix + "." + PAR_PUSHPULL);
	status = false;
}


public Object clone()
{
	EpidemicRumor ret = null;
	try {
		ret = (EpidemicRumor) super.clone();
	} catch (CloneNotSupportedException e) {
	}
	ret.status = false;
	return ret;
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

public Node selectPeer(Node lnode)
{
	Linkable linkable = (Linkable) lnode.getProtocol(lid);
	int r = CommonState.r.nextInt(linkable.degree());
	return linkable.getNeighbor(r);
}

public Message prepareRequest(Node lnode, Node rnode)
{
	if (status && !pushpull)
		return null;
	
	if (Simulator.getSimID() == Simulator.CDSIM) {
		return (status ? TRUE : FALSE);
	} else if (Simulator.getSimID() == Simulator.EDSIM) {
		return new InfectionMessage(status);
	} else {
		throw new IllegalStateException("Unknow simulator");
	}
}

public Message prepareResponse(Node lnode, Node rnode, Message request)
{
	InfectionMessage msg = (InfectionMessage) request;
	
	if (Simulator.getSimID() == Simulator.CDSIM) {
		return (status ? TRUE : FALSE);
	} else if (Simulator.getSimID() == Simulator.EDSIM) {
		return new InfectionMessage(status);
	} else {
		throw new IllegalStateException("Unknow simulator");
	}
}

public void merge(Node lnode, Node rnode, Message message)
{
	InfectionMessage msg = (InfectionMessage) message;
	status = status || msg.getStatus();
}


public void setInfected(boolean infected)
{
	this.status = infected;
}


public boolean isInfected()
{
	return status;
}

}