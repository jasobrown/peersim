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

package aggregation;

import peersim.util.CommonRandom;
import peersim.core.*;
import peersim.config.*;

/**
 *  This class emulates
 *
 *  @author Alberto Montresor
 *  @version $Revision$
 */
public class FallibleAverageFunction 
extends AbstractFunction
{

	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////

	private final static String PAR_FAILUREPROB = "failure";

	private final static String PAR_STYLE = "style";


	////////////////////////////////////////////////////////////////////////////
	// Static fields
	////////////////////////////////////////////////////////////////////////////

	/** Failure probability */
	private static double failureProb;

	/** True if the failure style is atomic */
	private static boolean atomic;

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

  /** 
   * This flag is set to true when a node is created. Node that are created
   * during an epoch do not partecipate in the aggregation protocol for that
   * epoch. When the value is re-initialized, nodes are re-created equal.
   */
	private boolean justArrived;


	////////////////////////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////////////////////////

	public FallibleAverageFunction(String prefix, Object obj)
	{
		super(prefix, obj);
		failureProb = Configuration.getDouble(prefix + "." + PAR_FAILUREPROB);
		String style = Configuration.getString(prefix + "." + PAR_STYLE, "atomic");
		atomic = ("atomic".equalsIgnoreCase(style));
		justArrived = true;
	}

	public Object clone() throws CloneNotSupportedException
	{
		FallibleAverageFunction af = (FallibleAverageFunction) super.clone();
		af.value = value;
		af.justArrived = true;
		return af;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Using a {@link Linkable} protocol choses a neighbor and performs a
	 * variance reduction step.
	 */
	public void nextCycle(Node node, int pid)
	{
		/* Nodes that have been created during the current epoch do not
		 * partecipate in the aggregation protocol
		 */
		if (justArrived)
		  return;
		
		Node rnode = selectNeighbor(node, pid);

		/* Quick and dirty check whether the selected node is up and running.
		 * If not, we just ignore the fact.
		 */
		if (rnode == null || !rnode.isUp())
			return;

		FallibleAverageFunction rpeer = 
			(FallibleAverageFunction) rnode.getProtocol(pid);
		
		/* Sends an exchange request to the remote node that has
		 * been selected. To simulate message losses, we perform
		 * a random test on failure probability. In case of
		 * communication failure, the exchange request is not
		 * delivered.
		 */
		if (failureProb == 0.0 || CommonRandom.r.nextDouble() >= failureProb) {
			rpeer.deliverRequest(rnode, this, getValue());
		} 
		
		/* Here, we should wait for a response. Since we are
		 * using a single-threaded simulation, we let the
		 * remote peer to invoke deliverResponse on this
		 * protocol object. So, the waiting section of the
		 * code is not represented.
		 */
	}
	
	/**
	 * 
	 * @param lnode the node that hosts this protocol
	 * @param rpeer the remote protocol that performed this invocation
	 * @param rvalue the value stored at the remote protocol
	 */
	protected void deliverRequest(Node lnode, FallibleAverageFunction rpeer, 
		double rvalue)
	{
		/* Nodes that have been created during the current epoch do not
		 * partecipate in the aggregation protocol
		 */
		if (justArrived)
			return;

		double lvalue = this.value;
		this.value = (lvalue + rvalue)/2;
		/* If the failure mode is atomic, we send back a response.
		 * If the failure mode is not atomic, we perform
		 * a random test on failure probability. In case of
		 * communication failure, the exchange response is not
		 * delivered.
		 */
		if (failureProb == 0.0 || atomic || 
		    CommonRandom.r.nextDouble() >= failureProb) {
			rpeer.deliverResponse(lvalue);
		}
	}
	
	/**
	 * 
	 * @param rvalue the value stored at the remote protocol
	 */
	protected void deliverResponse(double rvalue)
	{
		/* Here, we do not check whether justArrived is true or false.
		 * The reason for this is that in our simulation, no nodes
		 * send a response without having sent a request. In the
		 * reality, every node can easily discard responses for which
		 * it has not sent any request. 
		 */
		 
		/* Update the value */
		this.value = (this.value + rvalue)/2;
	}

  protected Node selectNeighbor(Node node, int pid)
  {
		int linkableID = Protocols.getLink(pid);
		Linkable linkable = (Linkable) node.getProtocol(linkableID);
		Node rnode = null;
		if (linkable.degree() > 0) {
			rnode =
				linkable.getNeighbor(CommonRandom.r.nextInt(linkable.degree()));
		}
		return rnode; 
  }

}
