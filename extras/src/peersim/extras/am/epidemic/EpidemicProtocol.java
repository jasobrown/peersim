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

package peersim.extras.am.epidemic;

import peersim.core.*;

/**
 * This interface represents a generic gossip protocol.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface EpidemicProtocol extends Protocol
{

/**
 * Selects the node to interact with during a cycle.
 * @param lnode
 * 					the local node on which this method is invoked
 * @return the selected node
 */
public Node selectPeer(Node lnode);

/**
 * Returns an active-thread initiated request for the specified node.
 * 
 * @param lnode
 * 					the local node on which this method is invoked
 * @param rnode
 *          the remote node for which the request is generated
 * @return a generic message
 */
public Message prepareRequest(Node lnode, Node rnode);

/**
 * Returns a passive-thread response for the specified node, in response to the
 * specified request.
 * 
 * @param lnode
 * 					the local node on which this method is invoked
 * @param rnode
 *          the remote node for which the response is generated
 * @param request
 *          the request message
 * @return a generic message
 */
public Message prepareResponse(Node lnode, Node rnode, Message request);

/**
 * Merge the received message to the local view.
 * 
 * @param lnode
 * 					the local node on which this method is invoked
 * @param rnode
 *          rnode the remote node from which the request comes for
 * @param msg
 *          the message
 */
public void merge(Node lnode, Node rnode, Message msg);

}