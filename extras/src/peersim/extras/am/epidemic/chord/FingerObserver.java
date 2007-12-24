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
import peersim.extras.am.id.*;
import peersim.util.*;

/**
 * Reports statistics on the number of fingers.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class FingerObserver implements Control
{

/** 
 * The Chord protocol to be observed 
 */
private final static String PAR_PROT = "protocol";

/** 
 * The protocol containing the identifier; defaults to {@value #PAR_PROT}.
 */
private final static String PAR_HOLDER = "holder";

/** Id of the protocol implementing Chord */
private final int pid;

/** Id of the protocol implementing IDHolder */
private final int hid;

/** Prefix for printing */
private final String prefix;

/**
 * Reads configuration parameters, using the specified prefix.
 */
public FingerObserver(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER, pid);
	this.prefix = prefix;
}

public boolean execute()
{
	// Initialize statistics
	IncrementalStats[] stats = new IncrementalStats[ID.BITS];
	for (int i=0; i < stats.length; i++)
		stats[i] = new IncrementalStats();
	
	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		long lid = IDUtil.getID(node, hid);
		Chord chord = (Chord) node.getProtocol(pid);
		for (int j = 0; j < ID.BITS; j++) {
			Node finger = chord.getFinger(j);
			if (finger != null) {
				long rid = IDUtil.getID(finger, hid);
				long dist = ID.dist(lid, rid);
				stats[j].add(dist);
			}
		}
	}
	
	// Print final statistics
	for (int i=0; i < stats.length; i++) {
		System.out.println(prefix + ": TIME " + CommonState.getTime() + " " + 
				i + " " + stats[i]);
		stats[i] = new IncrementalStats();
	}
	
	return false;
}
}
