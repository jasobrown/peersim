/*
 * Copyright (c) 2008 M. Jelasity and N. Tolgyesi
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

package peersim.extras.mj.ednewscast;

import peersim.config.*;
import peersim.core.*;
import peersim.transport.Transport;
import peersim.edsim.*;
import java.security.InvalidParameterException;

/**
 * The newscast protocol.
 */

public class EdNewscast implements EDProtocol, Linkable {

// --------------------------------------------------------------------------
// Static fields
// --------------------------------------------------------------------------

// We are using static temporary arrays to avoid garbage collection
// of them. these are used by all EdNewscast protocols included
// in the protocol array so its size is the maximum of the cache sizes

/** Temp array for merging. Its size is the same as the cache size. */
private static Node[] tn;

/** Temp array for merging. Its size is the same as the cache size. */
private static int[] ts;

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * Cache size.
 * 
 * @config
 */
private static final String PAR_CACHE = "cache";

/**
 * A base frequency of list refreshing.
 * 
 * @config
 */
private static final String PAR_BASEFREQ = "baseFreq";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Neighbors currently in the cache */
private Node[] cache;

/** Time stamps currently in the cache */
private int[] tstamps;

/** A pre-defined refreshing frequency */
private int baseFreq;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

public EdNewscast(String n) {
	final int cachesize = Configuration.getInt(n + "." + PAR_CACHE);
	baseFreq = Configuration.getInt(n + "." + PAR_BASEFREQ);

	// check base freq to be fair
	if (baseFreq <= 0) {
		throw (InvalidParameterException) new InvalidParameterException(
				"parameter 'ednewscast.baseFreq' must be >0");
	}

	if (EdNewscast.tn == null || EdNewscast.tn.length < cachesize) {
		EdNewscast.tn = new Node[cachesize];
		EdNewscast.ts = new int[cachesize];
	}

	cache = new Node[cachesize];
	tstamps = new int[cachesize];

}

public Object clone() {

	EdNewscast sn = null;
	try {
		sn = (EdNewscast) super.clone();
	} catch (CloneNotSupportedException e) {
	} // never happens
	sn.cache = new Node[cache.length];
	sn.tstamps = new int[cache.length];
	System.arraycopy(cache, 0, sn.cache, 0, cache.length);
	System.arraycopy(tstamps, 0, sn.tstamps, 0, tstamps.length);

	return sn;
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

private Node getPeer() {
	final int d = degree();
	if (d == 0)
		return null;
	else
		return cache[CommonState.r.nextInt(d)];
}

// --------------------------------------------------------------------------

public Node getNeighbor(int i) {
	return cache[i];
}

// --------------------------------------------------------------------------

public int degree() {

	int len = cache.length - 1;
	while (len >= 0 && cache[len] == null)
		len--;
	return len + 1;
}

// --------------------------------------------------------------------------

public boolean addNeighbor(Node node) {
	int i;
	for (i = 0; i < cache.length && cache[i] != null; i++) {
		if (cache[i] == node)
			return false;
	}

	if (i < cache.length) {
		if (i > 0 && tstamps[i - 1] < CommonState.getIntTime()) {
			// we need to insert to the first position
			for (int j = cache.length - 2; j >= 0; --j) {
				cache[j + 1] = cache[j];
				tstamps[j + 1] = tstamps[j];
			}
			i = 0;
		}
		cache[i] = node;
		tstamps[i] = CommonState.getIntTime();
		return true;
	} else
		throw new IndexOutOfBoundsException();
}

// --------------------------------------------------------------------------

public boolean contains(Node n) {
	for (int i = 0; i < cache.length; i++) {
		if (cache[i] == n)
			return true;
	}
	return false;
}

// --------------------------------------------------------------------------

private static boolean contains(int size, Node peer) {
	for (int i = 0; i < size; i++) {
		if (EdNewscast.tn[i] == peer)
			return true;
	}
	return false;
}

// --------------------------------------------------------------------------

/**
 * A possiblity for optimization
 */
public void pack() {
}

// --------------------------------------------------------------------------

/**
 * This method merges two cache to keep the freshest information.
 */
private void merge(Node thisNode, EdNewscastMessage EdNcMsg) {

	int i1 = 0; // Index first cache
	int i2 = 0; // Index second cache
	boolean first;
	boolean lastTieWinner = CommonState.r.nextBoolean();
	int i;
	i = 1; // Index new cache. first element set in the end

	final int d1 = degree();
	final int d2 = EdNcMsg.degree();
	// cachesize is cache.length

	// merging two arrays
	while (i < cache.length && i1 < d1 && i2 < d2) {
		if (tstamps[i1] == EdNcMsg.tstampsM[i2]) {
			lastTieWinner = first = !lastTieWinner;
		} else {

			first = tstamps[i1] > EdNcMsg.tstampsM[i2];
		}

		if (first) {
			if (cache[i1] != EdNcMsg.sender
					&& !EdNewscast.contains(i, cache[i1])) {
				EdNewscast.tn[i] = cache[i1];
				EdNewscast.ts[i] = tstamps[i1];
				i++;
			}
			i1++;
		} else {
			if (EdNcMsg.cacheM[i2] != thisNode
					&& !EdNewscast.contains(i, EdNcMsg.cacheM[i2])) {
				EdNewscast.tn[i] = EdNcMsg.cacheM[i2];
				EdNewscast.ts[i] = EdNcMsg.tstampsM[i2];
				i++;
			}
			i2++;
		}
	}// while

	// if one of the original arrays got fully copied into
	// tn and there is still place, fill the rest with the other
	// array
	if (i < cache.length) {
		// only one of the for cycles will be entered

		for (; i1 < d1 && i < cache.length; ++i1) {
			if (cache[i1] != EdNcMsg.sender
					&& !EdNewscast.contains(i, cache[i1])) {
				EdNewscast.tn[i] = cache[i1];
				EdNewscast.ts[i] = tstamps[i1];
				i++;
			}
		}

		for (; i2 < d2 && i < cache.length; ++i2) {
			if (EdNcMsg.cacheM[i2] != thisNode
					&& !EdNewscast.contains(i, EdNcMsg.cacheM[i2])) {
				EdNewscast.tn[i] = EdNcMsg.cacheM[i2];
				EdNewscast.ts[i] = EdNcMsg.tstampsM[i2];
				i++;
			}
		}
	}

	// if the two arrays were not enough to fill the buffer
	// fill in the rest with nulls
	if (i < cache.length) {
		for (; i < cache.length; ++i) {
			EdNewscast.tn[i] = null;
		}
	}

	System.arraycopy(tn, 0, cache, 0, cache.length);
	System.arraycopy(ts, 0, tstamps, 0, tstamps.length);

	// Set the sender
	tstamps[0] = CommonState.getIntTime();
	cache[0] = EdNcMsg.sender;
}

// --------------------------------------------------------------------------

public void onKill() {
	cache = null;
	tstamps = null;
}

// --------------------------------------------------------------------------

public String toString() {
	if (cache == null)
		return "DEAD!";

	StringBuffer sb = new StringBuffer();

	for (int i = 0; i < degree(); ++i) {
		sb.append(" (" + cache[i].getIndex() + "," + tstamps[i] + ")");
	}
	return sb.toString();
}

// --------------------------------------------------------------------------

/**
 * This method starts a communication.
 */
private void sendReq(Node node, int pid) {
	
	// Choose a node from own cache to communicate with
	final Node peern = getPeer();

	if (peern != null) {
		((Transport) node.getProtocol(FastConfig.getTransport(pid))).send(
				node, 
				peern, 
				new EdNewscastMessage(node, cache, tstamps,false), 
				pid
		);
	}
}

// --------------------------------------------------------------------------

/**
 * This method process all kind of events.
 * {@link CycleMessage} means that node has to execute the next gossip cycle.
 * {@link EdNewscastMessage} is the newscast message containing the cache.
 */
public void processEvent(Node node, int pid, Object event) {
	
	if (event instanceof EdNewscastMessage) {
		
		final EdNewscastMessage enm = (EdNewscastMessage) event;
		if (enm.sender == null)
			throw new RuntimeException("No Sender (sender is NULL)!");

		if (!enm.isAnswer()) {
			/* Got request, send an answer */
			((Transport) node.getProtocol(FastConfig.getTransport(pid))).send(
					node, 
					enm.sender, 
					new EdNewscastMessage(node,cache, tstamps, true), 
					pid
			);
		}

		merge(node, enm);
	}

	if (event instanceof CycleMessage) {
		// sending a connection request
		sendReq(node, pid);

		// add the next event to the queue with base frequency as a delay
		EDSimulator.add(baseFreq, CycleMessage.inst, node, pid);
	}
}

}

