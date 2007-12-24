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

package peersim.extras.am.epidemic.chord;

import java.util.*;
import java.util.Arrays;
import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;
import cern.colt.*;

/**
 * Extract a Chord structure from a ring.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class WireChordTopology implements Control
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

/** Nodes */
private Node[] nodes;

/** Node comparator */
private final Comparator idc;

/**
 * 
 */
public WireChordTopology(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER);
	idc = new IDNodeComparator(hid);
}

public boolean execute()
{
	int size = Network.size();
	if (nodes == null || nodes.length != size) {
		nodes = new Node[size];
	}
	for (int i = 0; i < size; i++) {
		nodes[i] = Network.get(i);
	}
	Arrays.sort(nodes, 0, nodes.length, idc);
	for (int i = 0; i < size; i++) {
		long lid = IDUtil.getID(nodes[i], hid);
		Chord prot = (Chord) nodes[i].getProtocol(pid);
		// Reset finger table
		for (int j = 0; j < ID.BITS; j++) {
			prot.setFinger(j, null);
		}
		// Extract fingers
		for (int j = 0; j < ID.BITS; j++) {
			long key = (lid + (1L << j)) % ID.SIZE;
			((IDHolder) Network.prototype.getProtocol(hid)).setID(key);
			int pos = Sorting.binarySearchFromTo(nodes, Network.prototype, 0,
					size - 1, idc);
			if (pos < 0) {
				pos = -(pos + 1);
				if (pos == size)
					pos = 0;
			}
			long rid = getID(nodes[pos]);
			long dist = dist(lid, rid);
			if (j == ID.log2(dist)) {
				prot.setFinger(j, nodes[pos]);
			}
		}
		// Extract leafs
		int degree = prot.successors();
		for (int j = 0; j < degree; j++) {
			prot.setSuccessor(j, nodes[(i + j + 1) % nodes.length]);
			// System.out.println(lid + " --> " + IDUtil.getID(prot.getSuccessor(j),
			// hid));
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
