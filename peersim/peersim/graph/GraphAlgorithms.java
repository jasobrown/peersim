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
* Implements graph algorithms. The current implementation is NOT thread
* safe. Some algorithms are not static, many times the result of an
* algorithm can be read from fileds of the GraphAlgorithm object.
*/
public class GraphAlgorithms {

// =================== public fields ==================================
// ====================================================================


public final static int WHITE=0;
public final static int GREY=1;
public final static int BLACK=2;

/** working area for the dfs algorithm */
public int[] color = null;

/** working variable for the dfs algorithm */
public Set cluster = null;

public int[] d = null;

// =================== private methods ================================
// ====================================================================


/**
* Collects accessible nodes form node "from" using a depth-first search.
* Works on the array {@link #color} which must be of the same length as
* the size of the graph
* and must contain values according to the following semantics: 
* WHITE (0): not seen yet, GREY (1): currently worked upon. BLACK
* (not 0 or 1): finished.
* If meets a negatie color saves it in the set {@link #cluster} and treats it
* as black. 
* This can be used to check if the cluster currently searched is weakly
* connected to another cluster.
* On exit no nodes are GREY.
* The result is the modified array {@link #color} and the modified set
* {@link #cluster}.
*/
private void dfs( Graph g, int from ) {

	color[from]=GREY;

	Iterator it=g.getNeighbours(from).iterator();
	while( it.hasNext() )
	{
		int j = ((Integer)it.next()).intValue();
		if( color[j]==WHITE )
		{
			dfs(g,j);
		}
		else
		{
			if( color[j]<0 ) cluster.add(new Integer(color[j]));
		}
	}

	color[from]=BLACK;
}

// --------------------------------------------------------------------

/**
* Collects accessible nodes form node "from" using a breadth-first search.
* Its parameters and side-effects are identical to those of dfs.
* In addition, it stores the shortest
* distances from "from" in {@link #d}, if it is not null,
* i.e. <code>d[i]</code> is the
* length of the shortest path from "from" to i. d must be long enough or must
* be null.
*/
private void bfs( Graph g, int from ) {

	List q = new LinkedList();
	int u, du;
	
	q.add( new Integer(from) );
	q.add( new Integer(0) );
	
	color[from]=GREY;

	while( ! q.isEmpty() )
	{
		u = ((Integer)q.remove(0)).intValue();
		du = ((Integer)q.remove(0)).intValue();
		
		Iterator it=g.getNeighbours(u).iterator();
		while( it.hasNext() )
		{
			Integer j = (Integer)it.next();
			final int jj = j.intValue();
			if( color[jj]==WHITE )
			{
				color[jj]=GREY;
				
				q.add(j);
				if( d != null ) d[jj] = du+1;
				q.add(new Integer(du+1));
			}
			else
			{
				if( color[jj]<0 )
					cluster.add(new Integer(color[jj]));
			}
		}
		color[u]=BLACK;
	}
}

// --------------------------------------------------------------------

/** Returns the weakly connected cluster indexes with size as a value.
* Cluster membership can be seen from the content of the array {@link #color};
* each node has the cluster index as color.
*/
public Map weaklyConnectedClusters( Graph g ) {

	if( cluster == null ) cluster = new HashSet();
	if( color==null || color.length<g.size() ) color = new int[g.size()];

	// cluster numbers are negative integers
	int i, j, actCluster=0;
	for(i=0; i<g.size(); ++i) color[i]=WHITE;
	for(i=0; i<g.size(); ++i)
	{
		if( color[i]==WHITE )
		{
			cluster.clear();
			bfs(g,i); // dfs is recursive, for large graphs not ok
			--actCluster;
			for(j=0; j<g.size(); ++j)
			{
				if( color[j] == BLACK ||
				cluster.contains(new Integer(color[j])) )
					color[j] = actCluster;
			}
		}
	}

	Hashtable ht = new Hashtable();
	Integer one = new Integer(1);
	for(j=0; j<g.size(); ++j)
	{
		Integer in = new Integer(color[j]);
		Integer num = (Integer)ht.get(in);
		if( num == null ) ht.put(in,one);
		else ht.put(in,new Integer(num.intValue()+1));
	}
	
	return ht;
}

// --------------------------------------------------------------------

/**
* In <code>d[j]</code> returns the length of the shortest path between
* i and j. The value -1 indicates that j is not accessible from i.
* @see #d
*/
public void dist( Graph g, int i ) {

	if( d==null || d.length<g.size() ) d = new int[g.size()];
	if( color==null || color.length<g.size() ) color = new int[g.size()];
	
	for(int j=0; j<g.size(); ++j)
	{
		color[j]=WHITE;
		d[j] = -1;
	}
	
	bfs( g, i );
}

// --------------------------------------------------------------------

/**
* Calculates the clustering coefficient for the given node in the given
* graph. The clustering coefficient is the number of edges between
* the neighbours of i divided by the number of possible edges.
* If the graph is directed, an exception is thrown.
* If the number of neighbours is 1, returns 1. For zero neighbours
* returns NAN.
* @throws IllegalArgumentException if g is directed
*/
public static double clustering( Graph g, int i ) {

	if( g.directed() ) throw new IllegalArgumentException(
		"graph is directed");
		
	Object[] n = g.getNeighbours(i).toArray();
	
	if( n.length==1 ) return 1.0;
	
	int edges = 0;
	
	for(int j=0; j<n.length; ++j)
	for(int k=j+1; k<n.length; ++k)
		if( g.isEdge(	((Integer)n[j]).intValue(),
				((Integer)n[k]).intValue() ) ) ++edges;

	return ((edges*2.0)/n.length)/(n.length-1);
}

// --------------------------------------------------------------------

/**
* Performs anti-entropy epidemic multicasting from node 0.
* As a result the number of nodes that have been reached in cycle i
* is put into b[i]. The number of cycles performed is determined by b.length.
* In each cycle each node contacts a random neighbour and exchanges
* information. The simulation is generational.
*/
public static void multicast( Graph g, int[] b, Random r ) {

	int c1[] = new int[g.size()];
	int c2[] = new int[g.size()];
	for(int i=0; i<c1.length; ++i) c2[i]=c1[i]=WHITE;
	c2[0]=c1[0]=BLACK;
	Collection neighbours=null;
	int black=1;
	
	int k=0;
	for(; k<b.length || black<g.size(); ++k)
	{
		for(int i=0; i<c2.length; ++i)
		{
			neighbours=g.getNeighbours(i);
			Iterator it=neighbours.iterator();
			for(int j=r.nextInt(neighbours.size()); j>0; --j)
				it.next();
			int randn = ((Integer)it.next()).intValue();
			
			// push pull exchane with random neighbour
			if( c1[i]==BLACK ) //c2[i] is black too
			{
				if(c2[randn]==WHITE) ++black;
				c2[randn]=BLACK;
			}
			else if( c1[randn]==BLACK )
			{
				if(c2[i]==WHITE) ++black;
				c2[i]=BLACK;
			}
		}
		System.arraycopy(c2,0,c1,0,c1.length);
		b[k]=black;
	}
	
	for(; k<b.length; ++k) b[k]=g.size();
}

// --------------------------------------------------------------------

/**
* Performs flooding from given node.
* As a result the number of nodes that have been reached in step i
* is put into b[i]. In other words, b[i] contains the number of nodes
* that can be reached by exactly i steps, and always b[0]=1.
* In fact, b[i] is the number of nodes that are of at most distance i from
* the starting node.
* If the maximal distance from k is lower than b.length, then the remaining
* elements of b are zero.
*/
public void flooding( Graph g, int[] b, int k ) {

	if( color==null || color.length<g.size() ) color = new int[g.size()];
	if( d==null || d.length<g.size() ) d = new int[g.size()];
	for(int i=0; i<g.size(); ++i)
		d[i]=color[i]=WHITE; // we use that WHITE=0
	for(int i=0; i<b.length; ++i) b[i]=0;

	int diam = 0;

	bfs(g,k);
	
	for(int i=0; i<d.length; ++i)
	{
		if( d[i] > diam ) diam = d[i];
		if( d[i] > 0 && d[i] < b.length ) b[d[i]]++;
	}

	b[0] = 1; 
}


}

