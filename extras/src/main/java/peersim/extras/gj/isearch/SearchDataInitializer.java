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

package peersim.extras.gj.isearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class SearchDataInitializer implements Control {

    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------

    /**
     * String name of the parameter used to determine the number of distinct
     * keywords.
     */
    public static final String PAR_KEYWORDS = "keywords";

    /**
     * String name of the parameter used to determine the number of nodes
     * emitting queries (default = network size)
     */
    public static final String PAR_QUERY_NODES = "query_nodes";

    /**
     * String name of the parameter used to determine the maximum number of
     * queries emitted by a single node (default = unlimited).
     * 
     */
    public static final String PAR_MAX_QUERIES = "max_queries";

    /**
     * String name of the parameter used to determine the average time interval
     * between queries for the Poisson distribution (default = 10).
     */
    public static final String PAR_QUERY_INTERVAL = "query_interval";

    /**
     * String name of the parameter that defines the protocol to initialize.
     * Parameter read will has the full name <tt>prefix+"."+PAR_PROT</tt>
     */
    public static final String PAR_PROT = "protocol";

    /**
     * String name of the parameter that defines the maximum number of cycles
     * the simulation will last. Default is 100 cycles. Parameter read will has
     * the full name <tt>prefix+"."+PAR_MAX_CYCLES</tt>
     */
    public static final String PAR_MAX_CYLES = "max_cycles";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    /**
     * Distribution used to generate the required random numbers.
     */
    private final long keywords;

    private final int query_nodes;

    private final int max_queries;

    private final int query_interval;

    private final int protocolID;

    private final long maxCycles;

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public SearchDataInitializer(String prefix) {
        keywords = Configuration.getLong(prefix + "." + PAR_KEYWORDS);
        protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
        query_nodes = Configuration.getInt(prefix + "." + PAR_QUERY_NODES,
                Network.size());
        max_queries = Configuration.getInt(prefix + "." + PAR_MAX_QUERIES, 0);
        query_interval = Configuration.getInt(
                prefix + "." + PAR_QUERY_INTERVAL, 10);
        maxCycles = Configuration.getInt(prefix + "." + PAR_MAX_CYLES, 100);
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    /**
     * Fills a {@link SearchProtocol} with the keywords representing the
     * documents it holds. It is called for each node.
     * 
     * @param proto
     *            the protocol instance to initialize
     */
    private void initializeData(SearchProtocol proto) {
        // create the stored keywords
        // number of keywords held by the node (poisson)
        int storageSize = CommonState.r.nextPoisson(1 + keywords / 1000);
        /* generates a distribution of keyIDs along with their frequencies */
        Map<Integer, Integer> keyStorage = makeKeyMap(storageSize);
        proto.addKeyStorage(keyStorage);
    }

    /**
     * Fills a {@link SearchProtocol} with the queries it will initiate.
     * 
     * @param proto
     *            the protocol to initialize
     */
    private void initializeQueries(SearchProtocol proto) {
        int cycle = -1;
        int nqueries = 0;
        while (true) {

            cycle += (1 + CommonState.r.nextPoisson(query_interval));
            System.err.println("cycle: " + cycle);
            if (cycle > (maxCycles - proto.ttl)) {
                System.err
                        .println("Warn: the TTL probably too large for maxCycles...");
                break;
            }
            if (max_queries > 0 && ++nqueries > max_queries) {
                // System.err.println("Warn: ");
                break;
            }
            // number of keywords in the query (exponential)
            int querySize = (int) Math.ceil(nextExponential(1.5));
            System.err.println("query size: " + querySize);
            int[] query = makeKeyArray(querySize);
            proto.addQueryData(cycle, query);
        }
    }

    /**
     * Generate a set of (keyID, frequency) pairs of the specified size.
     * 
     * @param size
     *            The size of the desired set
     * @return
     */
    private Map<Integer, Integer> makeKeyMap(int size) {
        HashMap<Integer, Integer> keys = new HashMap<Integer, Integer>();
        while (keys.size() < size) {
            int key = (int) Math.ceil(nextPower(keywords / (double) 100, 1.0));
            if (key > keywords) /*
                                 * keyIDs grater than the max key number are
                                 * trashed
                                 */
                continue;
            Integer ikey = Integer.valueOf(key);
            Integer oldValue = (Integer) keys.get(ikey);
            int newval = (oldValue == null ? 1 : oldValue.intValue() + 1);
            keys.put(ikey, Integer.valueOf(newval));
        }

        return keys;
    }

    private int[] makeKeyArray(int size) {
        HashSet<Integer> keys = new HashSet<Integer>();
        while (keys.size() < size) {
            int key = (int) Math.ceil(nextPower(keywords / (double) 100, 1.0));
            if (key > keywords)
                continue;
            keys.add(Integer.valueOf(key));
        }

        int[] result = new int[size];
        Object[] ikeys = keys.toArray();
        for (int i = 0; i < size; ++i) {
            result[i] = ((Integer) ikeys[i]).intValue();
        }
        return result;
    }

    public boolean execute() {
        for (int i = 0; i < Network.size(); ++i) {
            SearchProtocol proto = ((SearchProtocol) Network.get(i)
                    .getProtocol(protocolID));
            initializeData(proto);
            if (i < query_nodes)
                initializeQueries(proto);
        }
        return false;
    }

    /**
     * Extracts random values according to a power distribution.
     * 
     * @param base
     *            base
     * @param a
     *            scaling exponent
     * @return
     */
    private static double nextPower(double base, double a) {
        return base / Math.pow(CommonState.r.nextDouble(), a) - base;
    }

    public double nextExponential(double b) {
        return -1 * b * Math.log(CommonState.r.nextDouble());
    }

}
