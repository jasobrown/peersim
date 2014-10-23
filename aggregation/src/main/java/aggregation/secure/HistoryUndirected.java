/*
 * Copyright (c) 2003 The BISON Project
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

package aggregation.secure;

import peersim.core.*;
import peersim.config.*;

/**
 * Implementation of the {@link History} interface. A random node is 
 * selected among those that have initiated an exchange with the node 
 * that stores this history. Its history is compared with the history
 * of a node that contacted it.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class HistoryUndirected
implements Protocol, History
{

//--------------------------------------------------------------------------
// Parameters name
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to determine the size of the
 * table containing exchanges (initiated or received). Defaults to 50.
 */
public static final String PAR_SIZE = "size";

/** 
 * String name of the parameter used to determine the size of the
 * table containing exchanges that have been initiated. Defaults to
 * size.
 */
public static final String PAR_RSIZE = "received.size";

/** 
 * String name of the parameter used to determine the size of the
 * table containing exchanges that have been initiated. Defaults to
 * size.
 */
public static final String PAR_ISIZE = "initiated.size";


//--------------------------------------------------------------------------
// Table of received exchanges
//--------------------------------------------------------------------------

/** Nodes from which this node has received exchanges */
Node[] snodes;

/** Cycles at which the exchanges have occurred */  
int[] scycles;

/** Values that have been received in the exchanges */
double[] svalues;

/** Position at which to insert the next received exchange */
int spos;


//--------------------------------------------------------------------------
// Table of generated exchanges
//--------------------------------------------------------------------------

/** Nodes with which this node has started exchanges */
Node[] dnodes;

/** Cycles at which the exchanges have occurred */  
int[] dcycles;

/** Values that have been sent in the exchanges */
double[] dvalues;

/** Position at which to insert the next received exchange */
int dpos;


//--------------------------------------------------------------------------
// Static variables
//--------------------------------------------------------------------------

/**
 * Singleton array used to contain the return array of method
 * <tt>checkRandomNode()</tt>.
 */
static Node[] nodes = new Node[2];


//--------------------------------------------------------------------------
// Constructors
//--------------------------------------------------------------------------

/**
 * Creates the tables containing the initiated and received history.
 * The sizes of the tables are read from the configuration file.
 */
public HistoryUndirected(String prefix)
{
	int size = Configuration.getInt(prefix + "." + PAR_RSIZE, 50);
	int rsize = Configuration.getInt(prefix + "." + PAR_RSIZE, size);
	int isize = Configuration.getInt(prefix + "." + PAR_ISIZE, size);
	snodes = new Node[rsize];
	svalues = new double[rsize];
	scycles = new int[rsize];
	dnodes = new Node[isize];
	dvalues = new double[isize];
	dcycles = new int[isize];
	spos = 0;
	dpos = 0;
}

//--------------------------------------------------------------------------

/**
 * Creates the tables containing the initiated and received history.
 * The sizes are cloned by the original object.
 */
public Object clone()
{
	HistoryDirected h = null;
	try { h =(HistoryDirected) super.clone(); }
	catch( CloneNotSupportedException e ) {} // never happens
	h.snodes = new Node[snodes.length];
	h.svalues = new double[snodes.length];
	h.scycles = new int[snodes.length];
	h.dnodes = new Node[dnodes.length];
	h.dvalues = new double[dnodes.length];
	h.dcycles = new int[dnodes.length];
	h.spos = 0;
	h.dpos = 0;
	return h;
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

//Comment inherited from interface
public void addReceived(Node source, double value, int cycle)
{
	snodes[spos] = source;
	scycles[spos] = cycle;
	svalues[spos] = (double) value;
	spos = (spos + 1) % snodes.length;
}
	
//--------------------------------------------------------------------------

//Comment inherited from interface
public void addInitiated(Node destination, double value, int cycle)
{
	dnodes[dpos] = destination;
	dcycles[dpos] = cycle;
	dvalues[dpos] = (double) value;
	dpos = (dpos + 1) % dnodes.length;
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public Node[] checkRandomNode(Node rnode, int hid)
{
	int slen, dlen;
	slen = (snodes[spos] == null ? spos : snodes.length);
	if (slen == 0)
		return null;
	int k = CommonState.r.nextInt(slen);
	Node random1 = snodes[k];
	HistoryUndirected history1 = (HistoryUndirected) random1.getProtocol(hid);
	slen = (history1.snodes[history1.spos] == null ? history1.spos : history1.snodes.length);
  if (slen == 0)
    return null;
	k = CommonState.r.nextInt(slen);
	Node random2 = history1.snodes[k];
	HistoryUndirected history2 = (HistoryUndirected) random2.getProtocol(hid);
    
	dlen = (history2.dnodes[history2.dpos] == null ? history2.dpos : history2.dnodes.length);
	for (int i=0; i < dlen; i++) {
		if (history2.dnodes[i] == random1 && 
		    history2.dvalues[i] == history1.svalues[k] &&
		    history2.dcycles[i] == history1.scycles[k]) {
			return null;
    }
	}
	
	nodes[0] = random1;
	nodes[1] = random2;
	return nodes;
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void reset()
{
	for (int i=0; i < snodes.length; i++) {
		snodes[i] = null;
	}
	spos = 0;
	for (int i=0; i < dnodes.length; i++) {
		dnodes[i] = null;
	}
	dpos = 0;
}

//--------------------------------------------------------------------------

}
