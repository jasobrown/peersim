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

package example.loadbalance;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.*;
import peersim.vector.SingleValueHolder;
import peersim.cdsim.CDProtocol;

public class BasicBalance extends SingleValueHolder implements CDProtocol
{

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * Initial quota. Defaults to 1.
 * @config
 */
private static final String PAR_QUOTA = "quota";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

private final double quota_value; // original quota value taken from

protected double quota; // current cycle quota

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------
public BasicBalance(String prefix)
{
	super(prefix);
	// get quota value from the config file. Default 1.
	quota_value = (Configuration.getInt(prefix + "." + PAR_QUOTA, 1));
	quota = quota_value;
}

// clone is inherited

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

// Resets the quota.
protected void resetQuota()
{
	this.quota = quota_value;
}

// Implements CDProtocol interface
public void nextCycle(Node node, int protocolID)
{
	int linkableID = FastConfig.getLinkable(protocolID);
	Linkable linkable = (Linkable) node.getProtocol(linkableID);
	if (this.quota == 0) {
		return; // skip this node
	}
	// this takes the most distant neighbor based on local load
	BasicBalance neighbor = null;
	double maxdiff = 0;
	for (int i = 0; i < linkable.degree(); ++i) {
		Node peer = linkable.getNeighbor(i);
		// The selected peer could be inactive
		if (!peer.isUp())
			continue;
		BasicBalance n = (BasicBalance) peer.getProtocol(protocolID);
		if (n.quota != 1.0)
			continue;
		double d = Math.abs(value - n.value);
		if (d > maxdiff) {
			neighbor = n;
			maxdiff = d;
		}
	}
	if (neighbor == null) {
		return;
	}
	doTransfer(neighbor);
}

/**
 * Performs the actual load exchange selecting to make a PUSH or PULL
 * approach. It affects the involved nodes quota.
 */
protected void doTransfer(BasicBalance neighbor)
{
	double a1 = this.value;
	double a2 = neighbor.value;
	double maxTrans = Math.abs((a1 - a2) / 2);
	double trans = Math.min(maxTrans, quota);
	trans = Math.min(trans, neighbor.quota);
	if (a1 <= a2) // PULL
	{
		a1 += trans;
		a2 -= trans;
	} else // PUSH
	{
		a1 -= trans;
		a2 += trans;
	}
	this.value = a1;
	this.quota -= trans;
	neighbor.value = a2;
	neighbor.quota -= trans;
}

}
