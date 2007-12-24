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
import peersim.extras.am.epidemic.sorted.*;
import peersim.extras.am.id.*;
import peersim.transport.*;

/**
 * Extract a Chord structure from a ring.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class TChordProxExtraction implements Control
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
 * @config
 */
private static final String PAR_CHORD = "chord";

/**
 * The identifier of the transport protocol used to evaluate the distance of
 * nodes.
 * @config
 */
private static final String PAR_TRANSPORT = "transport";

/**
 * The number of probes to be sent to identify the "nearest" node.
 * @config
 */
private final static String PAR_PROBES = "probes";

/**
 * Initial capacity of array lists; large to avoid excessive garbage collection
 */
private final static int INIT_CAPACITY = 256;

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

/** The identifier of the transport protocol used to evaluate latency */
private final int tid;

/** The number of probes to be sent */
private final int probes;

/** Buffers used to separate neighbors in range intervals */
private final ArrayList[] buffers;

/**
 * 
 */
public TChordProxExtraction(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	rid = Configuration.getPid(prefix + "." + PAR_RING, pid);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER, pid);
	cid = Configuration.getPid(prefix + "." + PAR_CHORD);
	tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
	probes = Configuration.getInt(prefix + "." + PAR_PROBES);
	buffers = new ArrayList[ID.BITS];
	for (int i = 0; i < buffers.length; i++)
		buffers[i] = new ArrayList(INIT_CAPACITY);
}

public boolean execute()
{
	System.err.println(this.getClass());
	int degree = ((Chord) Network.prototype.getProtocol(cid)).successors();
	int size = Network.size();
	int nprobes = 0;
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		Linkable link = (Linkable) node.getProtocol(pid);
		SortedRing ring = (SortedRing) node.getProtocol(rid);
		Chord chord = (Chord) node.getProtocol(cid);
		Transport transport = (Transport) node.getProtocol(tid);
		long lid = IDUtil.getID(node, hid);
		// Separate nodes into intervals
		for (int j = 0; j < ID.BITS; j++) {
			buffers[j].clear();
		}
		int neighbors = link.degree();
		for (int j = 0; j < neighbors; j++) {
			Node rnode = link.getNeighbor(j);
			if (rnode == node)
				continue;
			long rid = getID(rnode);
			long dist = dist(lid, rid);
			int k = ID.log2(dist);
			buffers[k].add(rnode);
		}
		// Search the nearest fingers in range intervals
		for (int k = 0; k < ID.BITS; k++) {
			long minlatency = Long.MAX_VALUE;
			Node minnode = null;
			int buffersize = buffers[k].size();
			int tprobes = Math.min(probes, buffersize);
			nprobes += tprobes;
			for (int j = 0; j < tprobes; j++) {
				int r = CommonState.r.nextInt(buffersize);
				Node rnode = (Node) buffers[k].get(r);
				long latency = transport.getLatency(node, rnode);
				if (/* rnode.isUp() && */latency < minlatency) {
					minlatency = latency;
					minnode = rnode;
				}
				buffersize--;
				buffers[k].set(r, buffers[k].get(buffersize));
				buffers[k].remove(buffersize);
			}
			chord.setFinger(k, minnode);
		}
		// Identify and copy successors
		for (int j = 0; j < degree; j++)
			chord.setSuccessor(j, ring.getLeaf(SortedRing.NEXT, j));
	}
	return false;
}

private long dist(long a, long b)
{
	return (b - a + ID.SIZE) % ID.SIZE;
}

private long getID(Node node)
{
	return ((IDHolder) node.getProtocol(rid)).getID();
}
}
