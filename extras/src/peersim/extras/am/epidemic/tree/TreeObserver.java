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

package peersim.extras.am.epidemic.tree;


import java.util.*;


import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;
import peersim.util.*;


public class TreeObserver implements Control
{

/** The protocol to be observed */
public static final String PAR_PROT = "protocol";

/** If present, the protocol will stop when the tree has been obtained */
public static final String PAR_STOP = "stop";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------
/** Identifier of the protocol that hosts the protocol id */
private final int pid;

/** If true, the protocol will stop when the tree has been obtained */
private final boolean stop;

/** Observer prefix to be printed */
private final String prefix;

/** True when the tree has been obtained */
private boolean endtime = false;

/** Node array, used to detect the ring */
private Node[] nodes;


public TreeObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	stop = Configuration.contains(prefix + "." + PAR_STOP);
}

public boolean execute()
{
	Comparator idc = new IDNodeComparator(pid);
	int size = Network.size();
	if (nodes == null || nodes.length != size) {
		nodes = new Node[size];
		for (int i = 0; i < size; i++) {
			nodes[i] = Network.get(i);
		}
		Arrays.sort(nodes, idc);
	}
  IncrementalFreq stats = new IncrementalFreq();
  int n_parent = 0;
  int n_left = 0;
  int n_right = 0;
  int tot = 0;
	for (int i = 0; i < size; i++) {
		if (!nodes[i].isUp())
			continue;
		tot++;
		long id = IDUtil.getID(nodes[i], pid);
		long id_parent = id >> 1;
	  long id_left = id << 1;
	  long id_right = id_left | 1;
//	  System.out.println(dist(id, id_parent,32));
//	  System.out.println(dist(id, id_left,32));
//	  System.out.println(dist(id, id_right,32));
//	  System.out.println("-----------");
	  Linkable l = (Linkable) nodes[i].getProtocol(pid);
	  int degree = l.degree();
	  int count = 0;
//	  System.out.print(Long.toBinaryString(id) + " ");
	  for (int j=0; j < degree && count < 3; j++) {
	  	long peer_id = IDUtil.getID(l.getNeighbor(j), pid);
	  	if (peer_id == id_parent) {
	  		n_parent++;
	  		count++;
//	  		System.out.print("P");
	  	}
	  	else if (peer_id == id_left) {
	  		n_left++;
	  		count++;
//	  		System.out.print("L");
	  	}
	  	else if (peer_id == id_right) {
	  		n_right++;
	  		count++;
//	  		System.out.print("R");
	  	}
	  }
//	  System.out.println(" " + count);
	  stats.add(count);
	}
	System.out.println(prefix + ": " +
			(n_parent + n_left + n_right == size-1+size-1 ? 
		  "ENDTIME " : "TIME " )+	CommonState.getTime() +
		  " SIZE " + tot +
			" P,L,R " + n_parent + " " + n_left + " " + n_right + 
			" STATS " + stats);
	
	if (!stop)
		return false;
	else
		return (n_parent + n_left + n_right == size-1+size-1);
}

public static long dist( long a, long b, long bits ) {
//XXX this implementation fails if a or b is ~0

//	a+=1;
//	b+=1;
	long alevel=bits;
	long blevel=bits;
	long commonprefix=0;
	long mask = (long)1 << bits-1;
	while( (mask & a) == 0 )
	{
		a <<= 1;
		alevel--;
	}
	while( (mask & b) == 0 )
	{
		b <<= 1;
		blevel--;
	}
	long length = Math.min(alevel,blevel);
	while( (mask & ~(a ^ b)) != 0 && length>0)
	{
		b <<= 1;
		a <<= 1;
		commonprefix++;
		length--;
	}

	return alevel-commonprefix+blevel-commonprefix;
}


}
