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

package peersim.extras.am.epidemic.sorted;

import peersim.core.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface SortedRing
{

/** Predecessor direction in the ring */
public static final int PREV = -1;

/** Successor direction in the ring */
public static final int NEXT = +1;

/**
 * Return the <code>i</code>-th neighbor in the specified direction.
 * 
 * @param dir
 *          the direction in the ring
 * @param i
 *          the index of neighborhood, starting from 0
 */
public Node getLeaf(int dir, int i);

/**
 * Store a node as the <code>i</code>-th leaf in the specified direction.
 * 
 * @param dir
 *          the direction in the ring
 * @param i
 *          the index of neighborhood, starting from 0
 * @param node
 *          the node to be stored
 */
public void setLeaf(int dir, int i, Node node);

/**
 * Returns the number of neighbors in each direction.
 */
public int leafDegree();

}
