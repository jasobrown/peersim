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
import peersim.graph.*;

/**
* This class contains the implementation of the Barabasi-Albert model
* of growing scale free networks. The original model is described in
* <a href="http://arxiv.org/abs/cond-mat/0106096">http://arxiv.org/abs/cond-mat/0106096</a>. It also contains the option of building
* a directed network, in which case the model is a variation of the BA model
* described in <a href="http://arxiv.org/pdf/cond-mat/0408391">
http://arxiv.org/pdf/cond-mat/0408391</a>. In both cases, the number of the
* initial set of nodes is the same as the degree parameter, and no links are
* added. The first added node is connected to all of the initial nodes,
* and after that the BA model is used normally.
*/
public class WireScaleFreeBA implements Dynamics {


// ================ constants ============================================
// =======================================================================


/** 
 *  String name of the parameter used to select the protocol to operate on
 */
public static final String PAR_PROT = "protocol";

/** 
 * This config property represents the number of edges added to each new
 * node (apart from those forming the initial network).
 */
public static final String PAR_DEGREE = "degree";

/**
 * If this parameter is defined, method pack() is invoked on the specified
 * protocol at the end of the wiring phase. Default to false.
 */
public static final String PAR_PACK = "pack";

/** 
*  String name of the parameter to set if the graph should be undirected,
* that is, for each link (i,j) a link (j,i) will also be added.
*/
public static final String PAR_UNDIR = "undirected";


// =================== fields ============================================
// =======================================================================


/** Protocol id */
private final int pid;

/** Number of nodes */
private final int nodes;

/** Average number of edges to be created */
private int degree;

/** If true, method pack() is invoked on the initialized protocol */
private final boolean pack;

private final boolean undirected;


// ===================== initialization ==================================
// =======================================================================


public WireScaleFreeBA(String prefix)
{
	/* Read parameters */
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	nodes = Network.size();
	degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
	pack = Configuration.contains(prefix+"."+PAR_PACK);
	undirected = Configuration.contains(prefix+"."+PAR_UNDIR);
}


// ======================== methods =======================================
// ========================================================================


/** calls {@link GraphFactory#wireScaleFreeBA}.*/
public void modify() {
	
	GraphFactory.wireScaleFreeBA(
		new OverlayGraph(pid,!undirected), 
		degree,
		CommonRandom.r );
		
	if (pack) {
		int size = Network.size();
		for (int i=0; i < size; i++) {
			Linkable link=(Linkable)Network.get(i).getProtocol(pid);
			link.pack();
		}
	}
}

}

