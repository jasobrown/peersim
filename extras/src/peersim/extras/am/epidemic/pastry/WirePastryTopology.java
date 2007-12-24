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

package peersim.extras.am.epidemic.pastry;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * Extract a Chord structure from a ring.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class WirePastryTopology implements Control
{

/**
 * The identifier of the protocol where the Chord topology should be stored.
 */
private static final String PAR_PROT = "protocol";

/**
 * The identifier of the protocol where the Chord topology should be stored.
 */
private static final String PAR_HOLDER = "holder";

/** Identifier of the protocol to be wired */
private final int pid;

/** Identifier of the protocol holding the ids */
private final int hid;

/**
 * 
 */
public WirePastryTopology(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER);
}

public boolean execute()
{
	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		Pastry pastry = (Pastry) node.getProtocol(pid);
		for (int j = 0; j < size; j++) {
			pastry.addFinger(Network.get(j));
		}
	}
	return false;
}

/** Returns the distance over the ring */
private long dist(long a, long b)
{
	return (b - a + ID.SIZE) % ID.SIZE;
}

/** Utility method */
private long getID(Node node)
{
	return ((IDHolder) node.getProtocol(hid)).getID();
}
}
