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


// ====================== initialization ==============================
// ====================================================================


public IncrementalStats() { reset(); }

// --------------------------------------------------------------------

public void reset() {

	min = Double.POSITIVE_INFINITY;
	max = Double.NEGATIVE_INFINITY;
	sum = 0.0;
	sqrsum = 0.0;
	n = 0;
}


// ======================== methods ===================================
// ====================================================================


public void add( double item ) {
	
	if( item < min ) min = item;
	if( item > max ) max = item;
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

