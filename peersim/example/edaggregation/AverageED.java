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

package example.edaggregation;

import peersim.vector.SingleValueHolder;
import peersim.config.*;
import peersim.core.*;
import peersim.transport.Transport;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;

/**
* Event driven version of epidemic averaging.
*/
public class AverageED extends SingleValueHolder
implements CDProtocol, EDProtocol {

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * @param prefix string prefix for config properties
 */
public AverageED(String prefix) { super(prefix); }


//--------------------------------------------------------------------------
// methods
//--------------------------------------------------------------------------

/**
 * Using a {@link Linkable} protocol choses a neighbor and performs a
 * variance reduction step.
 */
public void nextCycle( Node node, int pid )
{
	Linkable linkable = 
		(Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
	if (linkable.degree() > 0)
	{
		Node peern = linkable.getNeighbor(
				CommonState.r.nextInt(linkable.degree()));
		
		// XXX quick and dirty handling of failures
		// (message would be lost anyway, we save time)
		if(!peern.isUp()) return;
		
		AverageED peer = (AverageED) peern.getProtocol(pid);
		
		((Transport)node.getProtocol(FastConfig.getTransport(pid))).
			send(
				node,
				peern,
				new AverageMessage(value,node),
				pid);
	}
}

//--------------------------------------------------------------------------

public void processEvent( Node node, int pid, Object event ) {
		
	AverageMessage aem = (AverageMessage)event;
	
	if( aem.sender!=null )
		((Transport)node.getProtocol(FastConfig.getTransport(pid))).
			send(
				node,
				aem.sender,
				new AverageMessage(value,null),
				pid);
				
	value = (value + aem.value) / 2;
}

}

//--------------------------------------------------------------------------
//--------------------------------------------------------------------------

class AverageMessage {

	final double value;
	/** If not null,
	this has to be answered, otherwise this is the answer. */
	final Node sender;
	public AverageMessage( double value, Node sender )
	{
		this.value = value;
		this.sender = sender;
	}
}

