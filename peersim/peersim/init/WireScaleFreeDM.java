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

package peersim.init;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;

/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class WireScaleFreeDM
implements Initializer
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
	public static final String PAR_EDGES = "edges";


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

  public WireScaleFreeDM(String prefix)
  {
		/* Read parameters */
		pid = Configuration.getInt(prefix + "." + PAR_PROT);
		nodes = Network.size();
		edges = Configuration.getInt(prefix + "." + PAR_EDGES);
  }


	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////
	
	// Comment inherited from interface
	public void initialize() 
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
					ni.addNeighbor(pid, nj);
					nj.addNeighbor(pid, ni);
					links[len*2] = ni;
					links[len*2+1] = nj;
					len++;
				}
			}
		}

    
		for (int i=clique; i < nodes; i++) {
			Node ni = Network.get(i);
			for (int j=0; j < edges; j++) {
				int edge = CommonRandom.r.nextInt(len);
				Node nk = links[edge*2];
				Node nj = links[edge*2+1];
			  ni.addNeighbor(pid, nk);
			  nk.addNeighbor(pid, ni);
				nj.addNeighbor(pid, nk);
				nk.addNeighbor(pid, nj);
				links[len*2] = ni;
				links[len*2+1] = nk;
				len++;
				links[len*2] = nj;
				links[len*2+1] = nk;
				len++;
			}
		}
	}
		
}
