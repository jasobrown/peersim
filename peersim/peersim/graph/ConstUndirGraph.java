package peersim.graph;

import java.util.*;

/**
* This class is an adaptor making any Graph an undirected graph
* by making its edges bidirectional. The graph to be made undirected
* is passed to the constructor. Only the reference is stored.
* However, at construction time the incoming edges are stored
* for each node, so if the graph
* passed to the constructor changes over time then
* methods {@link #getNeighbours(int)} and {@link #degree(int)}
* become inconsistent (but only those).
* The upside of this inconvinience is that getNeighbours will have
* constant time complexity.
*/
public class ConstUndirGraph implements Graph {


// ====================== private fileds ========================
// ==============================================================


private final Graph g;

private final List[] in;

// ====================== public constructors ===================
// ==============================================================


public ConstUndirGraph( Graph g ) {

	this.g = g;
	if( !g.directed() )
	{
		in = null;
		return;
	}
	
	final int max = g.size();
	in = new List[max];
	for(int i=0; i<max; ++i) in[i] = new ArrayList();
	for(int i=0; i<max; ++i)
	{
		Integer thisNode = new Integer(i);
		Collection out = g.getNeighbours(i);
		Iterator it = out.iterator();
		while( it.hasNext() )
		{
			int j = ((Integer)it.next()).intValue();
			if( ! g.isEdge(j,i) ) in[j].add(thisNode);
		}
	}
}


// ======================= Graph implementations ================
// ==============================================================


public boolean isEdge(int i, int j) {
	
	return g.isEdge(i,j) || g.isEdge(j,i);
}

// ---------------------------------------------------------------

/**
* Uses sets as collection so does not support multiple edges now, even if
* the underlying direced graph does.
*/
public Collection getNeighbours(int i) {
	
	List result = new ArrayList();
	result.addAll(g.getNeighbours(i));
	if( in != null ) result.addAll(in[i]);
	return Collections.unmodifiableCollection(result);
}

// ---------------------------------------------------------------

public Object getNode(int i) { return g.getNode(i); }
	
// ---------------------------------------------------------------

/**
* If there is an (i,j) edge, returns that, otherwise if there is a (j,i)
* edge, returns that, otherwise returns null.
*/
public Object getEdge(int i, int j) {
	
	if( g.isEdge(i,j) ) return g.getEdge(i,j);
	if( g.isEdge(j,i) ) return g.getEdge(j,i);
	return null;
}

// ---------------------------------------------------------------

public int size() { return g.size(); }

// --------------------------------------------------------------------
	
public boolean directed() { return false; }

// --------------------------------------------------------------------

public boolean setEdge( int i, int j ) {
	
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------

public boolean clearEdge( int i, int j ) {
	
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------

public int degree(int i) { return g.degree(i)+(in==null?0:in[i].size()); }

// ---------------------------------------------------------------

public static void main( String[] args ) {

	Graph net = new BitMatrixGraph(20);
	GraphFactory.wireRegularRandom(net,5,new Random());
	ConstUndirGraph ug = new ConstUndirGraph(net);
	for(int i=0; i<net.size(); ++i)
		System.err.println(
			i+" "+net.getNeighbours(i)+" "+net.degree(i));
	System.err.println("============");
	for(int i=0; i<ug.size(); ++i)
		System.err.println(i+" "+ug.getNeighbours(i)+" "+ug.degree(i));
	System.err.println("============");
	for(int i=0; i<ug.size(); ++i)
		System.err.println(i+" "+ug.in[i]);
	for(int i=0; i<ug.size(); ++i)
	{
		for(int j=0; j<ug.size(); ++j)
			System.err.print(ug.isEdge(i,j)?"W ":"+ ");
		System.err.println();
	}

	GraphIO.writeUCINET_DL(net,System.out);
	GraphIO.writeUCINET_DL(ug,System.out);
}
}

