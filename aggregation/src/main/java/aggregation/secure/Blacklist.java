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

import peersim.core.*;
import java.util.*;

/**
 * A blacklist protocol is a container for information about nodes suspected
 * to be maliciuous. The protocol may be a simple container, or a mechanism
 * for broadcasting suspected nodes may be present.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface Blacklist 
{

/**
 * Corresponds to a blacklisting event, involving a node that generated
 * it and two suspected nodes. Depending on the particular implementation
 * of the blacklist, any subset of these nodes may be added to the list.
 * 
 * @param blid
 *  the identifier of the blacklist protocol
 * @param src
 *  the source of this blacklisting event
 * @param node1
 *  the first suspected node
 * @param node2
 *  the second suspected node
 */
public void add(int blid, Node src, Node node1, Node node2);

/**
 * Returns true if the specified node is contained in the blacklist, false
 * otherwise.
 * 
 * @param node the node to be checked 
 */
public boolean contains(Node node);

/**
 * Returns an iterator over the blacklist, to iterate over all the nodes
 * contained in it.
 */
public Iterator iterator();

}
