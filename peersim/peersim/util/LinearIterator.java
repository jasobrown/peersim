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
		
package peersim.util;

import java.util.NoSuchElementException;

/**
* This class gives the linear order 0,1,etc or alternatively len-1, len-2, etc.
*/
public class LinearIterator implements IndexIterator {


// ======================= private fields ============================
// ===================================================================

/** if true then the order is len-1,len-2,..., otherwise 0,1,... */
private final boolean reverse;

private int len = 0;

private int pointer = 0;


// ======================= initialization ============================
// ===================================================================


public LinearIterator() { reverse=false; }

// -------------------------------------------------------------------

public LinearIterator( boolean rev ) { reverse=rev; }


// ======================= public methods ============================
// ===================================================================

public void reset(int k) {
	
	len = k;
	pointer = (reverse ? len-1 : 0);
}

// -------------------------------------------------------------------

public int next() {
	
	if( !hasNext() ) throw new NoSuchElementException();
	
	return (reverse ? pointer-- : pointer++);
}

// -------------------------------------------------------------------

/**
* Returns true if {@link #next} can be called at least one more time.
*/
public boolean hasNext() { return (reverse ? pointer >= 0 : pointer < len); }

// -------------------------------------------------------------------

/** to test the class */
public static void main( String pars[] ) throws Exception {
	
	LinearIterator rp = new LinearIterator(pars[0].equals("rev"));
	
	int k = Integer.parseInt(pars[1]);
	rp.reset(k);
	while(rp.hasNext()) System.out.println(rp.next());
	
	System.out.println();

	k = Integer.parseInt(pars[2]);
	rp.reset(k);
	while(rp.hasNext()) System.out.println(rp.next());
	System.out.println(rp.next());
}

}
