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
* If this is eg 2, new nodes will be added in each 2nd time tick.
* If 1, in all times, etc. Nodes will be added in time 0 in all cases.
* Defaults to 1.
*/
public static final String PAR_INEACH = "ineach";

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

/** 
* Operation starts from this time point.
* This operator will be applied first at this time.
* Defaults to 0.
*/
public static final String PAR_FROM = "from";

/** 
* Operation ends at this time point.
* This operator will be applied last at this time.
* Defaults to <tt>Integer.MAX_VALUE</tt>.
*/
public static final String PAR_UNTIL = "until";

protected final double add;

protected final boolean percentage;

protected final int inEach;

protected final int minsize;

protected final int maxsize;

protected final int from;

protected final int until;

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
			newnode = (Node)OverlayNetwork.prototype.clone();
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

		OverlayNetwork.add(newnode);
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
		OverlayNetwork.swap(
			OverlayNetwork.size()-1,
			CommonRandom.r.nextInt(OverlayNetwork.size()) );
		OverlayNetwork.remove();
	}
}


// ==================== initialization ==============================
// ==================================================================


public GrowingNetwork(String prefix) {

	add = Configuration.getDouble(prefix+"."+PAR_ADD);
	percentage = Configuration.contains(prefix+"."+PAR_PERC);
	inEach = Configuration.getInt(prefix+"."+PAR_INEACH,1);
	from = Configuration.getInt(prefix+"."+PAR_FROM,0);
	until = Configuration.getInt(prefix+"."+PAR_UNTIL,Integer.MAX_VALUE);
	maxsize = Configuration.getInt(prefix+"."+PAR_MAX,
			OverlayNetwork.getCapacity());
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

	if( add == 0 || CommonState.getT() < from || 
	    CommonState.getT() > until ||
	    (maxsize <= OverlayNetwork.size() && add>0) || 
	    (minsize >= OverlayNetwork.size() && add<0) || 
	    CommonState.getT()%inEach!=0 ) return;
	
	int toadd = (int)add;
	
	if( add < 0 )
	{
		if( percentage )
			toadd = (int)((add*OverlayNetwork.size()-50)/100);
		toadd = Math.max(minsize-OverlayNetwork.size(),toadd);
		remove(-toadd);
	}
	else
	{
		if( percentage )
			toadd = (int)((add*OverlayNetwork.size()+50)/100);
		toadd = Math.min(maxsize-OverlayNetwork.size(),toadd);
		add(toadd);
	}
}


}


