package peersim.util;

import java.util.NoSuchElementException;

/**
* This class provides iterations over the set of integers [0...,len-1].
*/
public interface IndexIterator {

	/**
	* This resets the iteration. The set of integers will be 0,..,len-1.
	*/
	public void reset(int len);
	
	/**
	* Returns next index.
	* @throws NoSuchElementException if there is no next element
	*/
	public int next();

	/**
	* Returns true if {@link #next} can be called at least one more time.
	*/
	public boolean hasNext();
}

