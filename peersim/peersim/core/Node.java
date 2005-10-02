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

package peersim.core;

/**
 * Class that represents one node with a network address. An {@link Network} is
 * made of a set of nodes. The functionality of this class is thin: it must be
 * able to represent failure states and store a list of protocols. It is the
 * protocols that do the interesting job.
 */
public interface Node extends Fallible, Cloneable
{

/**
 * Prefix of the parameters that defines protocols.
 * @config
 */
public static final String PAR_PROT = "protocol";

/**
 * Returns the i-th protocol in this node. If i is not a valid protocol id
 * (negative or larger than or equal to the number of protocols), then throws
 * IndexOutOfBoundsException.
 */
public Protocol getProtocol(int i);

/**
 * Returns the number of protocols included in this node.
 */
public int protocolSize();

/**
 * Sets the index of this node in the internal representation of the node list.
 * Applications should not use this method. Using this mehtod will result in
 * undefined behvior. It is provided for the core system.
 */
public void setIndex(int index);

/**
 * Returns the index of this node. It is such that
 * <code>Network.get(n.getIndex())</code> returns n. This index can
 * change during a simulation, it is not a fixed id.
 * @see Network#get
 */
public int getIndex();

/**
 * We include this to change the access right to public and to get rid if the
 * <code>throws</code> clause. 
 */
public Object clone();

}
