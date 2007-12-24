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

package peersim.extras.am.epidemic.chord;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.epidemic.sorted.*;
import peersim.extras.am.id.*;

/**
 * Extract a Chord structure from a ring.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class TChordExtraction implements Control
{

/**
 * The identifier of the protocol from which the chord topology should be
 * extracted. Normally, this protocol implements the {@link Linkable},
 * {@link SortedRing} and {@link IDHolder} interfaces. The first is used to extract
 * the fingers, the second is used to extract the leafs, while the third
 * provides the identifier of the node. It is possible to specify different
 * protocols, though, for the {@link SortedRing} and {@link IDHolder} interfaces
 * through parameters {@value #PAR_RING} and {@value #PAR_HOLDER}.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * The identifier of the Ring protocol containing the successors. Defaults to
 * {@value #PAR_PROT}.
 * @config
 */
private static final String PAR_HOLDER = "ring";

/**
 * The identifier of the IDHolder protocol containing the peersim.extras.am.id. Defaults
 * to {@value #PAR_PROT}.
 * @config
 */
private static final String PAR_RING = "ring";

/**
 * The identifier of the chord protocol where the topology should be stored.
 */
private static final String PAR_CHORD = "chord";

/**
 * The identifier of the linkable protocol from which the fingers are extracted
 */
private final int pid;

/** The identifier of the Ring protocol containing successors */
private final int rid;

/** The identifier of the protocol containing the Chord id. */
private final int hid;

/** The identifier of the chord protocol where the topology is stored */
private final int cid;

/**
 * 
 */
public TChordExtraction(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	rid = Configuration.getPid(prefix + "." + PAR_RING, pid);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER, pid);
	cid = Configuration.getPid(prefix + "." + PAR_CHORD);
}

public boolean execute()
{
	System.err.println(this.getClass());
	int degree = ((Chord) Network.prototype.getProtocol(cid)).successors();
	Node[] fingers = new Node[ID.BITS];
	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		Linkable link = (Linkable) node.getProtocol(pid);
		SortedRing ring = (SortedRing) node.getProtocol(rid);
		Chord chord = (Chord) node.getProtocol(cid);
		IDHolder holder = (IDHolder) node.getProtocol(hid);
		// Identify and copy fingers
		ChordLibrary.resetFingers(fingers);
		ChordLibrary.selectFingers(link, fingers, node, holder.getID(), rid,
				SortedRing.NEXT, null);
		for (int j = 0; j < fingers.length; j++) {
			chord.setFinger(j, fingers[j]);
		}
		// Identify and copy successors
		for (int j = 0; j < degree; j++) {
			chord.setSuccessor(j, ring.getLeaf(SortedRing.NEXT, j));
		}
	}
	return false;
}
}
