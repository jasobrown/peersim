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

package example.hot;

import peersim.core.IdleProtocol;

/**
 * This class does nothing. It is simply a container inside each node to collect
 * some useful data such as coordinates, hop count and degree count.
 * 
 * @author Gian Paolo Jesi
 */
public class InetNodeProtocol extends IdleProtocol {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** 2d coordinates components. */
    public double x, y;

    /** Hop distance from the ROOT node. */
    public int hops;

    /** If the current node is a root or not. */
    public boolean isroot;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
    public InetNodeProtocol(String prefix) {
        super(prefix);
        hops = 0;
        isroot = false;
    }


}
