package peersim.core;

public interface Protocol extends Cloneable {
	
	/**
	* We have to include this to change the access right to public.
	*/
	public Object clone() throws CloneNotSupportedException;
}

