/*
 * Copyright (c) 2003 The BISON Project
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
import peersim.util.*;

/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class WireScaleFreeBA
implements Dynamics
{

////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////

/** 
 *  String name of the parameter used to select the protocol to operate on
 */
public static final String PAR_PROT = "protocol";

/** 
 * This config property represents the number of edges added to each new
 * node (apart from those forming the initial network).
 */
public static final String PAR_EDGES = "degree";


////////////////////////////////////////////////////////////////////////////
// Fields
////////////////////////////////////////////////////////////////////////////

/** Protocol id */
int pid;

/** Number of nodes */
int nodes;

/** Average number of edges to be created */
int edges;


////////////////////////////////////////////////////////////////////////////
// Constructor
////////////////////////////////////////////////////////////////////////////

public WireScaleFreeBA(String prefix)
{
	/* Read parameters */
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	nodes = Network.size();
	edges = Configuration.getInt(prefix + "." + PAR_EDGES);
}


////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////

/** to save typing */
private boolean addNeighbor(Node n, int pid, Node n2) {

	return ((Linkable)n.getProtocol(pid)).addNeighbor(n2);
}

// -------------------------------------------------------------------------

// Comment inherited from interface
public void modify() 
{
	Node[] dest = new Node[2*edges*nodes];
	
	// Add initial edges; 
	Node ne = Network.get(edges);
	for (int i=0; i < edges; i++) {
		Node ni = Network.get(i);
		dest[i*2] = ne;
		dest[i*2+1] = ni;
		addNeighbor(ni, pid, ne);
		addNeighbor(ne, pid, ni);
	}
	
	int len=edges*2;
	for (int i=edges+1; i < nodes; i++) {
		Node ni = Network.get(i);
		for (int j=0; j < edges; j++) {
			boolean stop;
			Node nk;
			do {
				nk = dest[CommonRandom.r.nextInt(len)]; 
				stop = addNeighbor(ni, pid, nk) &&
					addNeighbor(nk, pid, ni);
			} while (!stop);
			dest[len++] = ni;
			dest[len++] = nk;
		}
	}
}

}
