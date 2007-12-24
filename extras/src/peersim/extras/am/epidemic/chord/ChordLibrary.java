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

package peersim.extras.am.epidemic.chord;


import java.util.*;

import peersim.core.*;
import peersim.extras.am.id.*;
import peersim.extras.am.util.*;
import cern.colt.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ChordLibrary
{

/**
 * 
 */
public static void resetFingers(Node[] fingers)
{
	for (int i = 0; i < fingers.length; i++)
		fingers[i] = null;
}

/**
 * 
 * @param array
 * @param size
 * @param fingers
 */
public static void selectFingers(Node[] array, int size, Node[] fingers,
		Node lnode, long id, int pid, int dir, NodeComparator c)
{
	for (int i = 0; i < size; i++) {
		if (array[i] == null)
			continue;
		long newid = IDUtil.getID(array[i], pid);
		if (newid == id)
			continue;
		// Could be a next finger?
		long newdist = dist(id, newid, dir);
		int k = ID.log2(newdist);
		if (fingers[k] == null) {
			fingers[k] = array[i];
		} else {
			if (c == null) {
				long oldid = IDUtil.getID(fingers[k], pid);
				long olddist = dist(id, oldid, dir);
				if (newdist < olddist)
					fingers[k] = array[i];
			} else if (c.compare(lnode, array[i], fingers[k]) < 0) {
				fingers[k] = array[i];
			}
		}
	}
}

/**
 * 
 * @param link
 * @param fingers
 */
public static void selectFingers(Linkable link, Node[] fingers, Node lnode,
		long id, int pid, int dir, NodeComparator c)
{
	int degree = link.degree();
	for (int i = 0; i < degree; i++) {
		Node node = link.getNeighbor(i);
		long newid = IDUtil.getID(node, pid);
		if (newid == id)
			continue;
		long newdist = dist(id, newid, dir);
		int k = ID.log2(newdist);
		if (fingers[k] == null) {
			fingers[k] = node;
		} else {
			if (c == null) {
				long oldid = IDUtil.getID(fingers[k], pid);
				long olddist = dist(id, oldid, dir);
				if (newdist < olddist)
					fingers[k] = node;
			} else if (c.compare(lnode, node, fingers[k]) < 0) {
				fingers[k] = node;
			}
		}
	}
}

public static long dist(long a, long b, int dir)
{
	return ((b - a) * dir + ID.SIZE) % ID.SIZE;
}

/**
 * XXX Documentation to be improved
 * 
 * @param message
 *          the buffer where extracted nodes are inserted
 * @param degree
 *          the number of nodes (on the left and on the right) that must be
 *          extracted
 * @param cache
 *          the cache array from where nodes are extracted
 * @param csize
 *          cache size (can be smaller than cache.length)
 * @param rnode
 *          node with which we are doing an exchange
 * @param idc
 *          the comparator used to search and sort ordered set of nodes.
 */
public static int extract(Node[] message, int degree, Node[] cache, int csize,
		Node rnode, Comparator idc)
{
	if (degree * 2 > csize - 1) {
		degree = (csize - 1) / 2;
	}
	// Search the local node in the list of the other node.
	int shift = 0;
	int max = degree * 2;
	int size = 0;
	int pos = Sorting.binarySearchFromTo(cache, rnode, 0, csize - 1, idc);
	if (pos < 0) {
		pos = -pos + 1;
		if (pos - degree < 0)
			shift = max + pos - degree;
		if (pos + degree - 1 >= csize)
			shift = max + pos + degree - csize;
		for (int i = degree; i > 0; i--) {
			message[(shift + size) % max] = cache[(csize + pos - i) % csize];
			size++;
		}
		for (int i = 0; i < degree; i++) {
			message[(shift + size) % max] = cache[(pos + i) % csize];
			size++;
		}
	} else {
		if (pos - degree < 0)
			shift = max + pos - degree;
		if (pos + degree >= csize)
			shift = max + pos + degree - csize + 1;
		// Extract the nodes from pos; count them in size
		if (message == null)
			System.out.println("Message");
		if (cache == null)
			System.out.println("Cache");
		for (int i = degree; i > 0; i--) {
			message[(shift + size) % max] = cache[(csize + pos - i) % csize];
			size++;
		}
		for (int i = 1; i <= degree; i++) {
			message[(shift + size) % max] = cache[(pos + i) % csize];
			size++;
		}
	}
	return size;
}



}
