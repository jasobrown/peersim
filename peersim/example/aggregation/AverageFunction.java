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

import peersim.core.*;
import peersim.config.FastConfig;
import peersim.vector.SingleValueHolder;
import peersim.cdsim.CDProtocol;

/**
 * 
 *
 *  @author Alberto Montresor
 *  @version $Revision$
 */
public class AverageFunction extends SingleValueHolder implements CDProtocol {

public AverageFunction(String prefix, Object obj) { super(prefix,obj); }

/*public Object clone() throws CloneNotSupportedException {
	
	AverageFunction af = (AverageFunction)super.clone();
	af.value = value;
	return af;
}*/

/**
 * Using a {@link Linkable} protocol choses a neighbor and performs a
 * variance reduction step.
 */
public void nextCycle( Node node, int protocolID )
{
	int linkableID = FastConfig.getLinkable(protocolID);
	Linkable linkable = (Linkable) node.getProtocol( linkableID );
	if (linkable.degree() > 0)
	{
		Node peer = linkable.getNeighbor(
				CommonState.r.nextInt(linkable.degree()));
		
		// XXX quick and dirty handling of failure
		if(peer.getFailState()!=Fallible.OK) return;
		
		AverageFunction neighbor =
				(AverageFunction)peer.getProtocol(protocolID);
		double mean = (this.value + neighbor.value) / 2;
		this.value = mean;
		neighbor.value = mean;
	}
}

}
