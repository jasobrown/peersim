package peersim.dynamics;

/**
* Generic interface to modify the underlying network
* (accessibility, failures, node removal and addition) and the state
* of the protocols.
* It is designed to allow
* maximal flexibility therefore poses virtually no restrictions on the
* implementation.
* It is a time-based concept which works fine with both cycle based and
* event based simulation.
*/
public interface Dynamics {

	/**
	* Performs arbitrary modifications on the components.
	*/
	public void modify();
}


