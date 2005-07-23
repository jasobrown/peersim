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

package peersim.dynamics;

import peersim.config.*;
import peersim.core.*;

/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class WireScaleFreeDM
implements Control
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/** 
 * This config property represents the number of edges added to each new
 * node (apart from those forming the initial network).
 * @config
 */
private static final String PAR_EDGES = "edges";

/**
 * If this config property is defined, method {@link Linkable#pack()} is 
 * invoked on the specified protocol at the end of the wiring phase. 
 * Default to false.
 * @config
 */
private static final String PAR_PACK = "pack";


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** Protocol id */
private final int pid;

/** Number of nodes */
private final int nodes;

/** Average number of edges to be created */	
private final int edges;

/** If true, method pack() is invoked on the initialized protocol */
private final boolean pack;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

public WireScaleFreeDM(String prefix)
{
	/* Read parameters */
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	nodes = Network.size();
	edges = Configuration.getInt(prefix + "." + PAR_EDGES);
	pack = Configuration.contains(prefix+"."+PAR_PACK);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/** to save typing */
private boolean addNeighbor(Node n, int pid, Node n2) {

	return ((Linkable)n.getProtocol(pid)).addNeighbor(n2);
}

// -------------------------------------------------------------------------


// Comment inherited from interface
public boolean execute() 
{
	Node[] links = new Node[4*edges*nodes];

	// Initial number of nodes connected as a clique
	int clique = (edges > 3 ? edges : 3);

	// Add initial edges, to form a clique
	int len=0;
	for (int i=0; i < clique; i++) {
		Node ni = Network.get(i);
		for (int j=0; j < clique; j++) {
			if (i != j) {
				Node nj = Network.get(j);
				addNeighbor(ni, pid, nj);
				addNeighbor(nj, pid, ni);
				links[len*2] = ni;
				links[len*2+1] = nj;
				len++;
			}
		}
	}


	for (int i=clique; i < nodes; i++) {
		Node ni = Network.get(i);
		for (int j=0; j < edges; j++) {
			int edge = CommonState.r.nextInt(len);
			Node nk = links[edge*2];
			Node nj = links[edge*2+1];
			addNeighbor(ni, pid, nk);
			addNeighbor(nk, pid, ni);
			addNeighbor(nj, pid, nk);
			addNeighbor(nk, pid, nj);
			links[len*2] = ni;
			links[len*2+1] = nk;
			len++;
			links[len*2] = nj;
			links[len*2+1] = nk;
			len++;
		}
	}
	if (pack) {
		int size = Network.size();
		for (int i=0; i < size; i++) {
			Linkable link = (Linkable) Network.get(i).getProtocol(pid);
			link.pack();
		}
	}

	return false;
}
		
}
