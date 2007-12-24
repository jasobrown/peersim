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

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * This class is idle protocol implementing the {@link Pastry} interface.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class IdlePastry implements Protocol, Pastry
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------
/**
 * The identifier of the protocol holding ids
 * @config
 */
private static final String PAR_HOLDER = "holder";

/**
 * Number of leafs
 * @config
 */
private static final String PAR_DEGREE = "degree";

/**
 * Parameter b of Pastry
 */
private static final String PAR_B = "b";

// ---------------------------------------------------------------------
// Protocol data
// ---------------------------------------------------------------------
/**
 * Helper class with protocol parameters. There is a single instance of this
 * class per node.
 */
private class ProtocolData
{

/** Host identifier */
private final int hid;

/** b parameter of pastry */
private final int b;

/** radix = 2^b */
private final int radix;

/** bit mask */
private final int mask;

/** number of rows in the routing table */
private final int digits;

/** size of the routing table */
private final int size;

/** protocol identifier */
private final int degree;

ProtocolData(String prefix)
{
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER);
	degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
	b = Configuration.getInt(prefix + "." + PAR_B);
	if ((ID.BITS / b) * b != ID.BITS) {
		System.err.println("Configuration error: ID.BITS not multiple of b");
		System.exit(1);
	}
	radix = 1 << b;
	mask = radix - 1;
	digits = ID.BITS / b;
	size = digits * radix;
}
}

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------
/** Reference to the protocol data */
private final ProtocolData p;

/** Local node */
private final Node mynode;

/** Fingers */
private final Node[] fingers;

/** Leafs */
private final Node[][] leafs;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * Initializes an empty Chord node.
 */
public IdlePastry(String prefix)
{
	p = new ProtocolData(prefix);
	mynode = CommonState.getNode();
	fingers = new Node[p.size];
	leafs = new Node[2][];
	leafs[0] = new Node[p.degree];
	leafs[1] = new Node[p.degree];
}

public IdlePastry(ProtocolData p)
{
	this.p = p;
	mynode = CommonState.getNode();
	fingers = new Node[p.size];
	leafs = new Node[2][];
	leafs[0] = new Node[p.degree];
	leafs[1] = new Node[p.degree];
}

/**
 * Clone an empty Chord node.
 */
public Object clone()
{
	return new IdlePastry(p);
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------
public int radix()
{
	return p.radix;
}

public int b()
{
	return p.b;
}

public int digits()
{
	return p.digits;
}

public Node getFinger(int common, int index)
{
	assert index >= 0 && index < p.radix;
	assert common >= 0 && common < p.digits;
	int k = common * p.radix + index;
	return fingers[k];
}

public void setFinger(int common, int index, Node node)
{
	assert index >= 0 && index < p.radix;
	assert common >= 0 && common < p.digits;
	fingers[common * p.radix + index] = node;
}

public void addFinger(Node n)
{
	long rid = getID(n);
	long lid = getID(mynode);
	if (lid == rid)
		return;
	long common = rid ^ lid;
	int prefix = commonprefix(common);
	int digit = ((int) (rid >> (ID.BITS - (prefix + 1) * p.b))) & p.mask;
	// System.out.println(prefix + ": " + " " + digit);
	// XXX Handle latency
	setFinger(prefix, digit, n);
}

public Node getLeaf(int dir, int i)
{
	if (dir == NEXT)
		return leafs[0][i];
	else
		return leafs[1][i];
}

public void setLeaf(int dir, int i, Node node)
{
	if (dir == NEXT)
		leafs[0][i] = node;
	else
		leafs[1][i] = node;
}

public int leafDegree()
{
	return p.degree;
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

/** Utility method */
private long getID(Node node)
{
	return ((IDHolder) node.getProtocol(p.hid)).getID();
}
}
