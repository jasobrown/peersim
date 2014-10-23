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
public class WireIDRing implements Control
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
private final static String PAR_SUCCESSORS = "degree";

// ---------------------------------------------------------------------
// Configuration Variables
// ---------------------------------------------------------------------
/** IDHolder protocol identifier */
private final int pid;

/** Linkable protocol identifier */
private final int lid;

/** Number of leafs to be considered when routing */
private final int degree;

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
public WireIDRing(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	lid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
	degree = Configuration.getInt(prefix + "." + PAR_SUCCESSORS);
	idc = new IDNodeComparator(pid);
}

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
// Comment inherited from interface
public boolean execute()
{
	// Copy node array
	int size = Network.size();
	nodes = new Node[size];
	for (int i = 0; i < size; i++) {
		nodes[i] = Network.get(i);
	}
	Arrays.sort(nodes, idc);
	buildChordTopology();
	return false;
}

/**
 * Builds a chord topology.
 */
private void buildChordTopology()
{
	int size = nodes.length;
	for (int i = 0; i < size; i++) {
		Linkable link = (Linkable) nodes[i].getProtocol(lid);
		// Extract leafs
		for (int j = 1; j <= degree; j++) {
			link.addNeighbor(nodes[(i - j + nodes.length) % nodes.length]);
			// System.out.println(getID(i) + " > " +
			// getID((i-j+nodes.length)%nodes.length));
		}
		for (int j = 1; j <= degree; j++) {
			link.addNeighbor(nodes[(i + j) % nodes.length]);
			// System.out.println(getID(i) + " < " + getID((i+j)%nodes.length));
		}
		link.pack();
	}
}

}