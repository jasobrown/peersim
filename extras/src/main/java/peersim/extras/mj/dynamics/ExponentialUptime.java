/*
 * Copyright (c) 2008 Mark Jelasity
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package peersim.extras.mj.dynamics;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.*;
import peersim.dynamics.NodeInitializer;

import cern.jet.random.Binomial;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * This {@link Control} can change the size of networks by adding and removing
 * nodes. Can be used to model churn. This class supports only permanent removal
 * of nodes and the addition of brand new nodes. That is, temporary downtime
 * is not supported by this class.
 * <p>
 * The main parameter is a probability. Every single node is removed with this
 * probability during the execution of the control. It is possible to add substitutes
 * for the nodes, thereby keeping the network size constant.
 */
public class ExponentialUptime implements Control
{

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * Config parameter which gives the prefix of node initializers.
 * These initialzers are used to initialize nodes that are added, when {@value #PAR_SUBST}
 * is set. An arbitrary
 * number of node initializers can be specified (Along with their parameters).
 * These will be applied
 * on the newly created nodes. The initializers are ordered according to
 * alphabetical order if their ID.
 * Example:
 * <pre>
control.0 DynamicNetwork
control.0.init.0 RandNI
control.0.init.0.k 5
control.0.init.0.protocol somelinkable
...
 * </pre>
 * @config
 */
private static final String PAR_INIT = "init";

/**
 * If defined, removed nodes are substituted.
 * That is, first the number of nodes to remove is calculated, and then exactly the same
 * number of nodes are added immediately so that the network size remains constant.
 * Not set by default.
 * @config
 */
private static final String PAR_SUBST = "substitute";

/**
 * Specifies the probability to remove each node. 
 * Every node will be removed by this probability. 
 * @config
 */
private static final String PAR_P = "p";

/**
 * Nodes are removed until the size specified by this parameter is reached. The
 * network will never go below this size as a result of this class.
 * Defaults to 0.
 * @config
 */
private static final String PAR_MIN = "minsize";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** value of {@value #PAR_P} */
protected final double p;

/** value of {@value #PAR_SUBST} */
protected final boolean substitute;

/** value of {@value #PAR_MIN} */
protected final int minsize;

/** node initializers to apply on the newly added nodes */
protected final NodeInitializer[] inits;

/** The binomial distribution sampler to calculate number of nodes to remove. */
protected final Binomial binom;

/** The uniform distribution sampler to select which nodes to remove exactly. */
protected final Uniform uni;

/** The size of the network at initialization time. */
protected final int initNetsize;

/** This control's own random engine initialized using the current random seed */
protected final MersenneTwister mtw =
	new MersenneTwister((int)CommonState.r.getLastSeed());

// --------------------------------------------------------------------------
// Protected methods
// --------------------------------------------------------------------------

/**
 * Adds n nodes to the network. Extending classes can implement any algorithm to
 * do that. The default algorithm adds the given number of nodes after calling
 * all the configured initializers on them.
 * 
 * @param n
 *          the number of nodes to add, must be non-negative.
 */
protected void add(int n)
{
	for (int i = 0; i < n; ++i) {
		Node newnode = (Node) Network.prototype.clone();
		for (int j = 0; j < inits.length; ++j) {
			inits[j].initialize(newnode);
		}
		Network.add(newnode);
	}
}

// ------------------------------------------------------------------

/**
 * Removes n nodes from the network. Extending classes can implement any
 * algorithm to do that. The default algorithm removes <em>random</em>
 * nodes <em>permanently</em> simply by calling {@link Network#remove(int)}.
 * @param n the number of nodes to remove
 */
protected void remove(int n)
{
	for (int i = 0; i < n; ++i) {
		Network.remove(uni.nextIntFromTo(0,Network.size()-1));
	}
}


// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public ExponentialUptime(String prefix)
{
	p = Configuration.getDouble(prefix + "." + PAR_P);
	if( p<0 || p>1 ) throw new IllegalParameterException(prefix+"."+PAR_P,
		"Should be a probability (from [0,1])");
	if( p !=1.0 && p!=0.0 )
	{
		binom = new Binomial( Network.size(), p, mtw );
		initNetsize = Network.size();
	}
	else // we don't use the binomial distribution
	{
		binom = null;
		initNetsize = -1;
	}
	substitute = Configuration.contains(prefix + "." + PAR_SUBST);
	Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
	inits = new NodeInitializer[tmp.length];
	for (int i = 0; i < tmp.length; ++i) {
		//System.out.println("Inits " + tmp[i]);
		inits[i] = (NodeInitializer) tmp[i];
	}
	minsize = Configuration.getInt(prefix + "." + PAR_MIN, 0);
	uni = new Uniform( mtw );
}

// --------------------------------------------------------------------------
// Public methods
// --------------------------------------------------------------------------

/**
 * Calls {@link #add(int)} and/or {@link #remove} based on the parameters defined by the
 * configuration.
 * @return always false 
 */
public final boolean execute()
{
	int toadd = 0;
	int toremove = 0;

	if( p==1.0 ) toremove = Network.size();
	else if( p>0.0 )
	{
		if( Network.size() == initNetsize ) toremove = binom.nextInt();
		else toremove = binom.nextInt( Network.size(), p );
	}
	
	if( !substitute && toremove > Network.size() - minsize )
		toremove = Network.size() - minsize;
	if( substitute ) toadd = toremove;
	
	remove(toremove);
	add(toadd);
	
	return false;
}

// --------------------------------------------------------------------------

}
