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
		
package newscast;

import peersim.core.*;

/**
* Provides as graph interface a different neighbor set (the protocol
* will work as normal, only the graph interface changes, so the analisers
* are affected only).
*/
public class Test extends SimpleNewscast {

public static final int CUTOFF=10;

// ====================== initialization ===============================
// =====================================================================


public Test(String n,Object obj) { super(n,obj); }

// ---------------------------------------------------------------------

public Object clone() throws CloneNotSupportedException {

	return super.clone();
}


// ====================== Linkable implementation =====================
// ====================================================================
 

/**
* Does not check if the index is out of bound
* (larger than {@link #degree()})
*/
public Node getNeighbor(int i) {
	
//	return cache[i+CUTOFF];
	return cache[i];
}

// --------------------------------------------------------------------

/** Cuts off first 10 elements. */
public int degree() {
	
//	int len = cache.length-1;
//	while(len>=CUTOFF && cache[len]==null) len--;
//	return len+1-CUTOFF;
	return CUTOFF;
}

// --------------------------------------------------------------------

public boolean contains(Node n)
{
//	for (int i=CUTOFF; i < cache.length; i++)
	for (int i=0; i < CUTOFF; i++)
	{
		if (cache[i] == n) return true;
	}
	return false;
}

}

