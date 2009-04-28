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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

/**
 * 
 * @author Gian Paolo Jesi
 */
public abstract class SearchProtocol implements CDProtocol, Linkable {

    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------

    /**
     * The Linkable enabled protocol to fetch neighbours from. If it is not
     * defined, then the SearchProtocol itself is used as a Linkable. Deafult:
     * not set.
     * 
     * @config
     */
    public static final String PAR_LINKABLE = "linkable";

    /**
     * The messages TTL size
     * 
     * @config
     */
    public static final String PAR_TTL = "ttl";

    /**
     * Parameter for the proliferation factor.
     * 
     * @config
     */
    public static final String PAR_PROLIFERATION = "proliferation";

    /**
     * Parameter to choose which key comparation approach is preferred. Default
     * is OR.
     * 
     * @config
     */
    public static final String PAR_ANDMATCH = "and_keys";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    /**
     * Stores each message that a node has seen. It may be cleaned by the an
     * observer considering how long a message can be valid (TTL).
     * 
     */
    public HashMap<SMessage, Integer> messageTable;

    /**
     * Stores each message that a node has seen. It may be cleaned by the an
     * observer considering how long a message can be valid (TTL).
     */
    public HashSet<SMessage> hitTable;

    /**
     * Stores the incoming received messages. The send() and forward() methods
     * can write here. Is up to the protocol behaviour processing all the queue
     * content during the cycle.
     */
    public ArrayList<SMessage> incomingQueue;

    /** The neighbor list view of the current node */
    protected ArrayList<Node> view;

    /** The local node search key storage */
    protected HashMap<Integer, Integer> keyStorage;

    /** Query distribution data structure; it holds the cycle and a key array. */
    protected TreeMap<Integer, Object> queryDistro;

    /** Counter for thecurrent node extra probing mesages. */
    protected int extraProbeCounter;

    protected int viewSize, ttl, pid;

    protected double pFactor;

    protected boolean andMatch;

    private int linkID; // Linkable id: may be the same as protocolID

    public Node whoAmI; // a reference the the protocol own node

    public HashMap<SMessage, HashMap<Node, Integer>> routingTable = null;

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public SearchProtocol(String prefix) {
        this.extraProbeCounter = 0;
        ttl = Configuration.getInt(prefix + "." + PAR_TTL, 5);
        pFactor = Configuration
                .getDouble(prefix + "." + PAR_PROLIFERATION, 1.0);
        int match = peersim.config.Configuration.getInt(prefix + "."
                + PAR_ANDMATCH, 0);
        if (match == 1)
            this.andMatch = true;
        else
            this.andMatch = false;
        try {
            linkID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
        } catch (MissingParameterException mpe) {
            linkID = -1;
        }
        if (linkID == -1)
            linkID = CommonState.getPid();
        pid = CommonState.getPid();
        view = new ArrayList<Node>();
        messageTable = new HashMap<SMessage, Integer>();
        hitTable = new HashSet<SMessage>();
        incomingQueue = new ArrayList<SMessage>();
        queryDistro = new TreeMap<Integer, Object>();
        keyStorage = new HashMap<Integer, Integer>();
        routingTable = new HashMap<SMessage, HashMap<Node, Integer>>();
        System.out.println("SearchProtocol instance using linkID: " + linkID
                + " ,pid: " + pid);
    }

