package peersim.cdsim;

import peersim.core.Protocol;
import peersim.core.Node;

public interface CDProtocol extends Protocol {

	/**
	* A protocol which is defined by performing an algorithm
	* in more or less regular periodic intrevals. The implementation
	* defines the protocol.
	*
	* @param node the node on which this component is run
	* @param protocolID the id of this protocol in the protocol array
	*/
	public void nextCycle( Node node, int protocolID );
}

