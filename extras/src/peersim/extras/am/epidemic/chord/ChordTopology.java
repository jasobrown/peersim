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
import java.util.Arrays;

import peersim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.epidemic.*;
import peersim.extras.am.epidemic.bcast.*;
import peersim.extras.am.epidemic.sorted.*;
import peersim.extras.am.epidemic.xtman.*;
import peersim.extras.am.id.*;
import peersim.extras.am.util.*;
import cern.colt.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ChordTopology
		implements
			EpidemicProtocol,
			IDHolder,
			SortedRing,
			Chord,
			Linkable
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------

/**
 * Degree of the underlying ring. Each node is connected to
 * <code>degree</code> predecessors and <code>degree</code> successors.
 * @config
 */
private static final String PAR_DEGREE = "degree";

/** 
 * Number of leafs in messages 
 * @config
 */
private static final String PAR_LDEGREE = "msg.leafs";

/** 
 * Number of fingers in messages
 * @config
 */
private static final String PAR_FDEGREE = "msg.fingers";

/**
 * If defined, the protocol will not start synchronously. It will start
 * based on the result taken from Infectable protocol specified.
 * @config
 */
private static final String PAR_INFECTABLE = "infectable";

/**
 * If a node does not receive any new node identifier for a period
 * of time equal to the value of this parameter, it stops to be
 * active.
 * @confi
 */
private static final String PAR_STOP = "inactive";

/**
 * Tabu list size. The tabu list is used when selecting a node, to avoid to
 * visit too early a node that has been visited recently.
 * @config
 */
private static final String PAR_TABU = "tabu";


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

/** Id comparator */
final Comparator idc;

/** Degree of the underlying ring */
final int ringDegree;

/** Leaf degree in messages */
final int lmsgdegree;

/** Finger degree in messages */
final int fmsgdegree;

/** Size of the selection window */
final int tabusize;

/** Temporary buffer used for merging */
final Node[] buffer;

/** Temporary buffer used for computing fingers */
final Node[][] fingers;

/** Singleton request message */
final ViewMessage request;

/** Singleton reply message */
final ViewMessage reply;

/** If true, messages are generated at each request */
final boolean event;

/** Protocol id of the infectable protocol used for starting */
final int infectable;

/** Number of "stable" cycles before stopping */
final int stop;


ProtocolData(String prefix)
{
	pid = CommonState.getPid();
	idc = new IDNodeComparator(pid);
	ringDegree = Configuration.getInt(prefix + "." + PAR_DEGREE);
	lmsgdegree = Configuration.getInt(prefix + "." + PAR_LDEGREE);
	fmsgdegree = Configuration.getInt(prefix + "." + PAR_FDEGREE);
	tabusize = Configuration.getInt(prefix + "." + PAR_TABU);
	event = (Simulator.getSimID() == Simulator.EDSIM);
	fingers = new Node[2][];
	fingers[0] = new Node[ID.BITS];
	fingers[1] = new Node[ID.BITS];
	buffer = new Node[ringDegree * 2 + ID.BITS * 2];
	request = new ViewMessage(lmsgdegree * 2 + fmsgdegree * 2);
	reply = new ViewMessage(lmsgdegree * 2 + fmsgdegree * 2);
	infectable = Configuration.getPid(prefix + "." + PAR_INFECTABLE, -1);
	stop = Configuration.getInt(prefix + "." + PAR_STOP, Integer.MAX_VALUE);
}
} // END ProtocolData

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------

/** References to my node; simplifies searching */
protected Node mynode;

/** Protocol-specific parameter */
protected ProtocolData p;

/** Identifier of this node */
protected long id;

/** Leafs */
protected Node[] leafs;

/** Number of leafs */
protected int nleafs;

/** Fingers */
protected Node[][] fingers;

/** Tabu list */
protected Node[] tabu;

/** Current "writing" position in the tabu */
protected int tpos;

/** My current index in the cache */
protected int myindex;

/** Last time at which the node has updated the local view */
protected long lastUpdate;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * @param prefix
 */
public ChordTopology(String prefix)
{
	init(new ProtocolData(prefix));
}

/**
 * @param p
 */
public ChordTopology(ProtocolData p)
{
  init(p);
}

private void init(ProtocolData p)
{
	this.p = p;
	mynode = CommonState.getNode();
	id = -1;
	leafs = new Node[p.ringDegree * 2 + 1];
	tabu = new Node[p.tabusize];
	fingers = new Node[2][];
	fingers[0] = new Node[ID.BITS];
	fingers[1] = new Node[ID.BITS];
	myindex = 0;
	leafs[0] = mynode;
	nleafs = 1;
	lastUpdate = -1;
}

