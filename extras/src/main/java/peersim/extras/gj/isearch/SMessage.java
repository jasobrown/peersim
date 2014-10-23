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

package peersim.extras.gj.isearch;

import peersim.cdsim.CDState;
import peersim.core.Node;

/**
 * 
 * @author Gian Paolo Jesi
 */
public class SMessage implements Cloneable {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    public static final int QRY = 0;

    public static final int FWD = 1;

    public static final int HIT = 2;

    private static int seq_generator = 0;

    public int hops, type, seq, start;

    public Node originator; // the query producer

    public int[] payload; // an array of keys

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public SMessage(Node originator, int type, int hops, int[] payload) {
        this.originator = originator;
        this.type = type;
        this.hops = hops;
        this.payload = payload;
        this.seq = ++seq_generator;
        this.start = CDState.getCycle();
    }

    public Object clone() throws CloneNotSupportedException {
        SMessage m = (SMessage) super.clone();
        return m;
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    public int hashCode() {
        return seq;
    }

    public boolean equals(Object obj) {
        return (obj instanceof SMessage) && (((SMessage) obj).seq == this.seq);
    }

    public String toString() {
        return "SMessage[" + seq + "] hops=" + hops;
    }
}
