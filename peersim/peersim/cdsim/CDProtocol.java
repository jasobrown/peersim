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

