package peersim.util;

import java.util.NoSuchElementException;

/**
* This class provides iterations over an interval of integers.
*/
public interface IndexIterator {

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

