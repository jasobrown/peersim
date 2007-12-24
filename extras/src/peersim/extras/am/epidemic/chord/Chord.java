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

package peersim.extras.am.epidemic.chord;

import peersim.core.*;

public interface Chord
{

/**
 * Returns the successor in position index.
 */
public abstract Node getSuccessor(int index);

/**
 * Store the specified node as index-th successor.
 */
public abstract void setSuccessor(int index, Node node);

/**
 * Returns the finger in position index.
 */
public abstract Node getFinger(int index);

/**
 * Store the specified node as index-th finger.
 */
public abstract void setFinger(int index, Node node);

/**
 * Returns the number of successors.
 */
public abstract int successors();
}