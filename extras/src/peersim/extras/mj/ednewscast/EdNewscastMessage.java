/*
 * Copyright (c) 2008 M. Jelasity and N. Tolgyesi
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

package peersim.extras.mj.ednewscast;

import peersim.core.*;

/**
 * This class represents the communication message in the system; both the
 * proactive and reactive (response) message.
 */

class EdNewscastMessage {

	// --------------------------------------------------------------------------
	// Fields
	// --------------------------------------------------------------------------

	/** sender adress */
	public final Node sender;
	
	/** node adress list to send */
	public final Node[] cacheM; 
	
	/** time stamps to send */
	public final int[] tstampsM; 
	
	/** type of message */
	private final boolean isAnswer; 

	// --------------------------------------------------------------------------
	// Initialization
	// --------------------------------------------------------------------------
	
	public EdNewscastMessage(Node senderP, Node[] cacheMP, int[] tstampsMP, boolean isAnswerP) {
		isAnswer = isAnswerP;
		sender = senderP;
		cacheM = new Node[cacheMP.length];
		tstampsM = new int[tstampsMP.length];

		for (int i = 0; i < cacheMP.length; i++) {
			cacheM[i] = cacheMP[i];
			tstampsM[i] = tstampsMP[i];
		}
	}
	
	// --------------------------------------------------------------------------
	// Public methods
	// --------------------------------------------------------------------------

	/**
	 * This function returns with a boolean that the message is an answer or not.
	 */	
	public boolean isAnswer() {
		return isAnswer;
	}

	/**
	 * This function returns the degree of the cache in the message.
	 */	
	public int degree() {
		int len = cacheM.length - 1;
		while (len >= 0 && cacheM[len] == null)
			len--;
		return len + 1;
	}

}
