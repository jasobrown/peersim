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
 
 import example.aggregation.AbstractFunction;
 import peersim.config.Configuration;
 import peersim.core.*;
 
 public class BasicBalance extends AbstractFunction {
 // Fields:
 public static final String PAR_QUOTA = "quota"; // allowed config file parameter
 private final double quota_value; // original quota value taken from configuration
 
 protected double quota; // current cycle quota
 
 // Constructor:
 public BasicBalance(String prefix, Object obj) {
 	super(prefix, obj);
  	// get quota value from the config file. Default 1.
 	quota_value = (double)(Configuration.getInt(prefix+"."+PAR_QUOTA, 1));
 	quota = quota_value;
 }
 
 // Resets the quota. 
 protected void resetQuota() {
 	this.quota = quota_value;
 }
 
 // Implements the Protocol interface
 public Object clone() throws CloneNotSupportedException {
 	BasicBalance af = (BasicBalance)super.clone();
 	return af;
 }
 
 // Implements CDProtocol interface
 public void nextCycle( Node node, int protocolID ) {
 	int linkableID = Protocols.getLink(protocolID);
 	Linkable linkable = (Linkable) node.getProtocol( linkableID );
	if (this.quota == 0) {
 		return; // skip this node
 	} 
 	// this takes the most distant neighbor based on local load
 	BasicBalance neighbor = null;
 	double maxdiff = 0;
 	for(int i = 0; i < linkable.degree() ; ++i)
 	{
 		Node peer = linkable.getNeighbor(i);
 		// The selected peer could be inactive
 		if(!peer.isUp()) continue; 
 		BasicBalance n = (BasicBalance)peer.getProtocol(protocolID);
 		if(n.quota!=1.0) continue;
 		double d = Math.abs(value-n.value);
 		if( d > maxdiff ) {
			neighbor = n;
			maxdiff = d;
		}
	}
 	if( neighbor == null ) {
 		return;
	 }
 doTransfer(neighbor);
 }
 
 // Performs the actual load exchange selecting to make a PUSH or PULL approach.
 // It affects the involved nodes quota. 
 protected void doTransfer(BasicBalance neighbor) {
 	double a1 = this.value;
 	double a2 = neighbor.value;
 	double maxTrans = Math.abs((a1-a2)/2);
 	double trans = Math.min(maxTrans,quota);
 	trans = Math.min(trans, neighbor.quota);
 
 	if( a1 <= a2 ) // PULL
 	{
 		a1+=trans;
 		a2-=trans;
 	}
 	else // PUSH
 	{
 		a1-=trans;
 		a2+=trans;
 	}
  
 	this.value = a1;
 	this.quota -= trans;
 	neighbor.value = a2;
 	neighbor.quota -= trans;
 }
 
}
 
