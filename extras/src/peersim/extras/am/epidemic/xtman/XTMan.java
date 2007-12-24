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

package peersim.extras.am.epidemic.xtman;


import java.util.*;

import peersim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.epidemic.*;
import peersim.extras.am.epidemic.bcast.*;
import peersim.extras.am.epidemic.sorted.*;
import peersim.extras.am.id.*;
import peersim.extras.am.reports.*;
import peersim.extras.am.util.*;
import cern.colt.*;

/**
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class XTMan implements EpidemicProtocol, SortedRing, IDHolder, Linkable
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------

/** The distance function defining the topology */
private static final String PAR_DISTANCE = "distance";

/**
 * Ring degree. Each node is connected to {@value #PAR_DEGREE} succcessors
 * and {@value #PAR_DEGREE} predecessors.
 * @config
 */
private static final String PAR_DEGREE = "degree";

/**
 * Message size. A message contains {@value #PAR_MESSAGE} nodes. Defaults
 * to {@value #PAR_DEGREE}*2.
 * @config
 */
private static final String PAR_MESSAGE = "msgsize";

/**
 * Tabu list size. The tabu list is used when selecting a node, to avoid to
 * visit too early a node that has been visited recently.
 * @config
 */
private static final String PAR_TABU = "tabu";

/**
 * PSI
 * @config
 */
private static final String PAR_PSI = "psi";

/**
 * Initial capacity of the cache. Defaults to {@value #PAR_DEGREE}. Each
 * time the capacity is exhausted, the size is doubled.
 * @config
 */
private static final String PAR_CAPACITY = "capacity";

/**
 * If defined, the protocol will not start synchronously. It will start 
 * based on the result taken from Infectable protocol specified.
 */
private static final String PAR_INFECTABLE = "infectable";

/** 
 * Number of inactive cycles before stopping 
 */
private static final String PAR_STOP = "inactive";



// ---------------------------------------------------------------------
// Helper class
// ---------------------------------------------------------------------
/**
 * Helper class that contains protocol-specific values.
 */
final class ProtocolData
{

/** Identifier of the local protocol */
final int pid;

/** Distance function to be used */
final Distance dist;

/** Id comparator */
final Comparator idc;

/** Ring degree */
final int degree;

/** Message size */
final int msgsize;

/** Size of the tabu list */
final int tabusize;

/** Size of the selection window */
final int psi;

/** Initial capacity */
final int capacity;

/** Buffer */
final Node[] buffer;

/** Fixed request message */
final ViewMessage request;

/** Fixed reply message */
final ViewMessage reply;

/** If true, messages are generated at each request */
final boolean event;

/** Protocol id of the infectable protocol to be used for starting */
final int infectable;

/** Number of "stable" cycles before stopping */
final int stop;

ProtocolData(String prefix)
{
	pid = CommonState.getPid();
	dist = (Distance) Configuration.getInstance(prefix + "." + PAR_DISTANCE);
	idc = new IDNodeComparator(pid);
	degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
	msgsize = Configuration.getInt(prefix + "." + PAR_MESSAGE, degree * 2);
	tabusize = Configuration.getInt(prefix + "." + PAR_TABU, 4);
	psi = Configuration.getInt(prefix + "." + PAR_PSI, 10);
	capacity = Configuration.getInt(prefix + "." + PAR_CAPACITY, degree);
	event = (Simulator.getSimID() == Simulator.EDSIM);
	infectable = Configuration.getPid(prefix + "." + PAR_INFECTABLE, -1);
	stop = Configuration.getInt(prefix + "." + PAR_STOP, Integer.MAX_VALUE);
	buffer = new Node[262144];
	request = new ViewMessage(msgsize);
	reply = new ViewMessage(msgsize);
}
}

// ---------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------
/** Local identifier */
protected long id;

/** Cache containing all the received nodes */
protected Node[] cache;

/** Cache size */
protected int csize;

/** Tabu list */
protected Node[] tabu;

/** Current "writing" position in the tabu */
protected int tpos;

/** My current index in the cache */
protected int myindex;

/** References to my node; simplifies searching */
protected final Node mynode;

/** Protocol data common to all instances of a single protocol */
protected final ProtocolData p;

/**
 * Last time in which the node was active.
 */
protected long stopped;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
public XTMan(String prefix)
{
	p = new ProtocolData(prefix);
	mynode = CommonState.getNode();
	myindex = 0;
	tabu = new Node[p.tabusize];
	cache = new Node[p.capacity];
	cache[0] = mynode;
	tpos = 0;
	csize = 1;
	id = -1;
	stopped = -1;
}

// ---------------------------------------------------------------------
public XTMan(ProtocolData p)
{
	this.p = p;
	mynode = CommonState.getNode();
	myindex = 0;
	tabu = new Node[p.tabusize];
	cache = new Node[p.capacity];
	cache[0] = mynode;
	tpos = 0;
	csize = 1;
	id = -1;
	stopped = -1;
}

