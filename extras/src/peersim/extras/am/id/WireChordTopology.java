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

package peersim.extras.am.id;

import java.util.*;
import peersim.config.*;
import peersim.core.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class WireChordTopology implements Control
{

// ---------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------
/**
 * String name of the parameter identifying the protocol implementing the
 * IDHolder interface.
 */
private final static String PAR_PROTOCOL = "idholder";

/**
 * String name of the parameter identifying the protocol implementing the
 * Linkable interface.
 */
private final static String PAR_LINKABLE = "linkable";

/**
 * String name of the parameter used to specify the number of successors to be
 * considered.
 */
private final static String PAR_SUCCESSORS = "successors";

/**
 * If present, no fingers will be added.
 */
private final static String PAR_NOFINGERS = "nofingers";

// ---------------------------------------------------------------------
// Configuration Variables
// ---------------------------------------------------------------------
/** IDHolder protocol identifier */
private final int pid;

/** Linkable protocol identifier */
private final int lid;

/** Number of leafs to be considered when routing */
private final int successors;

private final boolean nofingers;

// ---------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------
/** Freezed set of nodes */
private Node[] nodes;

/** Comparator for node ordering */
private Comparator idc;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * 
 */
public WireChordTopology(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	lid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
	successors = Configuration.getInt(prefix + "." + PAR_SUCCESSORS);
	nofingers = Configuration.contains(prefix + "." + PAR_NOFINGERS);
	idc = new IDNodeComparator(pid);
	// Copy node array
	int size = Network.size();
	nodes = new Node[size];
	for (int i = 0; i < size; i++) {
		nodes[i] = Network.get(i);
	}
}

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
// Comment inherited from interface
public boolean execute()
{
	buildChordTopology();
	return false;
}

/**
 * Builds a chord topology.
 */
private void buildChordTopology()
{
	Arrays.sort(nodes, idc);

	int size = nodes.length;
	int[] array = new int[ID.BITS];
	for (int i = 0; i < size; i++) {
		Linkable link = (Linkable) nodes[i].getProtocol(lid);
		long lid = getID(i);
		// Extract fingers
		if (!nofingers) {
			for (int j = 0; j < ID.BITS; j++) {
				long key = (lid + (1L << j)) % ID.SIZE;
				((IDHolder) Network.prototype.getProtocol(pid)).setID(key);
				int pos = Arrays.binarySearch(nodes, Network.prototype, idc);
				if (pos < 0) {
					pos = -(pos + 1);
					if (pos == size)
						pos = 0;
				}
				long rid = getID(pos);
				long dist = dist(lid, rid);
				//System.out.println(lid + " " + rid + " " + dist + " " + ID.log2(dist) + " " + j);
				if (j == ID.log2(dist)) {
					array[j]++;
					link.addNeighbor(nodes[pos]);
				}
			}
		}
		// Extract leafs
		for (int j = 1; j <= successors; j++) {
			link.addNeighbor(nodes[(i + j) % nodes.length]);
		}
	}
}

private long dist(long a, long b)
{
	return (b - a + ID.SIZE) % ID.SIZE;
}

private long getID(int pos)
{
	return ((IDHolder) nodes[pos].getProtocol(pid)).getID();
}
}
