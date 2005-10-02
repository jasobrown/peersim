/*
 * hotNodeProtocol.java
 *
 * Created on 29 marzo 2004, 13.06
 */

package example.hot;

import peersim.core.IdleProtocol;

/**
 * This class does nothing. It is simply a container inside each node to
 * collect some useful data such as coordinates, hop count and degree
 * count.
 * 
 * @author giampa
 */
public class InetNodeProtocol extends IdleProtocol
{

// coordinates in space:
public double x;

public double y;

public int in_degree;

public int hops; // hop distance from ROOT node

public boolean isroot;

/** Creates a new instance of hotNodeProtocol */
public InetNodeProtocol(String prefix)
{
	super(prefix);
	in_degree = 0;
	hops = 0;
	isroot = false;
}

public Object clone()
{
	InetNodeProtocol af = (InetNodeProtocol) super.clone();
	af.isroot = this.isroot;
	af.x = this.x;
	af.y = this.y;
	af.in_degree = this.in_degree;
	af.hops = this.hops;
	return af;
}

public void nextCycle(peersim.core.Node node, int protocolID)
{
}

}
