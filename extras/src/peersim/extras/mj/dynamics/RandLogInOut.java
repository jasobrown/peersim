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

package peersim.extras.mj.dynamics;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
/**
 * A {@link Control} to modell the random log in and out of nodes in network.
 * With a given configurable probability, all nodes that are up go offline
 * (but do not leave the network) and with a given probability all nodes that
 * are offline (down) come back online.
 */

public class RandLogInOut implements Control {

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * Defines the probability for a node that is offline (down) to go
 * online (up). Defults to 0.
 * @config
 */
private static final String PAR_pOn = "pOn";

/**
 * Defines the probability for a node that is online (up) to go
 * offline (down). Defults to 0.
 * @config
 */
private static final String PAR_pOff = "pOff";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** value of {@value #PAR_pOn} */
protected final double pOn;

/** value of {@value #PAR_pOff} */
protected final double pOff;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters. Invoked by
 * the simulation engine.
 * 
 * @param prefix
 *            the configuration prefix for this class
 */	
public RandLogInOut(String prefix) {
	
	pOn = Configuration.getDouble(prefix + "." + PAR_pOn, 0);
	if(pOn<0 || pOn>1) {
		throw new IllegalParameterException(
			prefix + "." + PAR_pOn,
			"probability must be between 0 and 1!");
	}
	pOff = Configuration.getDouble(prefix + "." + PAR_pOff, 0);
	if(pOff<0 || pOff>1) {
		throw new IllegalParameterException(
			prefix + "." + PAR_pOff,
			"probability must be between 0 and 1!");
	}
}

// --------------------------------------------------------------------------
// Public methods
// --------------------------------------------------------------------------

public boolean execute() {
	
	for (int i = 0; i < Network.size(); i++) {
		if (Network.get(i).getFailState() == Fallible.OK) {
			if (CommonState.r.nextDouble() <= pOff) {
				Network.get(i).setFailState(Fallible.DOWN);
			}
		} else {
			if (Network.get(i).getFailState() == Fallible.DOWN) {
				if (CommonState.r.nextDouble() <= pOn) {
					Network.get(i).setFailState(Fallible.OK);
				}
			}
		}
	}
	return false;
}
}

