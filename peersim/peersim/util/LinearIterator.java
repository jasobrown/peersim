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
