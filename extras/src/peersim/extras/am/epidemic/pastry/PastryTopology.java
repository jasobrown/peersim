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
import peersim.extras.am.epidemic.*;
import peersim.extras.am.epidemic.sorted.*;
import peersim.extras.am.id.*;

/**
 * This class is idle protocol implementing the {@link Pastry} interface.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class PastryTopology
		implements
			Protocol,
			Pastry,
			EpidemicProtocol,
			IDHolder,
			Linkable
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------
/**
 * The identifier of the actual protocol that implements Ring.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * Parameter b of Pastry
 */
private static final String PAR_B = "b";

/**
 * Message size
 */
private static final String PAR_MSGSIZE = "msgsize";

/** If defined, this protocol works with event-based engines */
private static final String PAR_EVENT = "event";

// ---------------------------------------------------------------------
// Helper classes
// ---------------------------------------------------------------------
/**
 * Helper class with protocol parameters. There is a single instance of this
 * class per node.
 */
private class ProtocolData
{

/** size of exchanged messages */
private final int msgsize;

/** b parameter of pastry */
private final int b;

/** radix = 2^b */
private final int radix;

/** mask used to obtain digits */
private final int mask;

/** number of rows in the routing table */
private final int digits;

/** size of the routing table */
private final int nfingers;

/** ring protocol identifier */
private final int rid;

/** Singleton request to be used in cycle-based simulations */
private final PastryMessage request;

/** Singleton reply to be used in cycle-based simulations */
private final PastryMessage reply;

/** this protocol identifier */
private final int pid;

/** Buffer for peer selection */
private final Node[] buffer;

/** Buffer for peer selection */
private final Node[] row;

/** True if this protocol is executed in an event-based simulator */
private final boolean event;

ProtocolData(String prefix)
{
	msgsize = Configuration.getInt(prefix + "." + PAR_MSGSIZE);
	event = Configuration.contains(prefix + "." + PAR_EVENT);
	rid = Configuration.getPid(prefix + "." + PAR_PROT);
	pid = CommonState.getPid();
	b = Configuration.getInt(prefix + "." + PAR_B);
	digits = ID.BITS / b;
	if (digits * b != ID.BITS) {
		System.err.println("Configuration error: ID.BITS not multiple of b");
		System.exit(1);
	}
	radix = 1 << b;
	mask = radix - 1;
	nfingers = digits * radix;
	buffer = new Node[nfingers];
	row = new Node[radix];
	request = new PastryMessage(msgsize, nfingers);
	reply = new PastryMessage(msgsize, nfingers);
}
}

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------
/** Ring protocol to be used for leafs */
private final SortedRing ring;

/** Reference to the protocol data */
private final ProtocolData p;

/** Fingers */
private final Node[] fingers;

/** Identifier */
private long id;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * Initializes an empty Chord node.
 */
public PastryTopology(String prefix)
{
	p = new ProtocolData(prefix);
	ring = (SortedRing) CommonState.getNode().getProtocol(p.rid);
	fingers = new Node[p.nfingers];
}

public PastryTopology(ProtocolData p)
{
	this.p = p;
	ring = (SortedRing) CommonState.getNode().getProtocol(p.rid);
	fingers = new Node[p.nfingers];
}

/**
 * Clone an empty Chord node.
 */
