package newscast;

import peersim.core.*;

/**
* Provides as graph interface a different neighbor set (the protocol
* will work as normal, only the graph interface changes, so the analisers
* are affected only).
*/
public class Test extends SimpleNewscast {

public static final int CUTOFF=10;

// ====================== initialization ===============================
// =====================================================================


public Test(String n) { super(n); }

// ---------------------------------------------------------------------

public Object clone() throws CloneNotSupportedException {

	return super.clone();
}


// ====================== Linkable implementation =====================
// ====================================================================
 

/**
* Does not check if the index is out of bound
* (larger than {@link #degree()})
*/
public Node getNeighbor(int i) {
	
//	return cache[i+CUTOFF];
	return cache[i];
}

// --------------------------------------------------------------------

/** Cuts off first 10 elements. */
public int degree() {
	
//	int len = cache.length-1;
//	while(len>=CUTOFF && cache[len]==null) len--;
//	return len+1-CUTOFF;
	return CUTOFF;
}

// --------------------------------------------------------------------

public boolean contains(Node n)
{
//	for (int i=CUTOFF; i < cache.length; i++)
	for (int i=0; i < CUTOFF; i++)
	{
		if (cache[i] == n) return true;
	}
	return false;
}

}