    public Object clone() {
        SearchProtocol sp = null;
        try {
            sp = (SearchProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        sp.view = new ArrayList<Node>();
        sp.keyStorage = new HashMap<Integer, Integer>();
        sp.messageTable = new HashMap<SMessage, Integer>();
        sp.hitTable = new HashSet<SMessage>();
        sp.incomingQueue = new ArrayList<SMessage>();
        sp.queryDistro = new TreeMap<Integer, Object>();
        sp.routingTable = new HashMap<SMessage, HashMap<Node, Integer>>();
        return sp;
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    // interface CDProtocol:
    public void nextCycle(Node node, int protocolID) {
        // currentIndex = node.getIndex();
        int currentTime = CDState.getCycle();
        if (currentTime == 0 || whoAmI == null)
            whoAmI = node;

        Iterator iter = incomingQueue.iterator();

        while (iter.hasNext()) {
            SMessage mes = (SMessage) iter.next();
            // if (mes.hops == (currentTime - mes.start + 1))
            // continue;
            Integer actual = (Integer) this.messageTable.get(mes);
            int index = (actual != null ? actual.intValue() + 1 : 1);
            this.messageTable.put(mes, Integer.valueOf(index));
            // int before = incomingQueue.size();
            this.process(mes);
            iter.remove();

            // System.err.println("Queue size: " + before + " : "
            // + incomingQueue.size() + " node " + whoAmI.getID());
        }

        // DEBUG:
        if (!incomingQueue.isEmpty())
            System.err.println("Warning: incoming queue not empty at node: "
                    + whoAmI.getID());
    }

    /**
     * Send a message to a Node. Used by the query originator. It takes care to
     * increase the message TTL.
     * 
     * @param n
     *            The node to communicate with.
     * @param mes
     *            The message to be fsent.
     */
    public void send(Node n, SMessage mes) {
        try {
            SMessage copy = (SMessage) mes.clone();
            copy.hops++;
            this.messageTable.put(mes, Integer.valueOf(1));

            // store to whom the message is sent and how many copies
            updateRoutingTable(n, mes);

            SearchProtocol sp = (SearchProtocol) n.getProtocol(pid);
            // System.err.println(
            // "send: " + currentIndex + " -> " + n.getIndex() + ": " + copy);
            sp.incomingQueue.add(copy);
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Forwards a message to a node. Used by the nodes along the message path.
     * It takes care to increase the message TTL and stops forwarding if it is
     * too high.
     * 
     * @param n
     *            The node to communicate with.
     * @param mes
     *            The message to be forwarded.
     */
    public void forward(Node n, SMessage mes) {
        // NOTE: it does not insert the message in the neighbor messageTable
        // because I belive that this operation has to performed by the
        // neighbor itselt; the idea is that a node has "to see" by itself.
        if (mes.hops < ttl) {
            try {
                // clone message and update TTL:
                SMessage copy = (SMessage) mes.clone();
                copy.hops++;
                SearchProtocol sp = (SearchProtocol) n.getProtocol(pid);
                // copy.type = SMessage.FWD; // sets FWD type
                // System.err.println(
                // "forward: "
                // + currentIndex
                // + " -> "
                // + n.getIndex()
                // + ": "
                // + copy);
                // store to whom the message is sent and how many copies
                updateRoutingTable(n, mes);
                sp.incomingQueue.add(copy);
            } catch (CloneNotSupportedException cnse) {
                System.out.println("Troubles with message cloning...!");
            }
        }
    }

    /**
     * Store in the local routingTable the message, the destination and how many
     * time the packet has been sento to the destination.
     * 
     * @param n
     *            The destination node.
     * @param mes
     *            The message to be sent.
     */
    protected void updateRoutingTable(Node n, SMessage mes) {
        HashMap<Node, Integer> map = this.routingTable.get(mes);
        if (map == null) { // 1st send of this mes
            HashMap<Node, Integer> entry = new HashMap<Node, Integer>();
            entry.put(n, 1);
            this.routingTable.put(mes, entry);
        } else {// mes already sent to somoeone
            Integer howmany = map.get(n);
            if (howmany == null) { // never sent to node n:
                map.put(n, 1);
            } else { // mes already sent to n:
                int k = howmany.intValue() + 1;
                map.put(n, k);
                // System.out.println("OLTRE 1!! " + k);
            }
        }

    }

    /**
     * Selects a random node from the current view. The current Linkable
     * protocol view is used according to the configuration.
     * 
     * @return A random picked node.
     */
    public Node getRNDNeighbor() {
        Node result = null;

        if (pid == linkID)
            result = (Node) this.view.get(CommonState.r.nextInt(this.view
                    .size()));
        else {
            Linkable l = (Linkable) whoAmI.getProtocol(linkID);
            result = l.getNeighbor(CommonState.r.nextInt(l.degree()));
        }
        return result;
    }

    /**
     * Select a free node from the current view. It is used by the restricted
     * protocol versions. It always return a random node even if no free node
     * are available.
     * 
     * @param mes The message for which the nodes must be "free".
     * @return A "free" node.
     */
    public Node selectFreeNeighbor(SMessage mes) {
        ArrayList<Node> tempList;
        if (pid == linkID)
            tempList = this.view;
        else { // copy the linkable view:
            tempList = new ArrayList<Node>();
            Linkable l = (Linkable) whoAmI.getProtocol(linkID);
            for (int i = 0; i < l.degree(); i++)
                tempList.add(l.getNeighbor(i));
        }

        Collections.shuffle(tempList, CommonState.r); // same as random pick
        Node result = null;

        for (int i = 0; i < tempList.size(); i++) {
            this.extraProbeCounter++;
            Node n = (Node) tempList.get(i);
            SearchProtocol sp = (SearchProtocol) n.getProtocol(pid);
            if (!sp.messageTable.containsKey(mes)) {
                result = n;
                break;
            }
        }

        if (result == null) {
            result = (Node) tempList.get(0);
        }
        return result;
    }

    /**
     * It is the equivalent of the passive thread in real setup. It has to
     * manage incoming message requests that are available in the incomingQueue
     * data structure. BTW this method has to deal with only a single message at
     * a time and the basic nextCycle method of CDProtocol will provide the
     * available messages.
     * 
     * @param mes
     *            The message to process.
     */
    public abstract void process(SMessage mes);

    /**
     * Notify a hit in the current node messageTable. The absolute value
     * indicates how many times the message has been seen and the negative sign
     * indicates a succesful query hit.
     * 
     * @param mes
     *            The message for which it notifies a hit.
     */
    public void notifyOriginator(SMessage mes) {
        this.hitTable.add(mes);
    }

    /**
     * Performs the actual checks to compare query keys to the protocol own key
     * storage. The returning array may be NULL elements.
     * 
     * @param keys
     *            The array of keys to be checked, it is extracted from the
     *            message.
     * @return A new array of keys: the ones that has matched. It may be null if
     *         no keys has matched.
     */
    protected int[] matches(int[] keys) {
        int[] result = null;
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (int i = 0; i < keys.length; i++) {
            if (this.keyStorage.containsKey(Integer.valueOf(keys[i]))) {
                temp.add( Integer.valueOf(keys[i]));
            }
        }
        if (temp.size() > 0) {
            result = new int[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                result[i] = temp.get(i);
            }
        }
        return result;
    }

    /**
     * Performs the actual checks to compare query keys to the protocol own key
     * storage. It returns if a hit has happended according to the actual
     * comparison method (AND or OR).
     * 
     * @param keys
     *            The array of keys to be checked, it is extracted from the
     *            message.
     * @return If a hit has occurred.
     */
    protected boolean match(int[] keys) {
        boolean result = false;
        int[] matchedKeys = this.matches(keys);
        if (matchedKeys != null) {
            // and match
            if (andMatch && matchedKeys.length == keys.length)
                result = true;
            else if (!andMatch)
                result = true;
        }
        return result;
    }

    /**
     * Picks the current node query data distribution to send a message from the
     * local node distribution structure.
     * 
     * @return The set of keys in the query according to the distribution.
     */
    protected int[] pickQueryData() {
        int[] result = null;

        if (queryDistro.isEmpty())
            return null;

        Integer key = (Integer) queryDistro.firstKey();
        // new Integer(CommonState.getT());
        if (key.intValue() == CDState.getCycle()) {
            result = (int[]) queryDistro.get(key);
            queryDistro.remove(key);
        }

        return result;
    }

    /**
     * Load the current node query data distribution structure.
     * 
     * @param cycle
     *            The cycle in which perform the query.
     * @param keys
     *            The query set.
     */
    public void addQueryData(int cycle, int[] keys) {
        // System.err.print("adding query: ");
        // for (int i = 0; i < keys.length; ++i)
        // System.err.print(keys[i] + " ");
        // System.err.println(" scheduled @" + cycle);

        this.queryDistro.put( Integer.valueOf(cycle), (Object) keys);
    }

    /**
     * Sets the node specific keys collection and their own frequency. Should be
     * called by the initializer.
     * 
     * @param entry
     *            A mapping from a key to its frequency.
     */
    public void addKeyStorage(Map<Integer, Integer> entry) {
        this.keyStorage.putAll(entry);
    }

    // Linkable interface implementation:
    public boolean addNeighbor(peersim.core.Node neighbour) {
        view.add(neighbour);
        return true;
    }

    public boolean contains(peersim.core.Node neighbor) {
        return this.view.contains(neighbor);
    }

    public int degree() {
        if (pid == linkID)
            return this.view.size();
        else {
            Linkable l = (Linkable) whoAmI.getProtocol(linkID);
            return l.degree();
        }
    }

    public peersim.core.Node getNeighbor(int i) {
        Node result = null;
        if (pid == linkID) {
            result = (Node) this.view.get(i);
        } else { // uses another Linkable
            Linkable l = (Linkable) whoAmI.getProtocol(linkID);
            result = l.getNeighbor(i);
        }
        return result;
    }

    public void pack() {
        ;
    }

    /*
     * @see peersim.core.Cleanable#onKill()
     */
    public void onKill() {
        viewSize = 0;
        view.clear();
        messageTable.clear();
        hitTable.clear();
        incomingQueue.clear();
        routingTable.clear();
        keyStorage.clear();
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

}