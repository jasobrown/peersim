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

package peersim.extras.am.epidemic.pastry;

import peersim.core.*;
import peersim.extras.am.epidemic.sorted.*;

public interface Pastry extends SortedRing
{

/**
 * Return the b parameter of pastry.
 */
public abstract int b();

/**
 * Return the radix parameter of pastry, equal to 2^b.
 */
public abstract int radix();

/**
 * Return the number of digits in pastry peersim.extras.am.id
 */
public abstract int digits();

/**
 * Returns the finger identified by the length of the common subsequence and the
 * index.
 */
public abstract Node getFinger(int common, int index);

/**
 * Store the specified node as index-th finger.
 */
public abstract void setFinger(int common, int index, Node node);

/**
 * node identifier
 * @param node
 *          the node to be added
 */
public abstract void addFinger(Node node);
}