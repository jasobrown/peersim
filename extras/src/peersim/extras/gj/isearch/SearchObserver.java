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

import java.util.HashMap;
import java.util.Iterator;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class SearchObserver implements Control {
    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------
    /**
     * String name of the parameter used to select the protocol to operate on
     */
    public static final String PAR_PROT = "protocol";

    /**
     * String name of the parameter used to set the verbosity level. The default
     * is 0 (non verbose).
     */
    public static final String PAR_VERBOSITY = "verbosity";

    /**
     * String name of the parameter used to force the non removal of ttl expired
     * messages from the node memory. The default is false (not remove).
     */
    public static final String PAR_CLEAN_CACHE = "clean_cache";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    /** The name of this observer in the configuration */
    protected final String name;

    /** Protocol identifier */
    protected final int pid;

    protected final int verbosity;

    protected final int len = Network.size();

    // To remove or not the ttl expired messages from cache.
    protected boolean cleanCache;

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public SearchObserver(String name) {
        this.name = name;
        // Other parameters from config file:
        pid = Configuration.getPid(name + "." + PAR_PROT);
        verbosity = Configuration.getInt(name + "." + PAR_VERBOSITY, 0);
        cleanCache = Configuration.contains(name + "." + PAR_CLEAN_CACHE);
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    public boolean execute() {
        HashMap<SMessage, SearchStats> messageStats = new HashMap<SMessage, SearchStats>();
        int time = CDState.getCycle();

        for (int i = 0; i < len; i++) {
            SearchProtocol prot = (SearchProtocol) Network.get(i).getProtocol(
                    pid);
            int ttl = prot.ttl;
            Iterator iter = prot.messageTable.keySet().iterator();
            while (iter.hasNext()) {
                // System.err.println("has next!");
                SMessage msg = (SMessage) iter.next();
                int age = time - msg.start - 1;
                // if (age > ttl) continue;
                Integer msgValue = (Integer) prot.messageTable.get(msg);
                int msgs = msgValue.intValue();
                // System.out.println("Copies: "+msgs);
                int hits = (prot.hitTable.contains(msg) ? 1 : 0);
                SearchStats stats = (SearchStats) messageStats.get(msg);
                if (stats == null) {
                    stats = new SearchStats(msg.seq, age, ttl);
                    messageStats.put(msg, stats);
                }
                stats.update(msgs, hits);

                if (cleanCache && age >= ttl) {
                    iter.remove();
                    prot.hitTable.remove(msg);
                    prot.routingTable.remove(msg);
                    prot.messageTable.remove(msg);
                }
            }
        }

        printItemsStatistics(messageStats);

        return false;
    }


    public void printItemsStatistics(HashMap messageStats) {
        Iterator iterStats = messageStats.values().iterator();
        while (iterStats.hasNext()) {
            SearchStats stats = (SearchStats) iterStats.next();
            if (verbosity == 0 && stats.getAge() < stats.getTtl())
                continue;
            System.out.println(name + ": " + CommonState.getIntTime() + " "
                    + stats.toString());
            // System.out.println(stats);
        }
    }

}
