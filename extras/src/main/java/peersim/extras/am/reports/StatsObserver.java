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

package peersim.extras.am.reports;

import java.util.*;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;


public class StatsObserver implements Control
{

/** Parameter for defining the counters to be used */
private static final String PAR_STATS = "stats";

/** 
 * Parameter defining additional counters to be
 * used; if at the end of a cycle no value have
 * been added, this control stops the execution.
 */
private static final String PAR_STOPS = "stops";

/** Map containing counters */ 
private static Map<String,IncrementalStats> map;

/** Names of the counter that can stop the execution */
private static String[] statsNames;

/** Names of the counter that can stop the execution */
private static String[] stopNames;

/** The prefix to be printed */
private String prefix;

public StatsObserver(String prefix)
{
	if (prefix != null)
		throw new RuntimeException("Only one " + getClass() + " can be instantiated.");
	this.prefix = prefix;
	
	map = new HashMap<String,IncrementalStats>();
	String stats = Configuration.getString(prefix + "." + PAR_STATS, null);
	if (stats != null) {
		statsNames = stats.split(",");
		for (int i=0; i < statsNames.length; i++)
			map.put(statsNames[i], new IncrementalStats());
	}
	String stops = Configuration.getString(prefix + "." + PAR_STOPS, null);
	if (stops != null) {
		stopNames= stops.split(",");
		for (int i=0; i < stopNames.length; i++)
			map.put(stopNames[i], new IncrementalStats());
	}	
}

public static void add(String name, double val)
{
	if (map == null)
		return;
	IncrementalStats s = map.get(name);
	if (s == null) 
		throw new RuntimeException("Counter " + name + " does not exist");
	s.add(val);
}

public boolean execute()
{
	boolean stop = false;
	if (statsNames != null) {
		for (int i=0; i < statsNames.length; i++) {
			IncrementalStats in = map.get(statsNames[i]);
			System.out.println(prefix + ": TIME " + CommonState.getTime() + 
					" " + statsNames[i] + " " + in);
			in.reset();
		}
	}
	if (stopNames != null) {
		for (int i=0; i < stopNames.length; i++) {
			IncrementalStats in = map.get(stopNames[i]);
			System.out.println(prefix + ": TIME " + CommonState.getTime() + 
					" " + stopNames[i] + " " + in);
			if (in.getN() == 0)
			  stop = true;
			in.reset();
		}
	}
	return stop; 
}

}
