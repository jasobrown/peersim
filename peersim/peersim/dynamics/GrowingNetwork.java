package peersim.dynamics;

import peersim.init.NodeInitializer;
import peersim.config.Configuration;
import peersim.util.CommonRandom;
import peersim.core.*;

/**
* A network dynamics manager which can grow networks.
*/
public class GrowingNetwork implements Dynamics {


// ========================= fields =================================
// ==================================================================


/** 
* Config parameter which gives prefix of node initializers.
*/
public static final String PAR_INIT = "init";

/** 
* The number of nodes to add when nodes are scheduled for addition. This
* can be negative which result in deletion of that many nodes.
* It can also be non-integer if {@link #PAR_PERC} is set.
*/
public static final String PAR_ADD = "add";

/** 
* If this parameter is present, PAR_ADD is interpreted as a percentage instead
* of a constant value. Default is constant value (ie that this parameter
* is not present).
*/
public static final String PAR_PERC = "percentage";

/** 
* The network is grown until reaching this number of nodes. The network
* will never exceed this size as a result of this network manager.
* Defaults to {@link Network#getCapacity()}.
*/
public static final String PAR_MAX = "maxsize";

/** 
* The network is cut until reaching this number of nodes. The network
* will never go below this size as a result of this network manager.
* Defaults to 0.
*/
public static final String PAR_MIN = "minsize";

protected final double add;

protected final boolean percentage;

protected final int minsize;

protected final int maxsize;

protected final NodeInitializer[] inits;


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
* simply by calling {@link Network#remove}. This is equialent
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


public GrowingNetwork(String prefix) {

	add = Configuration.getDouble(prefix+"."+PAR_ADD);
	percentage = Configuration.contains(prefix+"."+PAR_PERC);
	maxsize = Configuration.getInt(prefix+"."+PAR_MAX,
			Network.getCapacity());
	minsize = Configuration.getInt(prefix+"."+PAR_MIN,0);
	
	Object[] tmp = Configuration.getInstanceArray(prefix+"."+PAR_INIT);
	inits = new NodeInitializer[tmp.length];
	for(int i=0; i<tmp.length; ++i)
	{
		inits[i] = (NodeInitializer)tmp[i];
	}
}


// ===================== public methods ==============================
// ===================================================================


/**
* Calls {@link #add} or {@link #remove} with the parameters defined by
* the configuration.
*/
public final void modify() {

	if( add == 0 ||
	    (maxsize <= Network.size() && add>0) || 
	    (minsize >= Network.size() && add<0) ) return;
	
	int toadd = (int)add;
	
	if( add < 0 )
	{
		if( percentage )
			toadd = (int)((add*Network.size()-50)/100);
		toadd = Math.max(minsize-Network.size(),toadd);
		remove(-toadd);
	}
	else
	{
		if( percentage )
			toadd = (int)((add*Network.size()+50)/100);
		toadd = Math.min(maxsize-Network.size(),toadd);
		add(toadd);
	}
}


}


