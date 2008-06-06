/*
 * Copyright (c) 2008 M. Jelasity and N. Tolgyesi
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

package peersim.extras.mj.ednewscast;

import peersim.core.*;
import peersim.config.*;
import peersim.edsim.*;
import peersim.dynamics.NodeInitializer;

/**
 * A {@link Control} to initialize the event generating to each node.
 * 
 */

public class EventInitializer implements Control, NodeInitializer {

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * Protocol ID to send events to.
 * 
 * @config
 */
private static final String PAR_PROT = "protocolID";

/**
 * Maximum delay of first event. The actual delay is a random
 * value between 0 and this value, inclusive. Default is 0.
 * 
 * @config
 */
private static final String PAR_MFED = "maxFirstDelay";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** value of {@value #protocolID} */
private final int protocolID;

/** value of {@value #maxFirstEventDelay} */
private final long maxDelay;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

public EventInitializer(String prefix) {

	protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
	maxDelay = Configuration.getLong(prefix + "." + PAR_MFED, 0);
}

// --------------------------------------------------------------------------
// Public methods
// --------------------------------------------------------------------------

public boolean execute() {
	
	for (int i = 0; i < Network.size(); ++i) {
		initialize(Network.get(i));
	}

	return false;
}

/**
 * This function sets the first event to a node.
 */
public void initialize(Node n) {
	
	// add the first event to a queue to the actual node
	EDSimulator.add(CommonState.r.nextLong(maxDelay + 1),
			CycleMessage.inst, n, protocolID);
}

}
