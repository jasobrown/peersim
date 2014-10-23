/*
 * Copyright (c) 2011 Alberto Montresor
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

import peersim.config.*;
import peersim.core.*;

/**
* Takes a {@link Linkable} protocol and adds connections that are stored in a
* binary file. Note that no connections are removed, they are only added. 
* So it can be used in combination with other initializers.
* The binary format of the file is as follows. First, the total number
* of nodes is stored as a 32-bit integer. Each node is represented
* by the id, its out-degree, and a list of neighbors. 
*/
public class WireFromBinaryFile implements Control {


// ========================= fields =================================
// ==================================================================

/**
 * The {@link Linkable} protocol to operate on.
 * @config
 */
private static final String PAR_PROTOCOL = "protocol";

/** 
*  The filename to load links from.
*  @config
*/
private static final String PAR_FILE = "file";

/** 
*  If defined, the undirected version of the graph will be wired. I.e., if a pair
* <tt>x y</tt> is found in the file, <tt>y</tt> will be added as neighbor of
* <tt>x</tt> and <tt>x</tt> will be added as neighbor of <tt>y</tt>. Not defined
* by default.
*  @config
*/
private static final String PAR_UNDIR = "undir";

private final int pid;

private final String file;

private final boolean undir;



// ==================== initialization ==============================
// ==================================================================


/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public WireFromBinaryFile(String prefix) {

	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	file = Configuration.getString(prefix+"."+PAR_FILE);
	undir = Configuration.contains(prefix + "." + PAR_UNDIR); 

}


// ===================== public methods ==============================
// ===================================================================


/**
* Wires the graph from a binary file.
*/
public boolean execute() {
try
{
	boolean wasOutOfRange=false;
	DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
	int n = input.readInt();

	Network.setCapacity(n);
	System.out.println(Network.size());
	
	for (int i=0; i < n; i++) {
		int from = input.readInt();
		if( from < 0 || from >= Network.size() || from != i) {
			System.err.println(this.getClass().getName() + " error: in "+file+" "+
			"some nodes were out of range: from=" + from);
			System.exit(1);
		}
		Node nodeFrom = Network.get(from);
		Linkable linkFrom = (Linkable) nodeFrom.getProtocol(pid);		
		int d = input.readInt();
		for (int j=0; j < d; j++) {
			int to = input.readInt();
			if( to < 0 || to >= Network.size() ) {
				System.err.println(this.getClass().getName() + " error: in "+file+" "+
				"some nodes were out of range: to=" + to);
				System.exit(1);
			}
			Node nodeTo = Network.get(to);
			Linkable linkTo = (Linkable) nodeTo.getProtocol(pid);
			linkFrom.addNeighbor(nodeTo);
			if (undir) {
				linkTo.addNeighbor(nodeFrom);
			}
		}
		linkFrom.pack();
	}
	
	if( wasOutOfRange )
		System.err.println("WireFromFile warning: in "+file+" "+
			"some nodes were out of range and so ignored.");
	input.close();
	return false;
}
catch( IOException e )
{
	throw new RuntimeException(e);
}
}

}
