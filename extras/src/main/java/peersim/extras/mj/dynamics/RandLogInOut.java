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
import peersim.dynamics.NodeInitializer;

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
 * Config parameter which gives the prefix of node initializers. An arbitrary
 * number of node initializers can be specified (Along with their parameters).
 * These will be applied on the nodes that (re-)join the network.
 * The initializers are ordered according to
 * alphabetical order of their ID.
 * Example:
 * <pre>
control.0 RandLogInOut
control.0.init.0 RandNI
control.0.init.0.k 5
control.0.init.0.protocol somelinkable
...
 * </pre>
 * @config
 */
private static final String PAR_INIT = "init";

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

/** node initializers to apply on the newly added nodes */
protected final NodeInitializer[] inits;

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
	Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
	inits = new NodeInitializer[tmp.length];
	for (int i = 0; i < tmp.length; ++i) {
		inits[i] = (NodeInitializer) tmp[i];
	}
}

// --------------------------------------------------------------------------
// Public methods
// --------------------------------------------------------------------------

public boolean execute() {
	
	for (int i = 0; i < Network.size(); i++)
	{

		final Node n = Network.get(i);
		final double d = CommonState.r.nextDouble();
		
		if (n.getFailState() == Fallible.OK && d <= pOff)
		{
			n.setFailState(Fallible.DOWN);
		}
		else if (n.getFailState() == Fallible.DOWN && d <= pOn)
		{			
			n.setFailState(Fallible.OK);
			for (int j = 0; j < inits.length; ++j)
				inits[j].initialize(n);
		}
	}
	
	return false;
}
}
