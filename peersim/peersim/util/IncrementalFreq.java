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
private int[] freq; 

// ====================== initialization ==============================
// ====================================================================


public IncrementalFreq() { reset(); }

// --------------------------------------------------------------------

public void reset() {

	freq = new int[0];
	n = 0;
}


// ======================== methods ===================================
// ====================================================================


public void add( int i ) {
	
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

/** Returns number of data items seen.  */
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

/** to test. Input is a list of numbers in command line. */
public static void main(String[] pars) {
	
	IncrementalFreq ifq = new IncrementalFreq();
	for(int i=0; i<pars.length; ++i)
	{
		ifq.add(Integer.parseInt(pars[i]));
	}
	ifq.print(System.out);
}

}


