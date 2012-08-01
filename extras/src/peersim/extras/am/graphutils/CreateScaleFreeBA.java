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

package peersim.extras.am.graphutils;

import java.io.*;
import java.util.*;

import peersim.config.*;
import peersim.extras.am.util.*;
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
* @see GraphFactory#wireScaleFreeBA
*/
public class CreateScaleFreeBA {


// ================ constants ============================================
// =======================================================================

/**
 * The number of nodes to be added.
 * @config
 */
private static final String PAR_SIZE = "size";


/**
 * The number of edges added to each new node (apart from those forming the 
 * initial network).
 * @config
 */
private static final String PAR_DEGREE = "k";

/**
 * File to be written
 * @config
 */
private static final String PAR_FILENAME = "filename";


// ===================== initialization ==================================
// =======================================================================

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
*/
public static void main(String[] args)
throws Exception
{
	System.err.println("Loading configuration");
	Configuration.setConfig( new ParsedProperties(args) );
	
	
	int nodes = Configuration.getInt(PAR_SIZE);
	int k = Configuration.getInt(PAR_DEGREE);
	String filename = Configuration.getString(PAR_FILENAME);
	
	Random r = new Random();

	if( nodes <= k ) return;

	IntGraph g = new IntGraph(nodes);
	
	// edge i has ends (ends[2*i],ends[2*i+1])
	int[] ends = new int[2*k*(nodes-k)];
	
	// Add initial edges from k to 0,1,...,k-1
	for(int i=0; i < k; i++)
	{
		g.addEdge(k,i);;
		g.addEdge(i,k);
		ends[2*i]=k;
		ends[2*i+1]=i;
	}
	
	int len = 2*k; // edges drawn so far is len/2
	for(int i=k+1; i < nodes; i++) // over the remaining nodes
	{
		for (int j=0; j < k; j++) // over the new edges
		{
			int target;
			do
			{
				target = ends[r.nextInt(len)]; 
				int m=0;
				while( m<j && ends[len+2*m+1]!=target) ++m;
				if(m==j) break;
				// we don't check in the graph because
				// this wire method should accept graphs
				// that already have edges.
			}
			while(true);
			g.addEdge(i,target);
			g.addEdge(target,i);
			ends[len+2*j]=i;
			ends[len+2*j+1]=target;
		}
		len += 2*k;
	}

	DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

	out.writeInt(nodes);
	for (int i=0; i < nodes; i++) {
		out.writeInt(i);
		int size = g.neighbors(i).size();
		out.writeInt(size);
		for (int j=0; j < size; j++) {
			out.writeInt(g.neighbors(i).get(j));
		}
	}
	out.close();


}


}

