/*
 * Copyright (c) 2003-2005 The BISON Project
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
	
package peersim.dynamics;

import peersim.config.Configuration;
import peersim.core.*;

/**
 * A network dynamic manager that can make networks oscillate.
 */
public class OscillatingNetwork implements Control
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * Config parameter which gives prefix of node initializers.
 * @config
 */
private static final String PAR_INIT = "init";

/**
 * Config parameter used to define the number of cycles needed to complete an
 * oscillation.
 * @config
 */
private static final String PAR_PERIOD = "periodicity";

/**
 * Config parameter used to defined the maximum size of the oscillating network.
 * @config
 */
private static final String PAR_MAX = "maxsize";

/**
 * Config parameter used to defined the minimum size of the oscillating network.
 * @config
 */
private static final String PAR_MIN = "minsize";

/**
 * Operation starts from this time point. This operator will be applied first at
 * this time. Defaults to 0.
 * @config
 */
private static final String PAR_FROM = "from";

/**
 * Operation ends at this time point. This operator will be applied last at this
 * time. Defaults to <tt>Integer.MAX_VALUE</tt>.
 * @config
 */
private static final String PAR_UNTIL = "until";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** Periodicity */
private final int periodicity;

/** Maximum size */
private final int minsize;

/** Minimum size */
private final int maxsize;

/** Operation starts from this time point */
private final int from;

/** Operation ends at this time point */
private final int until;

/** New nodes initializers */
private final NodeInitializer[] inits;


//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters. Invoked by the
 * simulation engine.
 * @param prefix
 *          the configuration prefix for this class
 */
public OscillatingNetwork(String prefix)
{

	periodicity = Configuration.getInt(prefix + "." + PAR_PERIOD);
	from = Configuration.getInt(prefix + "." + PAR_FROM, 0);
	until=Configuration.getInt(prefix + "." + PAR_UNTIL, Integer.MAX_VALUE);
	maxsize =
		Configuration.getInt(
			prefix + "." + PAR_MAX,
			Network.getCapacity());
	minsize = Configuration.getInt(prefix + "." + PAR_MIN, 0);

	Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
	inits = new NodeInitializer[tmp.length];
	for (int i = 0; i < tmp.length; ++i)
	{
		inits[i] = (NodeInitializer) tmp[i];
	}
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

protected void add(int toAdd)
{

	Node[] newnodes = new Node[Math.min(maxsize - Network.size(), toAdd)];

	// create empty (not initialized) nodes
	try
	{
		for (int i = 0; i < newnodes.length; ++i)
		{
			newnodes[i] = (Node) Network.prototype.clone();
		}
	} catch (CloneNotSupportedException e)
	{
		// this is fatal but should never happen because nodes
		// are cloneable
		throw new Error(e + "");
	}

	// initialize nodes
	for (int j=0; j < newnodes.length; j++) 
		for (int i = 0; i < inits.length; ++i)
			inits[i].initialize(newnodes[j]);

	// add nodes to overlay network
	for (int i = 0; i < newnodes.length; ++i)
	{
		Network.add(newnodes[i]);
	}
}

// ------------------------------------------------------------------

protected void remove(int toRemove)
{

	for (int i = 0; i < toRemove; ++i)
	{
		Network.swap(
			Network.size() - 1,
			CommonState.r.nextInt(Network.size()));
		Network.remove();
	}
}

// ------------------------------------------------------------------

/**
 * {@inheritDoc}
 */
public boolean execute()
{
	long time = CommonState.getTime();
	int oscillation = (maxsize - minsize) / 2;
	int newsize = (maxsize + minsize) / 2 + 
	  (int) (Math.sin(((double) time) / periodicity * 3.14) * oscillation);
	int diff = newsize - Network.size();
	if (diff < 0)
		remove(-diff);
	else
		add(diff);
	
	return false;
}

}
