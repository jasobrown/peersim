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

package aggregation.secure;

import peersim.core.Node;

/**
 * This interface represents a collection of information about the 
 * exchanges initiated and received by a node. Three kind of methods
 * are provided: methods for adding information to the history
 * (that should be used whenever an exchange is initiated or
 * received); methods to reset the history (they may be needed to
 * correctly handle the restarting mechanism), and methods to
 * check the history of a node against the history of another node.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface History
{

/**
 * Add information about an exchange initiated by a remote node,
 * including the identifier of the initiating node, the value exchanged 
 * and the cycle at which the exchange happened.
 * 
 * @param source
 *  the node that initiated the exchange
 * @param value
 *  the value received
 * @param cycle
 *  the cycle at which the exchange happened
 */
public void addReceived(Node source, double value, int cycle);

/**
 * Add information about an exchange initiated locally,
 * including the identifier of the contacted node, the value exchanged
 * and the cycle at which the exchange happened.
 * 
 * @param destination
 *  the node that has
 * @param value
 *  the value sent
 * @param cycle
 *  the cycle at which the exchange happened
 */
public void addInitiated(Node destination, double value, int cycle);

/**
 * 
 * 
 * @param rnode
 * @param hid
 * @return
 */
public Node[] checkRandomNode(Node rnode, int hid);

/**
 * Reset the history.
 */
public void reset();

}
