package peersim.graph;

import java.util.*;

/**
* This class is an adaptor for representing special subgraphs of any graph.
* It can represent the subgraphs spanned by the nodes 0,...,i where
* i is less than or equal to n, the size of the original graph.
*/
public class PrefixSubGraph implements Graph {


// ====================== private fileds ========================
// ==============================================================


private final Graph g;

/** The graph represents the subgraph defined by nodes 0,...,prefSize */
private int prefSize;


// ====================== public constructors ===================
// ==============================================================


/**
* Constructs an initially max size subgraph of g. That is, the subgrpah will
* contain all nodes.
*/
public PrefixSubGraph( Graph g ) {

	this.g = g;
	prefSize = g.size();
}


// ======================= Graph implementations ================
// ==============================================================


public boolean isEdge(int i, int j) {
	
	if( i<0 || i>=prefSize ) throw new IndexOutOfBoundsException();
	if( j<0 || j>=prefSize ) throw new IndexOutOfBoundsException();
	return g.isEdge(i,j);
}

// ---------------------------------------------------------------

public Collection getNeighbours(int i) {
	
	if( i<0 || i>=prefSize ) throw new IndexOutOfBoundsException();
	
	List result = new LinkedList();
	Iterator it = g.getNeighbours(i).iterator();
	while(it.hasNext())
	{
		Integer in = (Integer)it.next();
		if( in.intValue() < prefSize ) result.add(in);
	}

	return Collections.unmodifiableCollection(result);
}

// ---------------------------------------------------------------

public Object getNode(int i) {

	if( i<0 || i>=prefSize ) throw new IndexOutOfBoundsException();
	return g.getNode(i);
}
	
// ---------------------------------------------------------------

/**
* Returns the edge in the original graph if both i and j are smaller than
* size().
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
public int size() { return prefSize; }

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

	if( i<0 || i>=prefSize ) throw new IndexOutOfBoundsException();
	return g.degree(i);
}
	

// ================= public functions =================================
// ====================================================================


/**
* Sets the size of the subgraph. If i is negative, it is changed to 0 and
* if it is larger than the underlying graph size, it is changed to the
* underlying graph size (set at construction time).
* @return old size.
*/
public int setSize(int i) {
	
	int was = prefSize;
	if( i < 0 ) i = 0;
	if( i > g.size() ) i=g.size();
	prefSize=i;
	return was;
}
}

