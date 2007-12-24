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


import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * This comparator class compares two node objects based on the value maintained
 * by one of its protocols. The protocol must implemente the IDHolder interface;
 * its identifier has to be specified when a new comparator is built.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class TreeIDComparator implements Comparator
{

/** Protocol to be be compared */
protected int pid = -1;

/** Number of bits in the identifier */
protected int bits;

/** Id currently used for comparison */
protected long id;

/**
 * Builds a new Comparator that compares the ids maintained by protocols
 * identified by <code>pid</pid>.
 */
public TreeIDComparator(int pid, int bits)
{
	this.pid = pid;
	this.bits = bits;
}

public void setID(long id)
{
	this.id = id;
}


// Comment inherited from interface
public int compare(Object o1, Object o2)
{
	long id1 = IDUtil.getID((Node) o1, pid);
	long id2 = IDUtil.getID((Node) o2, pid);
	long dist1 = dist(id1, id);
	long dist2 = dist(id2, id);
//	System.out.println(Long.toBinaryString(id));
//	System.out.println(Long.toBinaryString(id1));
//	System.out.println(Long.toBinaryString(id2));
//	System.out.println(dist1 + " " + dist2);
//	System.out.println("---");
	
	if (dist1 < dist2)
		return -1;
	else if (dist1 == dist2)
		return 0;
	else
		return +1;
}

public long dist( long a, long b ) {
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