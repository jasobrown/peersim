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

import peersim.util.CommonRandom;
import peersim.core.*;
import peersim.config.*;

/**
 * 
 *
 *  @author Alberto Montresor
 *  @version $Revision$
 */
public class FallibleAverageFunction extends AbstractFunction
{

	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////

	private final static String PAR_FAILUREPROB = "failure";

	private final static String PAR_STYLE = "style";

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

	/** Failure probability */
	private static double failureProb;

	/** True if the failure style is atomic */
	private static boolean atomic;

	////////////////////////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////////////////////////

	public FallibleAverageFunction(String prefix, Object obj)
	{
		super(prefix, obj);
		failureProb = Configuration.getDouble(prefix + "." + PAR_FAILUREPROB);
		String style = Configuration.getString(prefix + "." + PAR_STYLE, "atomic");
		atomic = ("atomic".equalsIgnoreCase(style));
	}

	public Object clone() throws CloneNotSupportedException
	{
		FallibleAverageFunction af = (FallibleAverageFunction) super.clone();
		af.value = value;
		return af;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Using a {@link Linkable} protocol choses a neighbor and performs a
	 * variance reduction step.
	 */
	public void nextCycle(Node node, int protocolID)
	{
		// We perform a random test based on the failure probability
		// In both the atomic and non-atomic case, if this test fails
		// we do not do nothing here: the first message has been lost.
		if (CommonRandom.r.nextDouble() >= failureProb)
		{
			
			int linkableID = Protocols.getLink(protocolID);
			Linkable linkable = (Linkable) node.getProtocol(linkableID);
			if (linkable.degree() > 0)
			{
				Node peer =
					linkable.getNeighbor(CommonRandom.r.nextInt(linkable.degree()));

				// XXX quick and dirty handling of failure
				if (peer.getFailState() != Fallible.OK)
					return;

				FallibleAverageFunction neighbor =
					(FallibleAverageFunction) peer.getProtocol(protocolID);
				if (atomic)
				{
					// In the atomic case, since we passed the first
					// test, we may safely perform the exchange
					double mean = (this.value + neighbor.value) / 2;
					this.value = mean;
					neighbor.value = mean;
				} else
				{
					// In the non-atomic case, we perform another test
					// for the response message. If the message is lost,
					// we just update the contacted node.
					double mean = (this.value + neighbor.value) / 2;
					neighbor.value = mean;
					if (CommonRandom.r.nextDouble() >= failureProb)
						this.value = mean;
				}
			}
		}
	}

}
