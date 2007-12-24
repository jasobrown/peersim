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


import java.util.*;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;
import peersim.extras.am.util.*;
import peersim.transport.*;
import peersim.util.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ChordObserver implements Control
{

// ---------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------
/**
 * The identifier of the Chord protocol to be observed.
 * @config
 */
private final static String PAR_PROTOCOL = "protocol";

/**
 * The identifier of the protocol containing the peersim.extras.am.id. Defaults to
 * parameter {@value #PAR_PROTOCOL}.
 * @config
 */
private final static String PAR_HOLDER = "holder";

/**
 * The identiefer of the transport protocol used to compute latency. If absent,
 * latency estimations are not produced.
 * @config
 */
private final static String PAR_TRANSPORT = "transport";

/** If present, the protocol will stop when the ring has been obtained */
public static final String PAR_STOP = "stop";

/**
 * String identifier that is used to log values. Defaults to "CHORD".
 */
private final static String PAR_STRING = "loginfo";


// ---------------------------------------------------------------------
// Configuration Variables
// ---------------------------------------------------------------------
/** Protocol identifier */
private final int pid;

/** Identifier of the IDHolder protocol */
private final int hid;

/** Transport protocol identifier */
private final int tid;

/** True if the observer has to stop the simulation when lost reaches 0 */
private final boolean stop;

private final String loginfo;

/** Observer prefix to be printed */
private final String prefix;

// ---------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------

/** For each finger level, the number of steps that have been performed */
IncrementalStats[] steps;

/** Comparator for node ordering */
private Comparator idc;

/** Temporary buffer */
private Node[] tobevisited;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * 
 */
public ChordObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER, pid);
	tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT, -1);
	stop = Configuration.contains(prefix + "." + PAR_STOP);
	loginfo = Configuration.getString(prefix + "." + PAR_STRING, "CHORD");
	idc = new IDNodeComparator(pid);
	// Prepare buffers
	int degree = ((Chord) Network.prototype.getProtocol(pid)).successors();
	tobevisited = new Node[degree + ID.BITS];
}

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

// Comment inherited from interface
public boolean execute()
{
	steps = new IncrementalStats[ID.BITS];
	for (int i = 0; i < ID.BITS; i++) {
		steps[i] = new IncrementalStats();
	}
	System.out.println(prefix + ": ");
	IncrementalStatsFreq hops = new IncrementalStatsFreq();
	IncrementalStatsFreq failures = new IncrementalStatsFreq();
	IncrementalStats latency = new IncrementalStats();
	int lost = 0;
	int size = Network.size();
	for (int j = 0; j < size; j++) {
		Node node = Network.get(j);
		if (!node.isUp())
			continue;
		int r = -1;
		while (r < 0 || !Network.get(r).isUp()) {
			r = CommonState.r.nextInt(size);
		}
		if (!doTest(Network.get(r), node, hops, failures, latency))
			lost++;
	}
	System.out.println(prefix + ": " +  "TIME " + CommonState.getTime() + " " + loginfo + "-H " 
			+ " LOST " + lost	+ " " + hops);
//	for (int i = 0; i < ID.BITS; i++) {
//		System.out.println(prefix + ": " +  "TIME " + CommonState.getTime() + " " + loginfo + "-S["
//				+ i + "] " + steps[i]);
//	}
	System.out.println(prefix + ": " +  "TIME " + CommonState.getTime() + " " + loginfo + "-F "
			+ failures);
	if (tid >= 0) {
		System.out.println(prefix + ": " +  "TIME " + CommonState.getTime() + " " + loginfo + "-L "
				+ latency);
	}
	return (lost == 0 && stop);
}


private boolean doTest(Node src, Node dst, IncrementalStatsFreq pathlen,
		IncrementalStatsFreq failures, IncrementalStats latency)
{
	int hops = 0;
	int fail = 0;
	double lat = 0;
	long did = getID(dst);
	long cid = getID(src);
	Node curr = src; // If null, we are stucked
	while (cid != did && curr != null) {
		Transport transport = null;
		if (tid >= 0) {
			transport = (Transport) curr.getProtocol(tid);
		}
		Chord chord = (Chord) curr.getProtocol(pid);
		int size = 0;
		for (int i = 0; i < chord.successors(); i++)
			tobevisited[size++] = chord.getSuccessor(i);
		for (int i = 0; i < ID.BITS; i++)
			if (chord.getFinger(i) != null)
				tobevisited[size++] = chord.getFinger(i);
		for (int i = size; i < tobevisited.length; i++)
			tobevisited[i] = null;
		Node next = searchNextLeaf(tobevisited, did, dist(cid, did));
		while (next != null && !next.isUp()) {
			if (transport != null)
				lat += transport.getLatency(curr, next) * 2;
			fail++;
			// System.out.println("+->" + cid);
			next = searchNextLeaf(tobevisited, did, dist(cid, did));
		}
		if (next != null) {
			if (transport != null)
				lat += transport.getLatency(curr, next);
			curr = next;
			long oldcid = cid;
			cid = getID(curr);
			long dist = dist(oldcid, cid);
			steps[ID.log2(dist)].add(dist);
			hops++;
		} else {
			curr = null;
		}
	}
	if (cid == did) {
		pathlen.add(hops);
		latency.add(lat);
		failures.add(fail);
		return true;
	} else {
		return false;
	}
}

private int searchNextFinger(Chord peer, long cid, long did, int k)
{
	long dist = dist(cid, did);
	while (k >= 0
			&& (peer.getFinger(k) == null || dist(getID(peer.getFinger(k)), did) > dist)) {
		k--;
	}
	return k;
}

private Node searchNextLeaf(Node[] buffer, long did, long minDist)
{
	int min = -1;
	int len;
	for (len = 0; len < buffer.length && buffer[len] != null && minDist > 0; len++) {
		IDHolder p = (IDHolder) buffer[len].getProtocol(hid);
		long d = dist(p.getID(), did);
		// System.out.println("dest " + did + " p " + p.getID() + " dist " + d + "
		// minDist " + minDist);
		if (d < minDist) {
			minDist = d;
			min = len;
		}
	}
	if (min >= 0) {
		Node n = buffer[min];
		buffer[min] = buffer[len - 1];
		buffer[len - 1] = null;
		return n;
	} else {
		return null;
	}
}

private long dist(long a, long b)
{
	return (b - a + ID.SIZE) % ID.SIZE;
}

private long getID(Node node)
{
	return ((IDHolder) node.getProtocol(hid)).getID();
}
}