// ---------------------------------------------------------------------
@Override
public Object clone()
{
	return new ChordTopology(p);
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------

public boolean isActive()
{
	if (p.infectable < 0) {
		if (lastUpdate < 0) 
			lastUpdate = CommonState.getTime();
		return (CommonState.getTime() - lastUpdate <= p.stop);
	} else {
		Infectable inf = (Infectable) mynode.getProtocol(p.infectable);
		if (!inf.isInfected())
			return false;
		if (lastUpdate < 0) 
			lastUpdate = CommonState.getTime();
		return (CommonState.getTime() - lastUpdate <= p.stop);
	}
}

private void addTabu(Node n)
{
	tabu[tpos] = n;
	tpos = (tpos + 1) % tabu.length;
}

public Node selectPeer(Node lnode)
{
	if (!isActive())
		return null;
	if (lastUpdate == 0)
		lastUpdate = CommonState.getTime();
	int size = 0;
	for (int i = 1; i <= p.tabusize+2; i++) {
		p.buffer[size++] = leafs[(myindex - i + nleafs) % nleafs];
		p.buffer[size++] = leafs[(myindex + i) % nleafs];
	}
	while (size > 0) {
		int r = CommonState.r.nextInt(size);
		if (!contains(tabu, p.buffer[r])) {
			addTabu(p.buffer[r]);
			return p.buffer[r];
		}
		p.buffer[r] = p.buffer[--size];
	}
  return null;	
}

public ViewMessage prepareRequest(Node lnode, Node rnode)
{
	ViewMessage request = getMsgRequest();
	prepareMessage(request, rnode);
	return request;
}

public ViewMessage prepareResponse(Node lnode, Node rnode, Message msg)
{
	ViewMessage reply = getMsgReply();
	prepareMessage(reply, rnode);
	return reply;
}

private void prepareMessage(ViewMessage msg, Node rnode)
{
	System.arraycopy(leafs, 0, p.buffer, 0, nleafs);
	int bsize = nleafs;
	for (int i = 0; i < ID.BITS; i++) {
		if (fingers[0][i] != null)
			p.buffer[bsize++] = fingers[0][i];
		if (fingers[1][i] != null)
			p.buffer[bsize++] = fingers[1][i];
	}
	Arrays.sort(p.buffer, 0, bsize, p.idc);
	bsize = ArrayUtil.removeDups(p.buffer, bsize);
	bsize = ChordLibrary.extract(msg.nodes, p.lmsgdegree, p.buffer, bsize, rnode,
			p.idc);
	long rid = IDUtil.getID(rnode, p.pid);
	// Compute next fingers
	ChordLibrary.resetFingers(p.fingers[0]);
	ChordLibrary.selectFingers(leafs, nleafs, p.fingers[0], rnode, rid, p.pid,
			SortedRing.NEXT, null);
	// Bug correction; nleafs substituted by fingers[i].length
	// Thanks to Zhang Hao
	ChordLibrary.selectFingers(fingers[0], fingers[0].length, p.fingers[0], rnode, rid,
			p.pid, SortedRing.NEXT, null);
	ChordLibrary.selectFingers(fingers[1], fingers[1].length, p.fingers[0], rnode, rid,
			p.pid, SortedRing.NEXT, null);
	// Compute prev fingers
	ChordLibrary.resetFingers(p.fingers[1]);
	ChordLibrary.selectFingers(leafs, nleafs, p.fingers[1], rnode, rid, p.pid,
			SortedRing.PREV, null);
	// Bug correction; nleafs substituted by fingers[i].length
	// Thanks to Zhang Hao
	ChordLibrary.selectFingers(fingers[0], fingers[0].length, p.fingers[1], rnode, rid,
			p.pid, SortedRing.PREV, null);
	ChordLibrary.selectFingers(fingers[1], fingers[0].length, p.fingers[1], rnode, rid,
			p.pid, SortedRing.PREV, null);
	int size = 0;

	for (int i = 0; i < ID.BITS && size < p.fmsgdegree; i++) {
		if (p.fingers[0][i] != null)
			msg.nodes[bsize + size++] = p.fingers[0][i];
	}
	for (int i = 0; i < ID.BITS && size < p.fmsgdegree * 2; i++) {
		if (p.fingers[1][i] != null)
			msg.nodes[bsize + size++] = p.fingers[1][i];
	}
	msg.size = bsize + size;
}

public void merge(Node lnode, Node rnode, Message message)
{
	ViewMessage msg = (ViewMessage) message;
	long checksum = 0;
	for (int i=0; i < nleafs; i++) {
		checksum += IDUtil.getID(leafs[i], p.pid); 
	}
	
	// Merge leafs
	System.arraycopy(msg.nodes, 0, p.buffer, 0, msg.size);
	System.arraycopy(leafs, 0, p.buffer, msg.size, nleafs);
	int size = msg.size + nleafs;
	Arrays.sort(p.buffer, 0, size, p.idc);
	size = ArrayUtil.removeDups(p.buffer, size);
	int pos = Sorting.binarySearchFromTo(p.buffer, mynode, 0, size - 1, p.idc);
	nleafs = 0;
	for (int i = 1; i <= p.ringDegree; i++) {
		leafs[nleafs++] = p.buffer[(pos + i) % size];
		leafs[nleafs++] = p.buffer[(pos - i + size) % size];
	}
	leafs[nleafs++] = mynode;
	Arrays.sort(leafs, 0, nleafs, p.idc);
	nleafs = ArrayUtil.removeDups(leafs, nleafs);
	myindex = Sorting.binarySearchFromTo(leafs, mynode, 0, nleafs - 1, p.idc);

	long nchecksum = 0;
	for (int i=0; i < nleafs; i++) {
		nchecksum += IDUtil.getID(leafs[i], p.pid); 
	}
	
	if (checksum != nchecksum) {
  	lastUpdate = CommonState.getTime();
  }
	
	checksum = 0;
	for (int i=0; i < fingers[0].length; i++) {
		if (fingers[0][i] != null) 
			checksum += IDUtil.getID(fingers[0][i], p.pid); 
		if (fingers[1][i] != null) 
			checksum += IDUtil.getID(fingers[1][i], p.pid); 
	}

	// Improves fingers
	ChordLibrary.selectFingers(msg.nodes, msg.size, fingers[0], mynode, id,
			p.pid, SortedRing.NEXT, null);
	ChordLibrary.selectFingers(msg.nodes, msg.size, fingers[1], mynode, id,
			p.pid, SortedRing.PREV, null);

	nchecksum = 0;
	for (int i=0; i < fingers[0].length; i++) {
		if (fingers[0][i] != null) 
			nchecksum += IDUtil.getID(fingers[0][i], p.pid); 
		if (fingers[1][i] != null) 
			nchecksum += IDUtil.getID(fingers[1][i], p.pid); 
	}
	
	
	if (checksum != nchecksum) {
  	lastUpdate = CommonState.getTime();
  }

}

// ---------------------------------------------------------------------
/**
 * If the protocol is executed in the event-based engine, returns a fresh
 * new message each time. Otherwise, use the single instance stored in the
 * ProtocolData structure.
 */
private ViewMessage getMsgRequest()
{
	if (p.event)
		return new ViewMessage(p.lmsgdegree * 2 + p.fmsgdegree * 2);
	else
		return p.request;
}

// ---------------------------------------------------------------------
/**
 * If the protocol is executed in the event-based engine, returns a fresh
 * new message each time. Otherwise, use the single instance stored in the
 * ProtocolData structure.
 */
private ViewMessage getMsgReply()
{
	if (p.event)
		return new ViewMessage(p.lmsgdegree * 2 + p.fmsgdegree * 2);
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
	return nleafs;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getNeighbor(int i)
{
	return leafs[i];
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public boolean addNeighbor(Node neighbour)
{
	if (nleafs < leafs.length) {
		leafs[nleafs++] = neighbour;
		return true;
	} else
		return false;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public boolean contains(Node neighbor)
{
	for (int i = 0; i < nleafs; i++) {
		if (leafs[i] == neighbor)
			return true;
	}
	return false;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void pack()
{
	Arrays.sort(leafs, 0, nleafs, p.idc);
	nleafs = ArrayUtil.removeDups(leafs, nleafs);
	ChordLibrary.selectFingers(leafs, nleafs, fingers[0], mynode, id, p.pid,
			SortedRing.NEXT, null);
	ChordLibrary.selectFingers(leafs, nleafs, fingers[1], mynode, id, p.pid,
			SortedRing.PREV, null);
	for (int i = 0; i < nleafs; i++) {
		if (leafs[i] == mynode) {
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
	return leafs[(myindex + dir * (index + 1) + nleafs) % nleafs];
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
	return nleafs / 2;
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getSuccessor(int index)
{
	return leafs[(myindex + NEXT * (index + 1) + nleafs) % nleafs];
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void setSuccessor(int index, Node node)
{
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public Node getFinger(int index)
{
	return fingers[0][index];
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public void setFinger(int index, Node node)
{
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------------
/** @inheritDoc */
public int successors()
{
	return p.ringDegree;
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

}