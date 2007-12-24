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

package peersim.extras.am.epidemic.pastry;

import java.util.*;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;
import peersim.transport.*;
import peersim.util.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class PastryObserver implements Control
{

// ---------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------
/**
 * The identifier of the Pastry protocol to be observed.
 * @config
 */
private final static String PAR_PROTOCOL = "protocol";

/**
 * The identifier of the Ring protocol to be observed.
 * @config
 */
private final static String PAR_RING = "ring";

/**
 * The identifier of the protocol containing the peersim.extras.am.id. Defaults to
 * parameter {@value #PAR_PROTOCOL}.
 * @config
 */
private final static String PAR_HOLDER = "holder";

/**
 * The identiifer of the transport protocol used to compute latency. If absent,
 * latency estimations are not produced.
 * @config
 */
private final static String PAR_TRANSPORT = "transport";

/**
 * String identifier that is used to log values. Defaults to "CHORD".
 */
private final static String PAR_STRING = "loginfo";

// ---------------------------------------------------------------------
// Configuration Variables
// ---------------------------------------------------------------------
/** Identifier of the Pastry protocol */
private final int pid;

/** Identifier of the IDHolder protocol */
private final int hid;

/** Transport protocol identifier */
private final int tid;

/** Observer prefix to be printed */
private final String prefix;

// ---------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------
/** Freezed set of nodes */
private Node[] nodes;

/** Comparator for node ordering */
private Comparator idc;

private Node[] tobevisited;

private int digits, radix, b, mask;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * 
 */
public PastryObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER, pid);
	tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT, -1);
	idc = new IDNodeComparator(pid);
	Pastry prot = (Pastry) Network.prototype.getProtocol(pid);
	digits = prot.digits();
	radix = prot.radix();
	b = prot.b();
	mask = (1 << b) - 1;
	tobevisited = new Node[4096];
}

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
// Comment inherited from interface
public boolean execute()
{
	IncrementalStats[] d = new IncrementalStats[digits];
	for (int i = 0; i < digits; i++)
		d[i] = new IncrementalStats();
	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		Pastry pastry = (Pastry) node.getProtocol(pid);
		for (int j = 0; j < digits; j++) {
			int count = 0;
			for (int k = 0; k < radix; k++) {
				if (pastry.getFinger(j, k) != null)
					count++;
			}
			d[j].add(count);
		}
	}
	for (int i = 0; i < digits; i++) {
		System.out.println(prefix + ": " +  "row " + i + ": " + d[i]);
	}
	IncrementalStats hops = new IncrementalStats();
	IncrementalStats latency = new IncrementalStats();
	int lost = 0;
	for (int j = 0; j < size; j++) {
		Node nj = Network.get(j);
		if (!nj.isUp())
			continue;
		int r;
		Node nr;
		do {
			r = CommonState.r.nextInt(size);
			nr = Network.get(r);
		} while (!nr.isUp());
		if (!doTest(hops, latency, nr, nj)) {
			lost++;
		}
	}
	System.out.println(prefix + ": " +  "TIME " + CommonState.getTime() + " OPT-H " + hops
			+ " LOST " + lost);
	System.out.println(prefix + ": " +  "TIME " + CommonState.getTime() + " OPT-L " + latency);
	return false;
}

private boolean doTest(IncrementalStats hops, IncrementalStats latency,
		Node src, Node dst)
{
	Transport transport = null;
	int nhops = 0;
	int nfailures = 0;
	int tlatency = 0;
	Node curr = src;
	long did = getID(dst);
	long cid = getID(src);
	boolean blocked = false;
	while (cid != did && curr != null) {
		if (tid >= 0)
			transport = (Transport) curr.getProtocol(tid);
		Pastry pastry = (Pastry) curr.getProtocol(pid);
		// Search the next hop in the routing table
		//System.out.println(idToString(cid));
		//System.out.println(idToString(did));
		int row = commonprefix(did ^ cid);
		int col = (int) ((did >> (ID.BITS - b - row * b)) & mask);
		//System.out.println(row + " " + col);
		Node next = pastry.getFinger(row, col);
		// System.out.println("search " + did + " " + curr + " " + next + " " +
		// row);
		if (next != null && !next.isUp()) {
			// Finger found, but crashed. Consider it not found.
			nfailures++;
			if (transport != null)
				tlatency += transport.getLatency(curr, next) * 2;
			next = null;
		}
		// If not found in the routing table
		if (next == null) {
			// System.out.print("O");
			int len = 0;
			long minDist = ID.dist(cid, did);
			// System.out.println("H " + minDist);
			for (int i = 0; i < digits; i++) {
				for (int j = 0; j < radix; j++) {
					Node n = pastry.getFinger(i, j);
					if (n != null && ID.dist(getID(n), did) < minDist)
						tobevisited[len++] = n;
				}
			}
			for (int i = 0; i < pastry.leafDegree(); i++) {
				if (pastry.getLeaf(Pastry.NEXT, i) != null)
					tobevisited[len++] = pastry.getLeaf(Pastry.NEXT, i);
				if (pastry.getLeaf(Pastry.PREV, i) != null)
					tobevisited[len++] = pastry.getLeaf(Pastry.PREV, i);
			}
			// System.out.println(len);
			while (len > 0) {
				// System.out.println("inside " + did + " " + curr + " " + next + " " +
				// len);
				// Search min
				minDist = ID.dist(cid, did);
				int min = -1;
				for (int k = 0; k < len; k++) {
					long nid = getID(tobevisited[k]);
					int pos = commonprefix(did ^ nid);
					long dist = ID.dist(nid, did);
					if ((pos >= row || dist == 0) && (dist < minDist)) {
						minDist = dist;
						min = k;
					}
				}
				// Check if a possible min has been found
				if (min < 0) {
					len = 0;
				} else {
					// System.out.println("next " + tobevisited[min]);
					if (tobevisited[min].isUp()) {
						next = tobevisited[min];
						len = 0;
						// System.out.print("*");
					} else {
						if (transport != null)
							tlatency += transport.getLatency(curr, tobevisited[min]) * 2;
						nfailures++;
						tobevisited[min] = tobevisited[--len];
					}
				}
			}
		}
		if (next != null) {
			if (transport != null)
				tlatency += transport.getLatency(curr, next);
			nhops++;
			cid = getID(next);
		}
		curr = next;
	}
	if (cid == did) {
		hops.add(nhops);
		latency.add(tlatency);
		return true;
	} else {
		return false;
	}
}

private int commonprefix(long val)
{
	int common = digits;
	while (val > 0) {
		common--;
		val = val >> b;
	}
	return common;
}

private long getID(Node node)
{
	return ((IDHolder) node.getProtocol(hid)).getID();
}

private String idToString(long id)
{
	String s1 = ("0000000000000000000000000000000000000000000000000000000" + Long
			.toString(id, radix));
	s1 = s1.substring(s1.length() - digits);
	return s1;
}
}
