package peersim.util;

import java.util.NoSuchElementException;

/**
* This class provides a random permutation of indexes. Useful for random
* walks and random sampling without replacement.
*/
public class RandPermutation implements IndexIterator {


// ======================= private fields ============================
// ===================================================================


private int[] buffer = null;

private int len = 0;

private int pointer = 0;


// ======================= public methods ============================
// ===================================================================


/**
* It calculates a random permutation of the integeres from 0 to k-1.
* The permutation can be read using method {@link #get}. 
* If the previous permutation was of the same length, it is more efficient.
*/
public void setPermutation(int k) {
	
	initPermutation(k);
	
	for(int i=len; i>1; i--)
	{
		int j = CommonRandom.r.nextInt(i);
		int a = buffer[j];
		buffer[j] = buffer[i-1];
		buffer[i-1] = a;
	}
}

// -------------------------------------------------------------------

/**
* Returns the ith element of the permutation set by {@link #setPermutation}.
*/
public int get(int i) {
	
	if( i >= len ) throw new IndexOutOfBoundsException();
	return buffer[i];
}

// -------------------------------------------------------------------

/**
* It initiates a random permutation of the integeres from 0 to k-1.
* It does not actually calculate the permutation.
* The permutation can be read using method {@link #next}.
* Calls to {@link #get} return undefined values, so {@link #next} must be used.
* If the previous permutation was of the same length, it is more efficient.
*/
public void initPermutation(int k) {
	
	pointer = k;
	if( len == k ) return;
	
	if( buffer == null || buffer.length < k )
	{
		buffer = new int[k];
	}
	
	len = k;
	for( int i=0; i<len; ++i ) buffer[i]=i;
}

// -------------------------------------------------------------------

public int next() {
	
	if( pointer < 1 ) throw new NoSuchElementException();
	
	int j = CommonRandom.r.nextInt(pointer);
	int a = buffer[j];
	buffer[j] = buffer[pointer-1];
	buffer[pointer-1] = a;
	
	return buffer[--pointer];
}

// -------------------------------------------------------------------

/**
* Returns true if {@link #next} can be called at least one more time.
*/
public boolean hasNext() { return pointer > 0; }

// -------------------------------------------------------------------

/** to test the class */
public static void main( String pars[] ) throws Exception {
	
	RandPermutation rp = new RandPermutation();

	int k;
	
	k = Integer.parseInt(pars[0]);
	rp.setPermutation(k);
	for(int i=0; i<k; ++i) System.out.println(rp.get(i));

	System.out.println();

	k = Integer.parseInt(pars[1]);
	rp.initPermutation(k);
	for(int i=0; i<k; ++i) System.out.println(rp.next());
	
	System.out.println();

	k = Integer.parseInt(pars[2]);
	rp.initPermutation(k);
	for(int i=0; i<k+1; ++i) System.out.println(rp.next());
}

}
