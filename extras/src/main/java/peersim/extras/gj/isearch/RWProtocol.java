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

import peersim.config.Configuration;
import peersim.core.Node;

/**
 * 
 * @author Gian Paolo Jesi
 */
public class RWProtocol extends SearchProtocol {

    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------

    /**
     * Parameter for the number of walkers at the query initiation. It must be <
     * then view size. Default is 1.
     */
    public static final String PAR_WALKERS = "walkers";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    protected int walkers;

    /** Creates a new instance of RWProtocol */
    public RWProtocol(String prefix) {
        super(prefix);
        walkers = Configuration.getInt(prefix + "." + PAR_WALKERS, 1);
    }

    // "Passive" behaviour implementation: process key similarity and notifies
    // any match and forwards messages.
    public void process(SMessage mes) {
        // checks for hits and notifies originator if any:
        boolean match = this.match(mes.payload);
        if (match)
            this.notifyOriginator(mes);

        // forwards the message to a random neighbor:
        Node neighbor = this.getRNDNeighbor();
        this.forward(neighbor, mes);
    }

    // "active" behaviour implementation: makes query
    public void nextCycle(peersim.core.Node node, int protocolID) {
        super.nextCycle(node, protocolID);
        // this will handle incoming messages

        int[] data = this.pickQueryData(); // if we have to produce a query...
        if (data != null) {
            System.err.println("DATA");
            SMessage m = new SMessage(node, SMessage.QRY, 0, data);
            // System.err.println("sending to " + view.size() + " neighbours: "
            // + m);
            // produces the specified number of walkers:
            for (int i = 0; i < this.walkers && i < this.degree(); i++) {
                this.send((Node) this.getNeighbor(i), m);
            }
        }
    }

}
