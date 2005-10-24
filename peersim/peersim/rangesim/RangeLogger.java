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

package peersim.rangesim;

import peersim.config.*;
import peersim.core.*;

/**
 * Extends {@link peersim.core.DefaultLogger} by adding a configurable
 * prefix to all lines to be printed. This feature is used by the
 * RangeSimulator to add the value of the range variables.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class RangeLogger extends DefaultLogger
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * If defined, the current time is printed on each line.
 * @config
 */
protected static final String PAR_PREFIX = "prefix";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** The prefix to be printed at each line */
protected final String prefix;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/** */
public RangeLogger(String name)
{
	super(name);
	prefix = Configuration.getString(name + "." + PAR_PREFIX, "");
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/**
 * Print the string prefixed by the log identifier, the
 * prefix specified by parameter {@value #PAR_PREFIX} and 
 * then terminates the line. If parameter {@value #PAR_TIME} 
 * is defined, prints also the current time.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void println(String logId, String s)
{
	System.out.print(logId + ": ");
	System.out.print(prefix + " ");
	if (logtime) {
		System.out.print("TIME " + CommonState.getTime() + " ");
	}
	System.out.println(s);
}

/**
 * Print the string prefixed by the log identifier and the
 * prefix specified by parameter {@value #PAR_PREFIX}. 
 * If parameter {@value #PAR_TIME} is defined, prints also 
 * the current time.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void print(String logId, String s)
{
	System.out.print(logId + ": ");
	System.out.print(prefix + " ");
	if (logtime) {
		System.out.print("TIME " + CommonState.getTime() + " ");
	}
	System.out.print(s);
}

/**
 * Print the string without modification. 
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void print0(String logId, String s)
{
	System.out.print(s);
}

}
