package peersim.init;

import peersim.core.Node;

/**
* Generic interface to initialize a simulation. It is designed to allow
* maximal flexibility therefore poses virtually no restrictions on the
* implementation. It can be used to imlpement initializations before the
* simulation that require global knowledge of the system.
*/
public interface NodeInitializer {

	/**
	* Performs arbitrary initializations on the given node.
	* It is guaranteed that this is called <em>before</em> inserting
	* the node into the network.
	*/
	public void initialize(Node n);
}


