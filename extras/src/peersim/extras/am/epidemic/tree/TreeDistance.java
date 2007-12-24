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

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.epidemic.xtman.*;
import peersim.extras.am.id.*;
import cern.colt.*;


public class TreeDistance implements Distance
{

private final TreeIDComparator idc;

private Node[] nodes;

/** 
 * This parameter is used to specify the protocol
 * that must be used to obtain IDs.
 */
private static final String PAR_PROTOCOL = "protocol";

/** 
* Config parameter defining the number of bits necessary to describe the
* largest ID node. It is not critical to set it, if not set, 32 is used,
* which results in the same distances only slower execution.
* On the other hand, if it is too small, infinite loops will be the result
* so care should be taken.
*/
public static final String PAR_BITS = "bits";


/** The protocol identifier */
private final int pid;

/** Number of bits */
private final int bits;

public TreeDistance(String prefix)
{
	nodes = new Node[Network.size()];
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	bits = Configuration.getInt(prefix + "." + PAR_BITS, 32);
	idc = new TreeIDComparator(pid, bits);
}


/**
 * Assumptions:
 * <ul>
 *   <li> the array set is ordered with respect to node ids
 *   <li> the array set
 */
public int rank(Node[] src, int ssize, Node node, Node[] dest, int dsize)
{
	idc.setID(IDUtil.getID(node, pid));
	//System.out.print(ssize + " ");
	int msize = Math.min(dsize, ssize);
	System.arraycopy(src, 0, nodes, 0, ssize);
	Sorting.quickSort(nodes, 0, ssize, idc);
	System.arraycopy(nodes, 0, dest, 0, msize);
	return msize;
}


public int merge(Node[] src1, int size1, Node[] src2, int size2, Node[] dest)
{
	int i1 = 0; // Index of src1
	int i2 = 0; // Index of src2
	int size = 0;
	while (i1 < size1 && i2 < size2) {
		long id1 = ((IDHolder) src1[i1].getProtocol(pid)).getID();
		long id2 = ((IDHolder) src2[i2].getProtocol(pid)).getID();
		if (id1 <= id2) {
			if (size == 0 || dest[size - 1] != src1[i1])
				dest[size++] = src1[i1];
			i1++;
		} else if (id2 < id1) {
			if (size == 0 || dest[size - 1] != src2[i2])
				dest[size++] = src2[i2];
			i2++;
		} 
	}
	while (i1 == size1 && i2 < size2) {
		if (size == 0 || dest[size - 1] != src2[i2])
			dest[size++] = src2[i2];
		i2++;
	}
	while (i2 == size2 && i1 < size1) {
		if (size == 0 || dest[size - 1] != src1[i1])
			dest[size++] = src1[i1];
		i1++;
	}
	return size;
}



}
