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

package peersim.extras.am.epidemic.sorted;


import java.util.*;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.epidemic.xtman.*;
import peersim.extras.am.id.*;
import cern.colt.*;


public class SortedRingDistance implements Distance
{

private final Comparator idc;

/** 
 * This parameter is used to specify the protocol
 * that must be used to obtain IDs.
 */
private static final String PAR_PROTOCOL = "protocol";

/** The protocol identifier */
private final int pid;

public SortedRingDistance(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	idc = new IDNodeComparator(pid);
}

public int rank(Node[] src, int ssize, Node node, Node[] dest, int dsize)
{
	long id = ((IDHolder) node.getProtocol(pid)).getID(); 
	
	int l,r;
	int pos = Sorting.binarySearchFromTo(src, node, 0, ssize - 1, idc);
	if (pos < 0) {
		//System.out.println(pos);
		l = (-pos-2+ssize)%ssize;
		r = (-pos-1)%ssize;
	} else {
		l = (pos-1+ssize)%ssize;
		r = (pos+1)%ssize;
	}
	
	int s=0;
	for (int i=0; i < dsize/2; i++) {
		if (CommonState.r.nextBoolean()) {
			dest[s++] = src[(l-i+ssize)%ssize];
			dest[s++] = src[(r+i)%ssize];
		} else {
			dest[s++] = src[(r+i)%ssize];
			dest[s++] = src[(l-i+ssize)%ssize];
		}
	}
	return s;
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
