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

package aggregation.general;

import peersim.core.*;
import peersim.vector.*;

/**
 * The methods contained in this interface are used to represent a more
 * complete approach to aggregation, where nodes simulate the exchange
 * of messages (instead of reading/writing directly the estimates known
 * to peer nodes) and may not be able to partecipate due to restarting.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface GeneralAggregation 
extends SingleValue
{

/**
 * Simulates the sending of a message to initiate an exchange with a peer
 * node. This method is invoked by the initiator of an exchange on the
 * receiver node of the exchange. If the simulated message is 
 * not lost, a well-behaving receiver should send its current estimate
 * to the initiator through method <code>deliverResponse()</code>, and 
 * update its estimate with the corresponding value.
 * 
 * @param initiator the node that initiated the exchange
 * @param receiver the node that received the exchange invitation
 * @param value the value sent by the initiator
 */
public void deliverRequest(Node initiator, Node receiver, double value);

/**
 * Simulates the sending of a message to respond to a message from a
 * peer node initiating an exchange. This method is invoked by the
 * receiver of an exchange on the initiator node of the exchange. If
 * the simulated message is not lost, a well-behaving initiator should
 * update its estimate with the corresponding value.
 * 
 * @param initiator the node that initiated the exchange
 * @param receiver the node that received the exchange invitation
 * @param value the value sent by the receiver
 */
public void deliverResponse(Node initiator, Node receiver, double value);

/**
 * Returns true if this node has just been created, and cannot 
 * partecipate in an aggregation protocol. If the protocol does
 * not support restarting, it should return false.
 */
public boolean isNew();

}
