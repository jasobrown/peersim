package peersim.graph;

import java.util.*;

/**
* Contains static methods for wiring certain kinds of graphs. The general
* contract of all methods is that they accept any graph and add edges
* as specified in the documentation.
*/
public class GraphFactory {
protected GraphFactory() {}

// ===================== public static methods ======================
// ==================================================================

/**
* Wires a ring lattice.
* The added connections are defined as follows: node i has conections to nodes
* i-k/2, i-k/2+1, ..., i+k/2 (but not with i) and all values are understood
* mod n to make the lattice circular, where n is the number of nodes in g.
* @param g the graph to be wired
* @param k lattice parameter
* @return returns g for convinience
*/
public static Graph wireRingLattice(Graph g, int k) {
	
	int n = g.size();
	for(int i=0; i<n; ++i)
	for(int j=-k/2; j<=k/2; ++j)
	{
		if( j==0 ) continue;
		final int v = (i+j+n)%n;
		g.setEdge(i,v);
	}
	return g;
}

// -------------------------------------------------------------------

/**
* Watts-Strogatz model. Rewiring is done
* with replacement, so the possibility of wiring two links to the same target
* is positive (though very small).
* @param g the graph to be wired
* @param k lattice parameter
* @param p the propability of rewireing
* @param r source of randomness
* @return returns g for convinience
*/
public static Graph wireWS( Graph g, int k, double p, Random r ) {

	int n = g.size();
	for(int i=0; i<n; ++i)
	for(int j=-k/2; j<=k/2; ++j)
	{
		if( j==0 ) continue;
		int newedge = (i+j+n)%n;
		if( r.nextDouble() < p )
		{
			newedge = r.nextInt(n-1);
			if( newedge >= i ) newedge++; // random _other_ node
		}
		g.setEdge(i,newedge);
	}
	return g;
}

// -------------------------------------------------------------------

/**
* Random graph. Generates randomly k edges out of each node. The neighbors
* (edge targets) are chosen with replacement. No loop edge is added.
* @param g the graph to be wired
* @param k samples to be drawn for each node
* @param r source of randomness
* @return returns g for convinience
*/
public static Graph wireRegularRandom( Graph g, int k, Random r ) {

	int n = g.size();
	if( n < 2 ) return g;
	for(int i=0; i<n; ++i)
	for(int j=0; j<k; ++j)
	{
		// draw from nodes other than i
		int newedge = r.nextInt(n-1);
		if( newedge >= i ) newedge++;
		g.setEdge(i,newedge);
	}
	return g;
}

// -------------------------------------------------------------------

/**
* A sink star.
* Wires a sink star topology adding a link to 0 from all other nodes.
* @param g the graph to be wired
* @return returns g for convinience
*/
public static Graph wireStar( Graph g ) {

	int n = g.size();
	for(int i=1; i<n; ++i) g.setEdge(i,0);
	return g;
}

// -------------------------------------------------------------------

public static void main(String[] pars) {
	
	Graph g = new BitMatrixGraph(100);
	wireWS(g,20,.1,new Random());
	GraphIO.writeChaco(new UndirectedGraph(g),System.out);
}

}

