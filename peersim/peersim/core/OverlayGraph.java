package peersim.core;

import peersim.graph.Graph;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;

/**
* This class is an adaptor which makes a protocol on our overlay network
* look like a graph. It is useful because it allows the application of many
* graph algorithms and graph topology initialization methods.
* It stores the reference of the overlay network only so it follows the
* changes in it. However, if the nodes are reshuffled in the overlay network's
* internal representation, or of the node list changes,
* then the behaviour becomes unspecified.
*
* The fail state of nodes has an effect on the graph: all nodes are included
* but edges are included only if both ends are up. This expresses the fact
* that this graph is in fact defined by the "can communicate with" relation.
*/
public class OverlayGraph implements Graph {


// ====================== fields ================================
// ==============================================================

/**
* The protocol ID that selects the Linkable protocol to convert to a graph.
*/
public final int protocolID;


// ====================== public constructors ===================
// ==============================================================

/**
* @param protocolID The protocol on which this adaptor is supposed
* to operate.
*/
public OverlayGraph( int protocolID ) {

	this.protocolID = protocolID;
}


// ======================= Graph implementations ================
// ==============================================================


public boolean isEdge(int i, int j) {
	
	return
		((Linkable)OverlayNetwork.node[i].getProtocol(protocolID)
		).contains(OverlayNetwork.node[j]) &&
		OverlayNetwork.node[j].isUp() &&
		OverlayNetwork.node[i].isUp();
}

// ---------------------------------------------------------------

public Collection getNeighbours(int i) {
	
	Linkable lble=(Linkable)OverlayNetwork.node[i].getProtocol(protocolID);
	ArrayList al = new ArrayList(lble.degree());
	for(int j=0; j<lble.degree(); ++j)
	{
		final Node n = lble.getNeighbor(j);
		// if acessible, we include it
		if(n.isUp()) al.add(new Integer(n.getIndex()));
	}
	return Collections.unmodifiableList(al);
}

// ---------------------------------------------------------------

public Object getNode(int i) { return OverlayNetwork.node[i]; }
	
// ---------------------------------------------------------------

/**
* If there is an (i,j) edge, returns that, otherwise if there is a (j,i)
* edge, returns that, otherwise returns null.
*/
public Object getEdge(int i, int j) { return null; }

// ---------------------------------------------------------------

public int size() { return OverlayNetwork.size(); }

// --------------------------------------------------------------------
	
public boolean directed() { return true; }

// --------------------------------------------------------------------

/**
* In some cases this behaves strangely. Namely, when node i or j is in a non-OK
* fail state but is not dead (eg it can be down temporarily).
* In such situations the relevant link is made, but afterwards
* getEdge(i,j) will NOT return true, only when the fail state has changed back
* to OK. This method is used normally by initializers when each node is in
* the OK state.
*
* <p>Conecptually one can think of it as a succesful operation which is
* immediately overruled by the dynamics of the underlying overlay network.
* Let's not forget that this class is an adaptor only.
*/
public boolean setEdge( int i, int j ) {
// XXX slightly unintuitive behavior but makes sense when understood
	
	return
		((Linkable)OverlayNetwork.node[i].getProtocol(protocolID)
		).addNeighbor(OverlayNetwork.node[j]);
}

// ---------------------------------------------------------------

public boolean clearEdge( int i, int j ) {
	
	throw new UnsupportedOperationException();
}

// ---------------------------------------------------------------

public int degree(int i) {
	
	return 
	 ((Linkable)OverlayNetwork.node[i].getProtocol(protocolID)).degree();
}

}


