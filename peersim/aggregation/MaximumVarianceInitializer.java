/*
 * Copyright (c) 2003 The BISON Project
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

package aggregation;

import peersim.config.*;
import peersim.core.*;
import peersim.init.*;
import peersim.dynamics.*;

/**
 * This initializer/dynamics class initializes the values of the first half 
 * of the nodes are initialized to the specified value, while the second
 * half is initialized to zero. No special mechanism is applied to deal
 * with crashed nodes.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MaximumVarianceInitializer implements Initializer, Dynamics
{

	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////

	/** 
	 * String name of the parameter used to determine the initial load at
	 * the peak node. Parameter read has the full name
	 * <tt>prefix+"."+PAR_VALUE</tt>
	 */
	public static final String PAR_VALUE = "value";

	/** 
	 * String name of the parameter that defines the protocol to initialize.
	 * Parameter read has the full name
	 * <tt>prefix+"."+PAR_PROT</tt>
	 */
	public static final String PAR_PROT = "protocol";

	/** 
	 * String name of the parameter that defines the length of an epoch
	 * in number of cycles. Parameter read will have the full name
	 * <tt>prefix+"."+PAR_EPOCH_LEN</tt>
	 */
	public static final String PAR_EPOCH_LEN = "epoch.length";

	////////////////////////////////////////////////////////////////////////////
	// Static fields
	////////////////////////////////////////////////////////////////////////////

	/** Value at the peak node */
	private final double value;

	/** Protocol identifier */
	private final int pid;

	/** Number of cycles composing an epoch */
	private final int epochLength;

	////////////////////////////////////////////////////////////////////////////
	// Initialization
	////////////////////////////////////////////////////////////////////////////

	public MaximumVarianceInitializer(String prefix)
	{
		value = Configuration.getDouble(prefix + "." + PAR_VALUE);
		pid = Configuration.getInt(prefix + "." + PAR_PROT);
		epochLength = Configuration.getInt(prefix + "." + PAR_EPOCH_LEN, 30);
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////

	// Comment inherited from interface
	public void initialize()
	{
		int len = OverlayNetwork.size();
		for (int i = 0; i < len / 2; i++)
		{
			Aggregation node = (Aggregation) OverlayNetwork.get(i).getProtocol(pid);
			node.setValue(value);
		}
		for (int i = len / 2; i < len; i++)
		{
			Aggregation node = (Aggregation) OverlayNetwork.get(i).getProtocol(pid);
			node.setValue(0);
		}
	}

	// Comment inherited from interface
	public void modify()
	{
		int time = peersim.core.CommonState.getT();
		if (time % epochLength == 0)
		{
			System.err.println("Restarting: " + OverlayNetwork.size());
			initialize();
		}
	}

}
