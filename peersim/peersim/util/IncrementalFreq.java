/*
 * Copyright (c) 2003 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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
// using hashtables.
/**
* A class that can collect frequency information on integer input.
* Even though it is easy to get such statistics with other tools and scripts,
* this class is useful for reducing the ize of output when this information
* is needed frequently.
* right now it can handle only unsigned input. It simply ignores negative
* numbers.
*/
public class IncrementalFreq implements Cloneable {


// ===================== fields ========================================
// =====================================================================


protected int n;

/** freq[i] holds the frequency of i. primitive implementation, to be changed */
protected int[] freq = null; 

/**
* The capacity, if larger than 0. Added values larger than or equal to
* this one will be ignored.
*/
protected final int N;


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

/** Returns number of processed data items.*/
public int getN() { return n; }

// --------------------------------------------------------------------

/** Returns frequency of given integer. */
public int getFreq(int i) {
	
	if( i>=0 && i<freq.length ) return freq[i];
	else return 0;
}

// --------------------------------------------------------------------
	

/**
 * Performs an element-by-element vector substraction of the
 * frequency vectors. If <code>strict</code> is true, it
 * throws an IllegalArgumentException if <code>this</code> is
 * not strictly larger than <code>other</code> (element by element)
 * (Note that both frequency vectors are positive.)
 * Otherwise just sets those elements in <code>this</code> to zero
 * that are smaller than those of <code>other</code>.
 * @param other The instance of IncrementalFreq to subtract
 * @param strict See above explanation
 */
public void remove(IncrementalFreq other, boolean strict) {

	// check is other has non-zero elements in non-overlapping part
	if(strict && other.freq.length>freq.length)
	{
		for(int i=other.freq.length-1; i>=freq.length; --i)
		{
			if (other.freq[i]!=0)
				throw new IllegalArgumentException();
		}
	}
	
	int minLength = Math.min(other.freq.length, freq.length);
	for (int i=minLength-1; i>=0; i--)
	{
		if (strict && freq[i] < other.freq[i])
			throw new IllegalArgumentException();
		int remove = Math.min(other.freq[i], freq[i]);
		n -= remove;
		freq[i] -= remove;
	}
}

// ---------------------------------------------------------------------

/**
* Prints current frequency information. Prints a separate line for
* all values from 0 to the capacity of the internal representation using the
* format
* <pre>
* value frequency
* </pre>
*/
public void printAll( PrintStream out ) {
	
	for(int i=0; i<freq.length; ++i)
	{
		out.println(i+" "+freq[i]);
	}
}

// ---------------------------------------------------------------------

/**
* Prints current frequency information. Prints a separate line for
* all values that have a frequency different from zero using the 
* format
* <pre>
* value frequency
* </pre>
*/
public void print( PrintStream out ) {

	for(int i=0; i<freq.length; ++i)
	{
		if(freq[i]!=0) out.println(i+" "+freq[i]);
	}
}

// ---------------------------------------------------------------------

public void print( String prefix ) 
{
	
	for(int i=0; i<freq.length; ++i)
	{
		if (freq[i] != 0)
			Log.println(prefix, i+" "+freq[i]);
	}
}

// ---------------------------------------------------------------------

public String toString() {
	
	String result="";
	for(int i=0; i<freq.length; ++i)
	{
		if (freq[i] != 0)
			result = result+" ("+i+","+freq[i]+")";
	}
	return result;
}

// ---------------------------------------------------------------------

/** An alternative method to convert the object to String */
public String toArithmeticExpression() {

	String result="";
	for (int i=freq.length-1; i>=0; i--)
	{
		if (freq[i] != 0)
			result = result+freq[i]+"*"+i+"+";
	}
	
	if (result.equals(""))
		result = "(empty)";
	else
		result = result.substring(0, result.length()-1);
	return result;
}

// ---------------------------------------------------------------------

public Object clone() throws CloneNotSupportedException {

	IncrementalFreq result = (IncrementalFreq)super.clone();
	if( freq != null ) result.freq = (int[])freq.clone();
	return result;
}

// ---------------------------------------------------------------------

/**
* Tests equality between two IncrementalFreq instances.
*/
public boolean equals(Object obj) {

	IncrementalFreq other = (IncrementalFreq)obj;

	if (this.freq.length != other.freq.length)
		return false;

	for (int i=freq.length-1; i>=0; i--)
		if (this.freq[i] != other.freq[i])
			return false;

	return true;
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


