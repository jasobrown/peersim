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

package peersim.extras.am.epidemic.ring;


import java.util.*;


import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class RingObserver implements Control
{

// --------------------------------------------------------------------------
// Constants
// --------------------------------------------------------------------------
/** The protocol to be observed */
public static final String PAR_PROT = "protocol";

/** If present, the protocol will stop when the ring has been obtained */
public static final String PAR_STOP = "stop";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------
/** Identifier of the protocol that hosts the protocol id */
private final int pid;

/** If true, the protocol will stop when the ring has been obtained */
private final boolean stop;

/** Observer prefix to be printed */
private final String prefix;

/** True when the ring has been obtained */
private boolean endtime = false;

/** Node array, used to detect the ring */
private Node[] nodes;

/** */
private int[] count;

// --------------------------------------------------------------------------
// Constants
// --------------------------------------------------------------------------
/**
 * 
 */
public RingObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	stop = Configuration.contains(prefix + "." + PAR_STOP);
}

// Comment inherited from interface
public boolean execute()
{
	Comparator idc = new IDNodeComparator(pid);
	int size = Network.size();
	if (nodes == null || nodes.length != size) {
		nodes = new Node[size];
		count = new int[size];
		for (int i = 0; i < size; i++) {
			nodes[i] = Network.get(i);
		}
		Arrays.sort(nodes, idc);
//		for (int i=1; i < nodes.length; i++) {
//			System.out.println("II " + IDUtil.getID(nodes[i], pid));
		// if (IDUtil.getID(nodes[i], pid) == IDUtil.getID(nodes[i-1], pid))
		// System.out.println("!!!");
//		}
	}
	int count = 0;
  for (int i=0; i < size; i++) {
  	int l = (i-1+size)%size;
  	int r = (i+1)%size;
  	long id = IDUtil.getID(nodes[i], pid);
  	long dl = ID.dist(id, IDUtil.getID(nodes[l], pid));
  	long dr = ID.dist(id, IDUtil.getID(nodes[r], pid));
//  	System.out.println(dl + " --> " + dr);
		Linkable link = (Linkable) nodes[i].getProtocol(pid);
  	if (dl < dr) {
  		if (link.contains(nodes[l]))
  		  count++;
//  		else if (CommonState.getTime()>10) {
//  			System.out.println("L -->" + i);
//  			for (int j=0; j < link.degree(); j++) {
//  				System.out.println(IDUtil.getID(link.getNeighbor(j), pid));
//  			}
//  			System.out.println("-------");
//  			for (int j=-4; j < 5; j++) {
//  				System.out.println(IDUtil.getID(link.getNeighbor((i+j+size)%size), pid));
//  			}
//  		}
  	} else if (dl > dr) {
  		if (link.contains(nodes[r]))
  			count++;
//  		else if (CommonState.getTime()>10) {
//  			System.out.println("R -->" + i);
//  			for (int j=0; j < link.degree(); j++) {
//  				System.out.println(IDUtil.getID(link.getNeighbor(j), pid));
//  			}
//  			System.out.println("-------");
//  			for (int j=-4; j < 5; j++) {
//  				System.out.println(IDUtil.getID(link.getNeighbor((i+j+size)%size), pid));
//  			}
//  		} 
  	} else {
	  	System.out.println("!!!!!!!!" + dl + " --> " + dr);
		} 		
  }
	System.out.println(prefix + ": " +  " TIME " + CommonState.getTime() + " PRESENT " + count + " SIZE " + size + " " + ((float) count)/size);
	return count == size;
}
}