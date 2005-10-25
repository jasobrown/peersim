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

package example.loadbalance;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.vector.*;

public class LBObserver implements Control
{

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * If defined, print the load value. The default is false.
 * @config
 */
private static final String PAR_SHOW_VALUES = "show_values";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** The name of this observer in the configuration */
private final String name;

/** Protocol identifier */
private final int pid;

/** Flag to show or not the load values at each node. */
private int show_values = 0;

private IncrementalStats stats = null;

private final int len = Network.size();

// --------------------------------------------------------------------------
// Constructor
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters. Invoked by
 * the simulation engine.
 * @param name
 *          the configuration prefix for this class
 */
public LBObserver(String name)
{
	// Call the parent class (abstract class)
	this.name = name;
	// Other parameters from config file:
	pid = Configuration.getPid(name + "." + PAR_PROT);
	show_values = Configuration.getInt(name + "." + PAR_SHOW_VALUES, 0);
	stats = new IncrementalStats();
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

// Returns always true!
public boolean execute()
{
	// final int len = Network.size();
	double sum = 0.0;
	double max = Double.NEGATIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	int count_zero = 0;
	int count_avg = 0;

	if (show_values == 1) {
		System.out.print(name+": ");
	}

	// target_node_load = targetp.getLocalLoad();
	/* Compute max, min, average */
	for (int i = 0; i < len; i++) {
		SingleValue prot=(SingleValue) Network.get(i).getProtocol(pid);
		double value = prot.getValue();
		stats.add(value);

		if (value == 0) {
			count_zero++;
		}
		if (value == 2) {
			count_avg++;
		}
		// shows the values of load at each node:
		if (show_values == 1) {
			System.out.print(value + ":");
		}
		sum += value;
		if (value > max)
			max = value;
		if (value < min)
			min = value;

		// agavg = protocol.getAVGLoad();
	}
	if (show_values == 1) {
		System.out.println();
	}
	
	System.out.println(name+": "+
			CommonState.getTime() + " " +
			stats.getAverage() + " " + 
			stats.getMax() + " " + 
			stats.getMin() + " " +
			count_zero + " " + // number of zero value node
			count_avg + " " + // number of correct avg nodes
			stats.getVar()
			);
	stats.reset();
	return false;

}

// --------------------------------------------------------------------------

}
