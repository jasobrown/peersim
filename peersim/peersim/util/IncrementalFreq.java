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
		
package peersim.util;

import java.io.PrintStream;

//XXX This implementation is very restricted, to be made more flexible
// using hastables.
/**
* A class that can collect frequency information on integer input.
* Even though it is easy to get such statistics with other tools and scripts,
* this class is useful for reducing the ize of output when this information
* is needed frequently.
* right now it can handle only unsigned input. It simply ignores negative
* numbers.
*/
public class IncrementalFreq {


// ===================== fields ========================================
// =====================================================================


private int n;

/** freq[i] holds the frequency of i. primitive implementation, to be changed */
private int[] freq = null; 

/**
* The capacity, if larger than 0. Added values larger than or equal to
* this one will be ignored.
*/
private final int N;


// ====================== initialization ==============================
// ====================================================================


/**
* Values larger than this one will be ignored.
*/
public IncrementalFreq(int maxvalue) {
	
	N = maxvalue+1;
	reset();
}

// --------------------------------------------------------------------

public IncrementalFreq() {
	
	N=0;
	reset();
}

// --------------------------------------------------------------------

public void reset() {

	if( freq==null || N==0 ) freq = new int[0];
	else for(int i=0; i<freq.length; ++i) freq[i]=0;
	n = 0;
}


// ======================== methods ===================================
// ====================================================================


public void add( int i ) {
	
	if( N>0 && i>=N ) return;
	n++;
	if( i>=0 && i<freq.length ) freq[i]++;
	else if( i>=0 )
	{
		int tmp[] = new int[i+1];
		System.arraycopy(freq,0,tmp,0,freq.length);
		tmp[i]++;
		freq=tmp;
	}
}

// --------------------------------------------------------------------

/** Returns number of processed data items.  */
public int getN(int i) { return n; }

// --------------------------------------------------------------------

/** Returns frequency of given integer. */
public int getFreq(int i) {
	
	if( i>=0 && i<freq.length ) return freq[i];
	else return 0;
}
	
// ---------------------------------------------------------------------

public void print( PrintStream out ) {
	
	for(int i=0; i<freq.length; ++i)
	{
		out.println(i+" "+freq[i]);
	}
}

// ---------------------------------------------------------------------

public String toString() {
	
	String result="";
	for(int i=0; i<freq.length; ++i)
	{
		result = result+freq[i]+" ";
	}
	return result;
}

// ---------------------------------------------------------------------

/** to test. Input is a list of numbers in command line. */
public static void main(String[] pars) {
	
	IncrementalFreq ifq = new IncrementalFreq(Integer.parseInt(pars[0]));
	for(int i=1; i<pars.length; ++i)
	{
		ifq.add(Integer.parseInt(pars[i]));
	}
	ifq.print(System.out);
	System.out.println(ifq);
}

}


