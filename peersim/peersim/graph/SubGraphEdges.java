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
		
package peersim.graph;

import java.util.*;

/**
* This class is an adaptor for representing subgraphs of any graph.
* The node set will remain the same as for the original graph however, only
* the edges are removed which have one of our ends outside the subgraph.
* This is to keep the contract of the original graph interface which
* says that the nodes are indexed with 0,...,size-1, and we don't reindex
* the nodes. The function size() return the original size accordingly.
*/
public class SubGraphEdges implements Graph {


// ====================== private fileds ========================
// ==============================================================


private final Graph g;

private final BitSet nodes;

private int nodesSize = 0; // since 1.4 there is cardinality for bitsets


// ====================== public constructors ===================
// ==============================================================


/**
* Constructs an initially empty subgraph of g. That is, the subgrpah will
* contain no nodes.
*/
public SubGraphEdges( Graph g ) {

	this.g = g;
	nodes = new BitSet(g.size());
}


// ======================= Graph implementations ================
// ==============================================================


public boolean isEdge(int i, int j) {
	
	return nodes.get(i) && nodes.get(j) && g.isEdge(i,j);
}

// ---------------------------------------------------------------

public Collection getNeighbours(int i) {
	
	List result = new LinkedList();
	if( nodes.get(i) )
	{
		Iterator it = g.getNeighbours(i).iterator();
		while(it.hasNext())
		{
			Integer in = (Integer)it.next();
			if( nodes.get( in.intValue() ) ) result.add(in);
		}
	}

	return Collections.unmodifiableCollection(result);
}

// ---------------------------------------------------------------

public Object getNode(int i) { return g.getNode(i); }
	
// ---------------------------------------------------------------

/**
* If both i and j are within the node set of the subgraph and the original
* graph has an (i,j) edge, returns that edge.
*/
public Object getEdge(int i, int j) {
	
	if( isEdge(i,j) ) return g.getEdge(i,j);
	return null;
}

// --------------------------------------------------------------------

/**
* Returns the original size of the graph, not the subgraph. This is to
* maintain the specification of the Graph interface. (Note that this
* subgraph still contains all nodes a=only removes edges.)
*/
public int size() { return g.size(); }

// --------------------------------------------------------------------
	
public boolean directed() { return g.directed(); }

// --------------------------------------------------------------------

public boolean setEdge( int i, int j ) {
	
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------

public boolean clearEdge( int i, int j ) {
	
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------

public int degree(int i) {

	if( nodes.get(i) ) return g.degree(i);
	else return 0;
}


// ================= public functions =================================
// ====================================================================


/**
* This function returns the size of the subgraph, ie the number of nodes
* in the subgraph.
*/
public int subGraphSize() { return nodesSize; }

// --------------------------------------------------------------------

/**
* Removes given node from subgraph.
* @return true if the node was already in the subgraph otherwise false.
*/
public boolean removeNode(int i) {
	
	boolean was = nodes.get(i);
	nodes.clear(i);
	if( was ) --nodesSize;
	return was;
}

// --------------------------------------------------------------------

/**
* Adds given node to subgraph.
* @return true if the node was already in the subgraph otherwise false.
*/
public boolean addNode(int i) {
	
	boolean was = nodes.get(i);
	nodes.set(i);
	if( !was ) ++nodesSize;
	return was;
}
}

