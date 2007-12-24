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

package peersim.extras.am.epidemic.sorted;


import java.util.*;


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
public class SortedRingObserver implements Control
{

// --------------------------------------------------------------------------
// Constants
// --------------------------------------------------------------------------
/** The protocol to be observed */
public static final String PAR_PROT = "protocol";

/** If present, the protocol will stop when the ring has been obtained */
public static final String PAR_STOP = "stop";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------
/** Identifier of the protocol that hosts the protocol id */
private final int pid;

/** If true, the protocol will stop when the ring has been obtained */
private final boolean stop;

/** Observer prefix to be printed */
private final String prefix;

/** True when the ring has been obtained */
private boolean endtime = false;

/** Node array, used to detect the ring */
private Node[] nodes;

/** */
private int[] count;

// --------------------------------------------------------------------------
// Constants
// --------------------------------------------------------------------------
/**
 * 
 */
public SortedRingObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	stop = Configuration.contains(prefix + "." + PAR_STOP);
}

// Comment inherited from interface
public boolean execute()
{
	Comparator idc = new IDNodeComparator(pid);
	int size = Network.size();
//	for (int i = 0; i < Network.size(); i++) {
//		if (Network.get(i).isUp())
//			size++;
//	}
	if (nodes == null || nodes.length != size) {
		nodes = new Node[size];
		count = new int[size];
		for (int i = 0; i < size; i++) {
			nodes[i] = Network.get(i);
		}
		Arrays.sort(nodes, idc);
		// for (int i=1; i < nodes.length; i++) {
		// if (IDUtil.getID(nodes[i], pid) == IDUtil.getID(nodes[i-1], pid))
		// System.out.println("!!!");
		// }
	}
	int degree = ((SortedRing) Network.get(0).getProtocol(pid)).leafDegree();
	IncrementalStats nmissing = new IncrementalStats();
	IncrementalStats pmissing = new IncrementalStats();
	IncrementalStats[] pcorrect = new IncrementalStats[degree];
	IncrementalStats[] ncorrect = new IncrementalStats[degree];
	IncrementalStats ndistance = new IncrementalStats();
	IncrementalStats pdistance = new IncrementalStats();
	for (int i = 0; i < degree; i++) {
		pcorrect[i] = new IncrementalStats();
		ncorrect[i] = new IncrementalStats();
	}
	int[] pcorr = new int[degree];
	int[] ncorr = new int[degree];
	for (int i = 0; i < size; i++) {
		Node node = nodes[i];
		SortedRing ring = (SortedRing) node.getProtocol(pid);
		if (!node.isUp())
			continue;
		for (int k = 0; k < pcorr.length; k++)
			pcorr[k] = 0;
		for (int k = 0; k < ncorr.length; k++)
			ncorr[k] = 0;
		int nmiss = degree;
		int pmiss = degree;
		long lid = IDUtil.getID(node, pid);
		long rid = IDUtil.getID(ring.getLeaf(SortedRing.NEXT, 0), pid);
		long dist = ID.dist(lid, rid);
		long cid = IDUtil.getID(nodes[(i + 1) % size], pid);
		long cdist = ID.dist(lid, cid);
		ndistance.add(dist-cdist);
		rid = IDUtil.getID(ring.getLeaf(SortedRing.PREV, 0), pid);
		cid = IDUtil.getID(nodes[(i -1 + size) % size], pid);
		dist = ID.dist(lid, rid);
		cdist = ID.dist(lid, cid);
		pdistance.add(dist-cdist);
		for (int j = 0; j < degree; j++) {
			Node npeer = ring.getLeaf(SortedRing.NEXT, j);
			if (npeer != null) {
				for (int k = 0; k < degree; k++) {
					if (npeer == nodes[(i + k + 1) % size]) {
						nmiss--;
						ncorr[k]++;
						break;
					}
				}
			}
			Node ppeer = ring.getLeaf(SortedRing.PREV, j);
			if (ppeer != null) {
				for (int k = 0; k < degree; k++) {
					if (ppeer == nodes[(i - k - 1 + size) % size]) {
						pmiss--;
						pcorr[k]++;
						break;
					}
				}
			}
		}
		for (int j = 0; j < degree; j++) {
			pcorrect[j].add(pcorr[j]);
			ncorrect[j].add(ncorr[j]);
		}
		// if ((nmiss > 0 || pmiss > 0) && CommonState.getTime() > 10) {
		// for (int k=0; k < degree; k++) {
		// System.out.print("ID: " + lid);
		// System.out.print(" RP " + IDUtil.getID(ring.getLeaf(Ring.PREV, k), pid));
		// System.out.print(" AP " + IDUtil.getID(nodes[(i-k-1+size)%size], pid));
		// System.out.print(" RN " + IDUtil.getID(ring.getLeaf(Ring.NEXT, k), pid));
		// System.out.print(" AN " + IDUtil.getID(nodes[(i+k+1)%size], pid));
		// System.out.println("");
		// }
		// }
		nmissing.add(nmiss);
		pmissing.add(pmiss);
	}
	System.out.println(prefix + ": " +  " TIME " + CommonState.getTime() + " MISSING PREV "
			+ pmissing);
	System.out.println(prefix + ": " +  " TIME " + CommonState.getTime() + " MISSING NEXT "
			+ nmissing);
	System.out.println(prefix + ": " +  " TIME " + CommonState.getTime() + " DISTANCE PREV "
			+ pdistance);
	System.out.println(prefix + ": " +  " TIME " + CommonState.getTime() + " DISTANCE NEXT "
			+ ndistance);
	for (int i = 0; i < degree; i++) {
		System.out.println(prefix + ": " +  "" + " TIME " + CommonState.getTime() + " CORRECT[" + i
				+ "] PREV " + pcorrect[i] + " NEXT " + ncorrect[i]);
	}
	if (!endtime && pmissing.getSum() == 0.0 && nmissing.getSum() == 0.0) {
		System.out.println(prefix + ": " +  " ENDTIME " + CommonState.getTime() + " MISSING PREV "
				+ pmissing);
		System.out.println(prefix + ": " +  " ENDTIME " + CommonState.getTime() + " MISSING NEXT "
				+ nmissing);
		endtime = true;
		return stop;
	}
	return false;
}
}