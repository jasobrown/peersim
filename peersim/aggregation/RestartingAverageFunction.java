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

/**
 * 
 *
 *  @author Alberto Montresor
 *  @version $Revision$
 */
public class RestartingAverageFunction extends AbstractFunction
{

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

	private boolean justArrived;

	////////////////////////////////////////////////////////////////////////////
	// Constructor and cloning
	////////////////////////////////////////////////////////////////////////////

	public RestartingAverageFunction(String prefix, Object obj)
	{
		super(prefix, obj);
		justArrived = true;
	}

	public Object clone() throws CloneNotSupportedException
	{
		RestartingAverageFunction af = (RestartingAverageFunction) super.clone();
		af.value = value;
		justArrived = true;
		return af;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////

	public void setValue(double value)
	{
		super.setValue(value);
		justArrived = false;
	}

	/**
	 * Using a {@link Linkable} protocol choses a neighbor and performs a
	 * variance reduction step.
	 */
	public void nextCycle(Node node, int pid)
	{
		// Nodes "just arrived" do not partecipate in the current epoch 
		if (justArrived)
			return;

		// Get neighbors      
		int linkableID = Protocols.getLink(pid);
		Linkable linkable = (Linkable) node.getProtocol(linkableID);

		// Nodes with no neighbors (?) do not partecipate in the 
		// protocol
		if (linkable.degree() == 0)
			return;

		// Perform a variance reduction step with a neighbor	  
		boolean found = false;
		while (!found)
		{
			Node peer =
				linkable.getNeighbor(CommonRandom.r.nextInt(linkable.degree()));

			RestartingAverageFunction neighbor =
				(RestartingAverageFunction) peer.getProtocol(pid);
				
			// Nodes just arrived shouldn't be considered in the
			// selection. Since we do not store information about
			// neighbors apart from their reference, we just skip
			// these nodes
			// XXX quick and dirty handling of failure
			if (peer.getFailState() == Fallible.OK)
			{
				if (!neighbor.justArrived)
				{
					double mean = (this.value + neighbor.value) / 2;
					this.value = mean;
					neighbor.value = mean;
				}
				found = true;
			}
		}
	}

} 
