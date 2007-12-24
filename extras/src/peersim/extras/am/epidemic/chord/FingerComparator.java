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
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class FingerComparator implements Control
{

private final static String PAR_PROT1 = "protocol1";

private final static String PAR_PROT2 = "protocol2";

private final static String PAR_HOLDER = "holder";

private final int pid1;

private final int pid2;

private final int hid;

private final String prefix;

/**
 * 
 */
public FingerComparator(String prefix)
{
	pid1 = Configuration.getPid(prefix + "." + PAR_PROT1);
	pid2 = Configuration.getPid(prefix + "." + PAR_PROT2);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER);
	this.prefix = prefix;
}

public boolean execute()
{
	int size = Network.size();
	IncrementalFreq m1 = new IncrementalFreq();
	IncrementalFreq m2 = new IncrementalFreq();
	IncrementalStats[] dist = new IncrementalStats[ID.BITS];
	for (int i = 0; i < ID.BITS; i++) {
		dist[i] = new IncrementalStats();
	}
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		Chord chord1 = (Chord) node.getProtocol(pid1);
		Chord chord2 = (Chord) node.getProtocol(pid2);
		int count = 0;
		int cm1 = 0;
		int cm2 = 0;
		for (int j = 0; j < ID.BITS; j++) {
			long lid = IDUtil.getID(node, hid);
			Node finger1 = chord1.getFinger(j);
			Node finger2 = chord2.getFinger(j);
			if (finger1 == null && finger2 != null) {
				cm1++;
			} else if (finger2 == null && finger1 != null) {
				cm2++;
			} else if (finger1 != null && finger2 != null) {
				long rid1 = IDUtil.getID(finger1, hid);
				long rid2 = IDUtil.getID(finger2, hid);
				long dist1 = (rid1 - lid + ID.SIZE) % ID.SIZE;
				long dist2 = (rid2 - lid + ID.SIZE) % ID.SIZE;
				dist[j].add(dist1 - dist2);
			}
		}
		m1.add(cm1);
		m2.add(cm2);
	}
	System.out.println("M1 " + m1);
	System.out.println("M2 " + m2);
	for (int i = 0; i < ID.BITS; i++) {
		System.out.println(prefix + ": " +  "FINGER[" + i + "] = " + dist[i]);
	}
	return false;
}
}