// ---------------------------------------------------------------------
@Override
public Object clone()
{
	return new XTMan(p);
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------

public boolean isActive()
{
	if (p.infectable < 0) {
		if (stopped < 0) 
			stopped = CommonState.getTime();
		return (CommonState.getTime() - stopped <= p.stop);
	} else {
		Infectable inf = (Infectable) mynode.getProtocol(p.infectable);
		if (!inf.isInfected())
			return false;
		if (stopped < 0) 
			stopped = CommonState.getTime();
		return (CommonState.getTime() - stopped <= p.stop);
	}
}

private void addTabu(Node n)
{
	tabu[tpos] = n;
	tpos = (tpos + 1) % tabu.length;
}

/** @inheritDoc */
public Node selectPeer(Node lnode)
{
	if (!isActive())
		return null;

	int s = p.dist.rank(cache, csize, lnode, p.buffer, p.psi);
	while (s > 0) {
		int r = CommonState.r.nextInt(s);
		if (!contains(tabu, p.buffer[r])) {
			addTabu(p.buffer[r]);
			return p.buffer[r];
		} else {
			p.buffer[r] = p.buffer[--s];
		}
	}
//	int s = p.dist.rank(cache, csize, lnode, p.buffer, p.tabusize+2);
//	for (int i = 0; i < s + 1; i++) {
//		if (!contains(tabu, p.buffer[i])) {
//			addTabu(p.buffer[i]);
//			return p.buffer[i];
//		}
//	}

	throw new RuntimeException("This should never be reached");
}

// ---------------------------------------------------------------------
private boolean contains(Object[] objs, Object obj)
{
	int size = objs.length;
	for (int i = 0; i < size; i++)
		if (objs[i] == obj)
			return true;
	return false;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public ViewMessage prepareRequest(Node lnode, Node rnode)
{
	StatsObserver.add("REQUEST", 1);
	ViewMessage request = getMsgRequest();
	request.size = p.dist.rank(cache, csize, rnode, request.nodes, p.msgsize);
	Sorting.quickSort(request.nodes, 0, request.size, p.idc);
	return request;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public ViewMessage prepareResponse(Node lnode, Node rnode, Message msg)
{
	if (p.infectable >= 0) {
		Infectable inf = (Infectable) lnode.getProtocol(p.infectable);
		inf.setInfected(true);
	}

	addTabu(rnode);

	ViewMessage reply = getMsgReply();
	reply.size = p.dist.rank(cache, csize, rnode, reply.nodes, p.msgsize);
	Sorting.quickSort(reply.nodes, 0, reply.size, p.idc);
	return reply;
}

// ---------------------------------------------------------------------

/** @inheritDoc */
public void merge(Node lnode, Node rnode, Message msg)
{
	// System.out.println(lnode.getID() + " merging from " +
	// msg.getSender().getID() + " " +
	// CommonState.getTime());
	ViewMessage message = (ViewMessage) msg;

	int oldcsize = csize;
	csize = p.dist.merge(cache, csize, message.nodes, message.size, p.buffer);
	if (csize != oldcsize)
		stopped = CommonState.getTime();
	// Copy buffer in local cache
	if (csize > cache.length) {
		cache = new Node[Math.max(cache.length * 2, csize)];
	}
	System.arraycopy(p.buffer, 0, cache, 0, csize);
	myindex = Sorting.binarySearchFromTo(cache, mynode, 0, csize, p.idc);
}

// ---------------------------------------------------------------------
/**
 * When run in a cycle-based simulator, it uses a singleton message to save
 * memory. In an event-based simulator, it creates a new message each time
 * is run.
 */
private ViewMessage getMsgRequest()
{
	if (p.event)
		return new ViewMessage(p.msgsize);
	else
		return p.request;
}

// ---------------------------------------------------------------------
/**
 * When run in a cycle-based simulator, it uses a singleton message to save
 * memory. In an event-based simulator, it creates a new message each time
 * is run.
 */
private ViewMessage getMsgReply()
{
	if (p.event)
		return new ViewMessage(p.msgsize);
	else
		return p.reply;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public long getID()
{
	return id;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void setID(long id)
{
	this.id = id;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public int degree()
{
	return csize;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getNeighbor(int i)
{
	return cache[i];
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public boolean addNeighbor(Node neighbour)
{
	if (csize < cache.length) {
		cache[csize++] = neighbour;
		return true;
	} else
		return false;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public boolean contains(Node neighbor)
{
	for (int i = 0; i < csize; i++) {
		if (cache[i] == neighbor)
			return true;
	}
	return false;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void pack()
{
	Sorting.quickSort(cache, 0, csize, p.idc);
	csize = ArrayUtil.removeDups(cache, csize);
	for (int i = 0; i < csize; i++) {
		if (cache[i] == mynode) {
			myindex = i;
			return;
		}
	}
	throw new Error("Bug");
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void onKill()
{
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getLeaf(int dir, int index)
{
	assert dir == NEXT || dir == PREV;
	return cache[(myindex + dir * (index + 1) + csize) % csize];
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void setLeaf(int dir, int index, Node node)
{
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public int leafDegree()
{
	return p.degree;
}

// ---------------------------------------------------------------------
/**
 * Returns a string containint the id of this node.
 */
@Override
public String toString()
{
	return "" + id;
}
// ---------------------------------------------------------------------

}
