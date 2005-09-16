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
import java.util.Random;

/**
* This class provides a weighted random permutation of indexes.
* Useful for weighted random sampling without replacement.
* The next sample is taken according to the weights given as a parameter
* to {@link #reset(int,double[])}. The weights work as follows.
* The first sample is drawn according to the probability distribution
* defined by the (normalized) weights.
* After this the remaining k-1 elements and the associated k-1
* (re-normalized) weights
* define a new probability distribution, according to which the 2nd element
* is drawn, and so on.
*/
public class WeightedRandPerm implements IndexIterator {


// ======================= private fields ============================
// ===================================================================


private int[] buffer = null;

private double[] weights = null;

private int len = 0;

private int pointer = 0;

private double sum = 0.0;

private final Random r;


// ======================= initialization ============================
// ===================================================================


/** Set the source of randomness to use. You need to call
* {@link #reset(int,double[])} to fully initialize the object. */
public WeightedRandPerm( Random r ) { this.r=r; }


// ======================= public methods ============================
// ===================================================================


/**
* It initiates a random permutation of the integeres from 0 to k-1.
* It does not actually calculate the permutation.
* The permutation can be read using method {@link #next}.
* If the previous permutation was of the same length, it is more efficient.
* @param k the set is defined as 0,...,k-1
* @param w the weights. The length of the array must be at least k, and
* the vector elements must be positive. It cannot be null when calling
* this method for the first time. However, it can be null later, but only
* if k is also the same as it was for the previous call. In this case, the
* same weights are used as in the previous call.
*/
public void reset(int k, double[] w ) {
	
	pointer = k;
	
	// make sure weights and sum are initialized
	if( w != null )
	{
		if( w.length < k ) throw new
			IllegalArgumentException("array w too short");
		
		if( weights == null || weights.length < k ) 
			weights = new double[k];
		
		sum = 0.0;
		for(int i=0; i<k; ++i)
		{
			if( w[i] <= 0.0 ) throw new IllegalArgumentException(
				"weights should be positive: "+w[i]);
			sum+=(weights[i]=w[i]);
		}
	}
	else
	{
		if( len != k || weights == null || weights.length < k )
			throw new
			IllegalArgumentException("array w should not be null");
		
		sum = 0.0;
		for(int i=0; i<k; ++i) sum+=weights[i];
		
	}
	
	// make sure buffer and len are initialized
	if( buffer == null || buffer.length < k ) buffer = new int[k];
	
	if( len != k || w != null )
	{
		len = k;
		for( int i=0; i<len; ++i ) buffer[i]=i;
	}	
}

// -------------------------------------------------------------------

/** Calls <code>reset(k,null)</code>.
* @see #reset(int,double[]) */ 
public void reset( int k ) { reset(k,null); }

// -------------------------------------------------------------------

/**
* The first sample is drawn according to the probability distribution
* defined by the (normalized) weights.
* After this the remaining k-1 elements and the associated k-1
* (re-normalized) weights
* define a new probability distribution, according to which the 2nd element
* is drawn, and so on.
* @see #reset(int,double[])
*/
public int next() {
	
	if( pointer < 1 ) throw new NoSuchElementException();
	
	double d = sum*r.nextDouble();
	int i = pointer;
	double tmp = weights[i-1];
	while( tmp < d && i>1 ) tmp += weights[--i-1];
	
	// now i-1 is the selected element, we shift it to next position
	int a = buffer[i-1];
	double b = weights[i-1];
	buffer[i-1] = buffer[pointer-1];
	weights[i-1] = weights[pointer-1];
	buffer[pointer-1] = a;
	weights[pointer-1] = b;
	sum -= b;
	
	return buffer[--pointer];
}

// -------------------------------------------------------------------

public boolean hasNext() { return pointer > 0; }

// -------------------------------------------------------------------

/*
public static void main( String pars[] ) throws Exception {
	
	WeightedRandPerm rp = new WeightedRandPerm(new Random());

	int k = Integer.parseInt(pars[0]);
	double w[] = new double[k];
	for(int i=0; i<k; ++i) w[i] = Double.parseDouble(pars[i+1]);
	
	rp.reset(k,w);
	for(int i=0; i<1000; ++i)
	{
		if(i%4==0) rp.reset(k,w);
		if(i%4==1) rp.reset(k);
		if(i%4==2) rp.reset(k-1,w);
		if(i%4==3) rp.reset(k-1);
		while(rp.hasNext()) System.out.print(rp.next()+" ");
		System.out.println();
	}
	
	System.out.println();
}
*/
}

