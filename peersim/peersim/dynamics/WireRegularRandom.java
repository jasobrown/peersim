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

import peersim.graph.*;
import peersim.core.*;
import peersim.config.Configuration;

/**
 * Takes a {@link Linkable} protocol and adds random connections. Note that no
 * connections are removed, they are only added. So it can be used in
 * combination with other initializers.
 */
public class WireRegularRandom implements Control, NodeInitializer
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * The out-degree degree of the graph.
 * @config
 */
private static final String PAR_DEGREE = "degree";

/**
 * If set, the generated graph is undirected. In other words, for each link
 * (i,j) a link (j,i) will also be added. Defaults to false.
 * @config
 */
private static final String PAR_UNDIR = "undirected";

/**
 * If this config property is defined, method {@link Linkable#pack()} is 
 * invoked on the specified protocol at the end of the wiring phase. 
 * Default to false.
 * @config
 */
private static final String PAR_PACK = "pack";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/**
 * The protocol we want to wire
 */
private final int pid;

/**
 * The degree of the regular graph
 */
private final int degree;

/**
 * If true, method pack() is invoked on the initialized protocol
 */
private final boolean pack;

/**
 * Value obtained from parameter {@link #PAR_UNDIR}.
 */
private final boolean undirected;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters. Invoked by the
 * simulation engine.
 * @param prefix
 *          the configuration prefix for this class
 */
public WireRegularRandom(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
	undirected = Configuration.contains(prefix + "." + PAR_UNDIR);
	pack = Configuration.contains(prefix + "." + PAR_PACK);
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/** Calls {@link GraphFactory#wireRegularRandom}. */
public boolean execute()
{
	GraphFactory.wireRegularRandom(new OverlayGraph(pid, !undirected), degree,
			CommonState.r);
	if (pack) {
		int size = Network.size();
		for (int i = 0; i < size; i++) {
			Linkable link = (Linkable) Network.get(i).getProtocol(pid);
			link.pack();
		}
	}
	return false;
}

// -------------------------------------------------------------------

/**
 * Takes {@value #PAR_DEGREE} random samples with replacement from the nodes of
 * the overlay network.
 */
public void initialize(Node n)
{
	if (Network.size() == 0)
		return;
	for (int j = 0; j < degree; ++j) {
		((Linkable) n.getProtocol(pid)).addNeighbor(Network.get(CommonState.r
				.nextInt(Network.size())));
	}
	if (pack) {
		((Linkable) n.getProtocol(pid)).pack();
	}
}

//--------------------------------------------------------------------------

}
