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

package aggregation.secure;

import peersim.core.*;
import peersim.util.*;
import peersim.config.*;
import java.util.*;

/**
 * This observer reports statistics about blacklist protocols, such as
 * false/true positives and negatives.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class BlacklistObserver
implements Control
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to select the protocol that is
 * controlled by the blacklist. It is needed to check which nodes are 
 * actually malicious, in order to collect statistics about false/true
 * positives and negatives.
 */
public static final String PAR_PID = "protocol";

/** 
 * String name of the parameter used to select the blacklist protocol.
 */
public static final String PAR_BLID = "blacklist";


//--------------------------------------------------------------------------
// Static fields
//--------------------------------------------------------------------------



//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** The name of this object in the configuration file */
private final String name;

/** Identifier of the controlled protocol */
private final int pid;

/** Identifier of the blacklist protocol */
private final int blid;

/** 
 * For each node, the number of nodes that suspect it. 
 */
private int[] nsuspects;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * Construct a new blacklist observer by reading the configuration
 * parameters.
 */
public BlacklistObserver(String name)
{
	this.name = name;
	pid = Configuration.getPid(name+"."+PAR_PID);
	blid = Configuration.getPid(name+"."+PAR_BLID);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean execute()
{
	/* Check size of nsuspects */
	int size = Network.size();
	if (nsuspects == null || nsuspects.length != size)
	  nsuspects = new int[size];
	for (int i=0; i < size; i++) {
		nsuspects[i] = 0;
	}
	
	/* True positive statistics for each node */
	IncrementalStats nodefp = new IncrementalStats();

	/* False positive statistics for each node */
	IncrementalStats nodetp = new IncrementalStats();

	/* Size statistics for each node */
	IncrementalStats nsize = new IncrementalStats();
	for (int i=0; i < size; i++) {
		Node node = Network.get(i);
		Blacklist rp = (Blacklist) node.getProtocol(blid);
		Iterator it = rp.iterator();
		Set set = new HashSet();
		int count = 0;
		while (it.hasNext()) {
			Node suspect = (Node) it.next();
			if (suspect != null) {
				set.add(suspect);
				nsuspects[suspect.getIndex()]++;
			}
			count++;
		}
		nsize.add(count);
		
		it = set.iterator();

		int fp = 0;
		int tp = 0;
		while (it.hasNext()) {
			Node suspect = (Node) it.next();
			if (suspect != null) {
				if (suspect.getProtocol(pid) instanceof MaliciousProtocol) {
					tp++;
				} else {
					fp++;
				}
			}
		}
		nodetp.add(tp);
		nodefp.add(fp);
	}
	
	/* Compute the number of false/true positive/negative */
	int tp = 0;	// True positive
	int fp = 0;	// False positive
	int tn = 0;	// True negative
	int fn = 0; // False negative
	for (int i=0; i < size; i++) {
		Node node = Network.get(i);
		if (nsuspects[i] > 0) {
			if (node.getProtocol(pid) instanceof MaliciousProtocol) 
				tp++;
			else 
				fp++;
		} else {
			if (node.getProtocol(pid) instanceof MaliciousProtocol)
				fn++;
			else
				tn++;
		}
	}
	Log.println(name, 
		" SIZEMIN  " + nsize.getMin() +
		" SIZEMAX  " + nsize.getMax() +
		" SIZEMINCNT " + nsize.getMinCount() +
		" SIZEMAXCNT " + nsize.getMaxCount() +
		" SIZEAVG  " + nsize.getAverage() +
		" NTPMAX " + nodetp.getMax() +
		" NTPMIN " + nodetp.getMin() +
		" NTPAVG " + nodetp.getAverage() +
		" NTPSTD " + nodetp.getStD() +
		" NFPMAX " + nodefp.getMax() +
		" NFPMIN " + nodefp.getMin() +
		" NFPAVG " + nodefp.getAverage() +
		" NFPSTD " + nodefp.getStD() +
		" TN " + tn +
		" FN " + fn +
		" TP " + tp +
		" FP " + fp 
	);
	return false;
}

//--------------------------------------------------------------------------

}
