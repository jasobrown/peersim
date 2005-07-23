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
		
package peersim.graph;

import java.util.Vector;
import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Collections;

/**
* Implements a graph which uses the neighbour list representation.
* No multiple edges are allowed.
*/
public class NeighbourListGraph implements Graph, java.io.Serializable {

// =================== private fields ============================
// ===============================================================

/** Contains the objects associated with the node indeces.*/
private final Vector nodes;

/**
* Contains the indices of the nodes. The vector "nodes" contains this
* information implicitly but this way we can find indeces in log time at
* the cost of memory (node that the edge lists typically use much more memory
* than this anyway). Note that the nodes vector is still necessary to
* provide constant acces to nodes based on indeces.
*/
private final Hashtable nodeindex;

/** Contains sets of node indeces (as defined by "nodes"). */
private final Vector neighbours;

/** Indicates if the graph is directed. */
private final boolean directed;

// =================== public constructors ======================
// ===============================================================

/**
* Constructs an empty graph.
* @param directed if true the graph will be directed
*/
public NeighbourListGraph( boolean directed ) {

	nodes = new Vector(1000,1000);	
	neighbours = new Vector(1000,1000);
	nodeindex = new Hashtable(1000);
	this.directed = directed;
}

// =================== public methods =============================
// ================================================================

/**
* If the given object is not associated with a node yet, adds a new
* node. Returns the index of the node.
*/
public int addNode( Object o ) {

	Object index = nodeindex.get(o);
	if( index == null )
	{
		index = new Integer(nodes.size());
		nodes.add(o);
		neighbours.add(new HashSet());
		nodeindex.put(o,index);
	}

	return ((Integer)index).intValue();
}


// =================== graph implementations ======================
// ================================================================


public boolean setEdge( int i, int j ) {
	
	boolean ret = ((Set)neighbours.get(i)).add(new Integer(j));
	if( ret && !directed ) ((Set)neighbours.get(j)).add(new Integer(i));
	return ret;
}

// ---------------------------------------------------------------

public boolean clearEdge( int i, int j ) {
	
	boolean ret = ((Set)neighbours.get(i)).remove(new Integer(j));
	if( ret && !directed ) ((Set)neighbours.get(j)).remove(new Integer(i));
	return ret;
}

// ---------------------------------------------------------------

public boolean isEdge(int i, int j) {
	
	return ((Set)neighbours.get(i)).contains(new Integer(j));
}

// ---------------------------------------------------------------

public Collection getNeighbours(int i) {
	
	return Collections.unmodifiableCollection((Set)neighbours.get(i));
}

// ---------------------------------------------------------------

public Object getNode(int i) { return nodes.get(i); }
	
// ---------------------------------------------------------------

/**
* Returns null always. 
*/
public Object getEdge(int i, int j) { return null; }

// ---------------------------------------------------------------

public int size() { return nodes.size(); }

// --------------------------------------------------------------------
	
public boolean directed() { return directed; }

// --------------------------------------------------------------------

public int degree(int i) { return ((Set)neighbours.get(i)).size(); }
}




