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

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class UniformIDDistribution implements Control, NodeInitializer
{

/** String name of the parameter identifying the protocol to be initialized */
private final static String PAR_PROTOCOL = "protocol";

/** Protocol identifier */
private int pid;

/**
 * 
 */
public UniformIDDistribution(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
}

// Comment inherited from interface
public boolean execute()
{
	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		IDHolder peer = (IDHolder) node.getProtocol(pid);
		peer.setID(ID.create());
	}
	// Node[] nodes = new Node[size];
	// for (int i=0; i < size; i++) {
	// nodes[i] = Network.get(i);
	// }
	// Arrays.sort(nodes, new IDNodeComparator(pid));
	//
	// long prev = ((IDHolder) nodes[0].getProtocol(pid)).getID();
	// for (int i=1; i < size; i++) {
	// long id = ((IDHolder) nodes[i].getProtocol(pid)).getID();
	// if (id == prev) {
	// initialize(nodes[i]);
	// }
	// prev = ((IDHolder) nodes[i].getProtocol(pid)).getID();
	// }
	return false;
}

// Comment inherited from interface
public void initialize(Node node)
{
	IDHolder peer = (IDHolder) node.getProtocol(pid);
	// long r;
	//  
	// boolean found;
	// do {
	// r = ID.create();
	// found = false;
	// for (int i=0; i < Network.size() && !found; i++) {
	// long id = ((IDHolder) Network.get(i).getProtocol(pid)).getID();
	// if (id == r)
	// found = true;
	// }
	// } while (!found);
	// peer.setID(r);
	peer.setID(ID.create());
}
}
