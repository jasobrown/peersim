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
package example.loadbalance;

import peersim.core.*;

public class AvgBalance extends BasicBalance {

    public static double average = 0.0;
    public static boolean avg_done = false;
    // ==================== initialization ================================
    // ====================================================================


    public AvgBalance(String prefix, Object obj) {
	super(prefix, obj);
    }


    // ====================== methods =====================================
    // ====================================================================

    // Calculates the average; it's called once at starting.
    private static void calculateAVG(int protocolID) {
	int len = Network.size();
	double sum = 0.0;
	for (int i = 0; i < len; i++)
	    {
		AvgBalance protocol =
		    (AvgBalance)Network.get(i).getProtocol(protocolID);
		double value = protocol.getValue();
       		sum += value;
		
	    }
	average = sum / len;
	avg_done = true;
    }

    /** Disables a node, shrinking the topology.
    */
    protected static void suspend( Node node ) {
	node.setFailState(Fallible.DOWN);
    }

    // --------------------------------------------------------------------

    /**
     * Using a {@link Linkable} protocol choses a neighbor and performs a
     * variance reduction step.
     */
    public void nextCycle( Node node, int protocolID ) {
	// Do that only once
	if (avg_done == false) {    
	    calculateAVG(protocolID);
	    System.out.println("AVG only once "+average);
	}

	if( Math.abs(value-average) < 1 ) {
		AvgBalance.suspend(node); // switch off node
		return;
	}
	
	if (quota == 0 ) return; // skip this node if it has no quota
	
	Node n = null;
	if (value < average ) {
	    n = getOverloadedPeer(node, protocolID);
	    if (n != null) { doTransfer((AvgBalance)n.getProtocol(protocolID)); } 
	}
	else {
	    n = getUnderloadedPeer(node, protocolID);
	    if (n != null) { doTransfer((AvgBalance)n.getProtocol(protocolID)); } 
	} 
	
       	if( Math.abs(value-average) < 1 ) AvgBalance.suspend(node);
	if (n != null) {
	if( Math.abs( ((AvgBalance)n.getProtocol(protocolID)).value-average) < 1 ) AvgBalance.suspend(n);
	}
    }

    private Node getOverloadedPeer(Node node, int protocolID) {
	int linkableID = Protocols.getLink(protocolID);
	Linkable linkable = (Linkable) node.getProtocol( linkableID );
	
	AvgBalance neighbor=null;
	Node neighborNode = null;
	double maxdiff = 0.0;
	for(int i = 0; i < linkable.degree(); ++i)
	    {
		Node peer = linkable.getNeighbor(i);
		// XXX quick and dirty handling of failure
		if(!peer.isUp()) continue;
		AvgBalance n = (AvgBalance)peer.getProtocol(protocolID);
		if(n.quota==0) continue;
		if(value >= average && n.value >= average) continue;
		if(value <= average && n.value <= average) continue;
		double d = Math.abs(value-n.value); 
		if( d > maxdiff )
		    {
			neighbor = n;
			neighborNode = peer;
			maxdiff = d;
		    }
	    }
	return neighborNode;
    } 

    private Node getUnderloadedPeer(Node node, int protocolID) {
	int linkableID = Protocols.getLink(protocolID);
	Linkable linkable = (Linkable) node.getProtocol( linkableID );
	
	AvgBalance neighbor=null;
	Node neighborNode = null;
	double maxdiff = 0.0;
	for(int i = 0; i < linkable.degree(); ++i)
	    {
		Node peer = linkable.getNeighbor(i);
		// XXX quick and dirty handling of failure
		if(!peer.isUp()) continue;
		AvgBalance n = (AvgBalance)peer.getProtocol(protocolID);
		if(n.quota==0) continue;
		if(value >= average && n.value >= average) continue;
		if(value <= average && n.value <= average) continue;
		double d = Math.abs(value-n.value); 
		if( d < maxdiff )
		    {
			neighbor = n;
			neighborNode = peer;
			maxdiff = d;
		    }
	    }
	return neighborNode;
    } 

}
