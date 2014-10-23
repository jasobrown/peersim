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

/**
* This class contains...
*/
public class CreateNearestNeighbor {


// ================ constants ============================================
// =======================================================================

/**
 * The number of nodes to be added.
 * @config
 */
private static final String PAR_SIZE = "size";


/**
 * Parameter u
 * @config
 */
private static final String PAR_U = "u";

/**
 * Parameter u
 * @config
 */
private static final String PAR_K = "k";

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
		
	int size = Configuration.getInt(PAR_SIZE);
	int k = Configuration.getInt(PAR_K, 1);
	double u = Configuration.getDouble(PAR_U, 0.8);
	String filename = Configuration.getString(PAR_FILENAME);
	
	Random r = new Random();

	IntGraph g = new IntGraph(size);

	int counter = 0;
	while (counter < size) {
		
		if (r.nextDouble() < u) {
			//System.out.print("|");
			// Add a new 2-hop edge
			if (counter <= 2)
				continue;
			
			int middle = r.nextInt(counter); 
			while (g.neighbors(middle).size() <= 1) {
				//System.out.print("*");
				middle = r.nextInt(counter);
			}
			IntArray list = g.neighbors(middle);
			int x = r.nextInt(list.size());
			int y = r.nextInt(list.size());
			while (x == y) {
				//System.out.print("-");
				y = r.nextInt(list.size());
			}
			if (!g.neighbors(x).contains(y)) {
				g.addEdge(x,y);
				g.addEdge(y,x);
			}
			
		} else {
			
			// Add a new node, connecting it to a random partner among the existing ones
			int newnode = counter;
			counter++;
			if (counter % 1000 == 0) {
				System.err.println(counter);
			}
			if (counter <= 1) 
				continue;
			int partner = r.nextInt(counter-1);
			g.addEdge(newnode, partner);
			g.addEdge(partner, newnode);
			for (int j=0; j<k; j++) {
				int x = r.nextInt(counter);
				int y = r.nextInt(counter);
				while (x == y) {
					//System.out.print("+");
					y = r.nextInt(counter);
				}
				if (!g.neighbors(x).contains(y)) {
					g.addEdge(x, y);
					g.addEdge(y, x);
				}
			}
			
		}
		
	}
	
	DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

	out.writeInt(size);
	for (int i=0; i < size; i++) {
		out.writeInt(i);
		int degree = g.neighbors(i).size();
		out.writeInt(degree);
		for (int j=0; j < degree; j++) {
			out.writeInt(g.neighbors(i).get(j));
		}
	}
	out.close();


}


}

