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

/**
* A class that can keep track of some statistics like variance, average, min,
* max incremetally. That is, when adding a new data item, it updates the
* statistics.
*/
public class IncrementalStats {


// ===================== fields ========================================
// =====================================================================


private double min;

private double max;

private double sum;

private double sqrsum;

private int n;

private int countmin;

private int countmax;

// ====================== initialization ==============================
// ====================================================================


public IncrementalStats() { reset(); }

// --------------------------------------------------------------------

public void reset() {
	
	countmin=0;
	countmax=0;
	min = Double.POSITIVE_INFINITY;
	max = Double.NEGATIVE_INFINITY;
	sum = 0.0;
	sqrsum = 0.0;
	n = 0;
}


// ======================== methods ===================================
// ====================================================================


public void add( double item ) {
	
	if( item < min )
	{
		min = item;
		countmin = 0;
	} 
	if( item == min ) countmin++;
	if( item > max )
	{
		max = item;
		countmax = 0;
	}
	if(item == max) countmax++;  
	n++;
	sum += item;
	sqrsum += item*item;
}

// --------------------------------------------------------------------

/** The number of data items seen so far */
public int getN() { return n; }

// --------------------------------------------------------------------

/** The maximum of the data items */
public double getMax() { return max; }

// --------------------------------------------------------------------

/** The minimum of the data items */
public double getMin() { return min; }

// --------------------------------------------------------------------

/** Returns the number of data items whose value equals the maximum. */
public int getMaxCount() { return countmax; }

// --------------------------------------------------------------------

/** Returns the number of data items whose value equals the minimum. */
public int getMinCount() { return countmin; }

// --------------------------------------------------------------------

/** The sum of the data items */
public double getSum() { return sum; }

// --------------------------------------------------------------------

/** The sum of the squares of the data items */
public double getSqrSum() { return sqrsum; }

// --------------------------------------------------------------------

/** The average of the data items */
public double getAverage() { return sum/n; }

// --------------------------------------------------------------------

/** the empirical variance of the data items */
public double getVar() {

	return (((double)n) / (n-1)) * (sqrsum/n - getAverage()*getAverage());
}

// --------------------------------------------------------------------

/** the empirical standard deviation of the data items */
public double getStD() { return Math.sqrt(getVar()); }

// --------------------------------------------------------------------

public String toString() {

	return min+" "+max+" "+n+" "+sum/n+" "+getVar();
}

}

