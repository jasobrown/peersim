/*
 * Copyright (c) 2003-2005 The BISON Project
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

package example.aggregation;

import peersim.config.*;
import peersim.core.*;
import peersim.vector.SingleValue;

/**
 * Initialize an aggregation protocol using a peak distribution.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class PeakDistributionInitializer implements Control
{

// --------------------------------------------------------------------------
// Constants
// --------------------------------------------------------------------------

/**
 * The load at the peak node.
 * @config
 */
private static final String PAR_VALUE = "value";

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * String name of the parameter that defines the length of an epoch in
 * number of cycles. Parameter read will have the full name
 * <tt>prefix+"."+PAR_EPOCH_LEN</tt>
 * @config
 */
private static final String PAR_EPOCH_LEN = "epoch.length";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Value at the peak node */
private final double value;

/** Protocol identifier */
private final int pid;

/** Number of cycles composing an epoch */
private final int epochLength;

// --------------------------------------------------------------------------
// Constructor
// --------------------------------------------------------------------------

/**
 * Read parameters.
 */
public PeakDistributionInitializer(String prefix)
{
	value = Configuration.getDouble(prefix + "." + PAR_VALUE);
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	epochLength = Configuration.getInt(prefix + "." + PAR_EPOCH_LEN, 30);
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

// Comment inherited from interface
public boolean execute()
{
	System.err.println("Restarting: " + Network.size());
	for (int i = 0; i < Network.size(); i++) {
		SingleValue prot = (SingleValue) Network.get(i).getProtocol(pid);
		prot.setValue(0);
	}
	SingleValue prot = (SingleValue) Network.get(0).getProtocol(pid);
	prot.setValue(value);

	return false;
}

// --------------------------------------------------------------------------

}