public Object clone()
{
	return new PastryTopology(p);
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------
/** @inheritDoc */
public int b()
{
	return p.b;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public int radix()
{
	return p.radix;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public int digits()
{
	return p.digits;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getFinger(int common, int index)
{
	assert index >= 0 && index < p.radix;
	assert common >= 0 && common < p.digits;
	int k = common * p.radix + index;
	return fingers[k];
}

public void addFinger(Node node)
{
	// XXX to be completed
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void setFinger(int common, int index, Node node)
{
	assert index >= 0 && index < p.radix;
	assert common >= 0 && common < p.digits;
	int k = common * p.radix + index;
	fingers[k] = node;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getLeaf(int dir, int i)
{
	return ring.getLeaf(dir, i);
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void setLeaf(int dir, int index, Node node)
{
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------------
public int leafDegree()
{
	return ring.leafDegree();
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node selectPeer(Node lnode)
{
	// Fingers are organized in a linear array, with fingers with longest
	// common sequences at the end. So, we reverse the ordering when
	// chosing them
	// for (int i=p.digits-1; i >= 0 && size == 0; i--) {
	// for (int j=0; j < p.radix; j++) {
	// if (fingers[i*p.radix+j] != null)
	// p.buffer[size++] = fingers[i*p.radix+j];
	// }
	// }
	/*
	 * Dato un livello, vengono selezionati sempre gli stessi.
	 */
	int size = 0;
	int maxsize = p.radix / 2;
	for (int i = p.digits - 1; i >= 0 && size < maxsize; i--) {
		int rsize = 0;
		for (int j = 0; j < p.radix; j++) {
			Node f = getFinger(i, j);
			if (f != null) {
				p.row[rsize++] = f;
			}
		}
		int excess = Math.max(0, (size + rsize) - maxsize);

		// Remove the excessive links
		for (int j = 0; j < excess; j++) {
			int r = CommonState.r.nextInt(rsize);
			p.row[r] = p.row[--rsize];
		}
		for (int j = 0; j < rsize; j++) {
			p.buffer[size++] = p.row[j];
		}
	}
	// System.out.println("------------");
	// size = Math.min(size, p.radix/4);
	if (size == 0) {
		return null;
	}
	int r = CommonState.r.nextInt(size);
	return p.buffer[r];
}

public Message prepareRequest(Node lnode, Node rnode)
{
	int degree = leafDegree();
	for (int i = 0; i < degree; i++) {
		Node n = getLeaf(SortedRing.NEXT, i);
		if (n != null) {
			insert(id, fingers, n);
		}
		n = getLeaf(SortedRing.PREV, i);
		if (n != null) {
			insert(id, fingers, n);
		}
	}
	PastryMessage request = getMsgRequest();
	prepareMessage(rnode, request, null);
	return request;
}

public Message prepareResponse(Node lnode, Node rnode, Message request)
{
	PastryMessage reply = getMsgReply();
	PastryMessage req = (PastryMessage) request;
	prepareMessage(rnode, reply, req.bitset);
	return reply;
}

private void prepareMessage(Node rnode, PastryMessage msg, BitSet bitset)
{
	for (int i = 0; i < p.nfingers; i++) {
		msg.bitset.set(i, fingers[i] == null);
	}
	long rid = getID(rnode);
	Node[] rfingers = new Node[p.nfingers];
	for (int i = 0; i < p.nfingers; i++) {
		if (fingers[i] != null)
			insert(rid, rfingers, fingers[i]);
	}
	int degree = leafDegree();
	for (int i = 0; i < degree; i++) {
		Node n = getLeaf(SortedRing.NEXT, i);
		if (n != null) {
			insert(rid, rfingers, n);
		}
		n = getLeaf(SortedRing.PREV, i);
		if (n != null) {
			insert(rid, rfingers, n);
		}
	}
	selectMessage(msg, rfingers, bitset);
}

private void selectMessage(PastryMessage msg, Node[] rfingers, BitSet bitset)
{
	if (bitset == null) {
		msg.size = 0;
		for (int i = rfingers.length - 1; i >= 0 && msg.size < p.msgsize; i--) {
			if (rfingers[i] != null)
				msg.nodes[msg.size++] = rfingers[i];
		}
	} else {
		msg.size = 0;
		for (int i = rfingers.length - 1; i >= 0 && msg.size < p.msgsize; i--) {
			if (rfingers[i] != null && bitset.get(i)) {
				// Is needed
				msg.nodes[msg.size++] = rfingers[i];
			}
		}
		for (int i = rfingers.length - 1; i >= 0 && msg.size < p.msgsize; i--) {
			if (rfingers[i] != null && !bitset.get(i)) {
				// It already has this, but we send it to give it a better chance
				msg.nodes[msg.size++] = rfingers[i];
			}
		}
	}
}

public void merge(Node lnode, Node rnode, Message message)
{
	PastryMessage msg = (PastryMessage) message;
	for (int i = 0; i < msg.size; i++) {
		insert(id, fingers, msg.nodes[i]);
	}
}

public long getID()
{
	return id;
}

public void setID(long key)
{
	id = key;
}

public int degree()
{
	return 0;
}

public Node getNeighbor(int i)
{
	return null;
}

public boolean addNeighbor(Node neighbour)
{
	insert(id, fingers, neighbour);
	return true;
}

public boolean contains(Node neighbor)
{
	return false;
}

public void pack()
{
}

public void onKill()
{
}

private void insert(long lid, Node[] f, Node n)
{
	long rid = getID(n);
	// System.out.println(idToString(lid));
	// System.out.println(idToString(rid));
	if (lid == rid)
		return;
	long common = rid ^ lid;
	int prefix = commonprefix(common);
	int digit = ((int) (rid >> (ID.BITS - (prefix + 1) * p.b))) & p.mask;
	// System.out.println(prefix + ": " + " " + digit);
	// XXX Handle latency
	f[prefix * p.radix + digit] = n;
}

private long getID(Node node)
{
	return ((IDHolder) node.getProtocol(p.pid)).getID();
}

private int commonprefix(long val)
{
	int common = p.digits;
	while (val > 0) {
		common--;
		val = val >> p.b;
	}
	return common;
}

/**
 * For this protocol to work with event-driven engines, this method must be
 * extended
 */
private PastryMessage getMsgRequest()
{
	if (p.event)
		return new PastryMessage(p.msgsize, p.nfingers);
	else
		return p.request;
}

// ---------------------------------------------------------------------
/**
 * For this protocol to work with event-driven engines, this method must be
 * extended
 */
private PastryMessage getMsgReply()
{
	if (p.event)
		return new PastryMessage(p.msgsize, p.nfingers);
	else
		return p.reply;
}
}
