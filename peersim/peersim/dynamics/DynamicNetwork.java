package peersim.dynamics;

import peersim.init.NodeInitializer;
import peersim.config.Configuration;
import peersim.util.CommonRandom;
import peersim.core.*;

/**
* A network dynamics manager which can grow networks.
*/
public class DynamicNetwork implements Dynamics {


////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////


/** 
* Config parameter which gives prefix of node initializers.
*/
public static final String PAR_INIT = "init";

/** 
 * Config parameter if the nodes have to be substituted, and not
 * just added or removed. 
 */
public static final String PAR_SUBST = "substitute";

/** 
* The number of nodes to add when nodes are scheduled for addition. This
* can be negative which result in deletion of that many nodes.
* It can also be non-integer if {@link #PAR_PERC} is set.
*/
public static final String PAR_ADD = "add";

/** 
* The network is grown until reaching this number of nodes. The network
* will never exceed this size as a result of this network manager.
* Defaults to {@link OverlayNetwork#getCapacity()}.
*/
public static final String PAR_MAX = "maxsize";

/** 
* The network is cut until reaching this number of nodes. The network
* will never go below this size as a result of this network manager.
* Defaults to 0.
*/
public static final String PAR_MIN = "minsize";


////////////////////////////////////////////////////////////////////////////
// Fields
////////////////////////////////////////////////////////////////////////////

protected double add;

protected boolean substitute;

protected int minsize;

protected int maxsize;

protected NodeInitializer[] inits;

protected String prefix;

// ====================== protected methods ============================
// ===================================================================


/**
* Adds n nodes to the network. Extending classes can implement
* any algorithm to do that. The default algorithm adds the given
* number of nodes after calling all the configured initializers on them.
* @param n the number of nodes to add, must be non-negative.
*/
protected void add(int n) {
	
	for(int i=0; i<n; ++i)
	{
		Node newnode = null;

		try
		{
			newnode = (Node)Network.prototype.clone();
		}
		catch(CloneNotSupportedException e)
		{
			// this is fatal but should never happen because nodes
			// are cloneable
			throw new Error(e+"");
		}

		for(int j=0; j<inits.length; ++j)
		{
			inits[j].initialize( newnode );
		}

		Network.add(newnode);
	}
}

// ------------------------------------------------------------------

/**
* Removes n nodes from the network. Extending classes can implement
* any algorithm to do that. The default algorithm removes random nodes
* simply by calling {@link OverlayNetwork#remove}. This is equialent
* to permanent failure without any cleanup.
* @param n the number of nodes to remove
*/
protected void remove( int n ) {

	for(int i=0; i<n; ++i)
	{
		Network.swap(
			Network.size()-1,
			CommonRandom.r.nextInt(Network.size()) );
		Network.remove();
	}
}


// ==================== initialization ==============================
// ==================================================================


public DynamicNetwork(String prefix) 
{
	this.prefix = prefix;
}


// ===================== public methods ==============================
// ===================================================================


/**
* Calls {@link #add} or {@link #remove} with the parameters defined by
* the configuration.
*/
public final void modify() 
{
	add = Configuration.getDouble(prefix+"."+PAR_ADD);
	substitute = Configuration.contains(prefix+"."+PAR_SUBST);
	
	Object[] tmp = Configuration.getInstanceArray(prefix+"."+PAR_INIT);
	inits = new NodeInitializer[tmp.length];
	for(int i=0; i<tmp.length; ++i)
	{
		inits[i] = (NodeInitializer)tmp[i];
	}
	maxsize = Configuration.getInt(prefix+"."+PAR_MAX,
			Network.getCapacity());
	minsize = Configuration.getInt(prefix+"."+PAR_MIN,0);


  int tochange;
  if (add==0)
    return;
  if (!substitute) {
  	if ( (maxsize <= Network.size() && add>0) || 
				 (minsize >= Network.size() && add<0) )
		return;
  }
	int toadd = 0;
	int toremove = 0;
	
	if (add > 0) {
		toadd = (int) (add < 1 ? add*Network.size() : add);
		if (!substitute && toadd > maxsize-Network.size())
			toadd = maxsize-Network.size();
		if (substitute)
			toremove = toadd;
	} else if (add < 0) {
		toremove = (int) (add > -1 ? -add*Network.size() : -add);
		if (!substitute && toremove > Network.size()-minsize)
			toremove = Network.size()-minsize;
		if (substitute)
			toadd = toremove;
	}
	remove(toremove);
	add(toadd);
}


}


