package peersim.core;

/**
* A protocol that does nothing but knows everything.
* It provides an interface which models a protocol that knows all nodes
* in the network, ie the neighborhood set of this protocol is always the
* whole node set. this protocol is also extremely cheap, in fact it
* has no data fields.
*/
public class OracleIdleProtocol implements Protocol, Linkable {

// =================== initialization, creation ======================
// ===================================================================


public OracleIdleProtocol(String s) {}

// --------------------------------------------------------------------

/** Returns <tt>this</tt> to maximize memory saving. It contains no fields.*/
public Object clone() throws CloneNotSupportedException { return this; }


// ===================== public methods ===============================
// ====================================================================


/** This is an expensive operation here, should not be used at all */
public boolean contains(Node n) {

	final int len = Network.size();
	for (int i=0; i < len; i++)
	{
		if (Network.node[i] == n)
		return true;
	}
	return false;
}

// --------------------------------------------------------------------

/** Unsupported operation */
public boolean addNeighbor(Node n) {

	throw new UnsupportedOperationException();
}

// --------------------------------------------------------------------
  
/**
* The neighborhood contains the node itself, ie it contains the loop
* edge.
*/
public Node getNeighbor(int i) {
	
	return Network.node[i];
}

// --------------------------------------------------------------------

public int degree() {
	
	return Network.size();
}

// --------------------------------------------------------------------

public void pack() {}  

// --------------------------------------------------------------------

public String toString() {

	return degree()+" [all the nodes of the overlay network]";
}

}

