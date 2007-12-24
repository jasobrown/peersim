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

package peersim.extras.am.epidemic.ring;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class BarsObserver implements Control
{

// ---------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------
/**
 * String name of the parameter identifying the protocol to be observed
 */
private final static String PAR_PROTOCOL = "protocol";

// ---------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------
/** Protocol identifier */
private final int pid;

/** Observer configuration prefix */
private final String prefix;

/** Node to be observed */
private static Node node;

/** Event counter */
private static int counter;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * 
 */
public BarsObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	counter = 0;
}

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
// Comment inherited from interface
public boolean execute()
{
	int[] count = new int[ID.BITS];
	if (node == null)
		node = Network.get(0);
	Linkable link = (Linkable) node.getProtocol(pid);
	long lid = ((IDHolder) node.getProtocol(pid)).getID();
	int degree = link.degree();
	for (int i = 0; i < degree; i++) {
		Node peer = link.getNeighbor(i);
		long id = ((IDHolder) peer.getProtocol(pid)).getID();
		long diff = ID.dist(id, lid);
		count[64 - Long.numberOfLeadingZeros(diff)]++;
		System.out.println(prefix + ".bars: " + counter + " " + diff + " 50 0 100 " + " "
				+ CommonState.getTime());
	}
	for (int i = 0; i < ID.BITS; i++) {
		System.out.println(prefix + ".count: " + counter + " " + i + " " + count[i] + " "
				+ CommonState.getTime());
	}
	counter++;
	return false;
}
}
