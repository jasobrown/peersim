package peersim.init;

/**
* Generic interface to initialize a simulation. It is designed to allow
* maximal flexibility therefore poses virtually no restrictions on the
* implementation. It can be used to imlpement initializations before the
* simulation that require global knowledge of the system.
*/
public interface Initializer {

	/**
	* Performs arbitrary initializations or modifications on the overlay
	* nework and protocols before the simulation.
	* Implementations will typically know many details of the
	* actual overlay network and protocols, but there will be general
	* purpose reusable initializers too for example to set up a topology.
	*/
	public void initialize();
}

