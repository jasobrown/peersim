/*
 * hotNodeProtocol.java
 *
 * Created on 29 marzo 2004, 13.06
 */

package example.hot;

import peersim.core.IdleProtocol;

/** This class does nothing! It is simply a container inside each node to 
 * collect some useful data such as coordinates, hop count and degree count.
 *
 * @author  giampa
 */
public class InetNodeProtocol extends IdleProtocol {
    
    /**
     * String name of the parameter used to select the linkable protocol 
    * used to obtain information about neighbors.
    */
    public static final String PAR_CONN = "linkableID";

    
    // coordinates in space:
    public double x;
    public double y;
    
    public int in_degree;
    public int hops;
    public boolean isroot;
   
    
    /** Creates a new instance of hotNodeProtocol */
    public InetNodeProtocol(String prefix, Object obj) {
        super(prefix);
        in_degree = 0;
        hops = 0;
        isroot = false;
    }
    
    public Object clone() throws CloneNotSupportedException {
	InetNodeProtocol af = (InetNodeProtocol) super.clone();
	
	return af;
    }
    
    public void nextCycle(peersim.core.Node node, int protocolID) {
    }
        
}
