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

package peersim.core;

import peersim.config.*;


public class DefaultLogger implements Logger
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * If this parameter is defined, the current time is added at the beginning
 * of each line; the format is "TIME t ", where t is the current time
 * as returned by {@link CommonState#getTime()}.
 * @config
 */
protected static final String PAR_TIME = "time";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** If true, the current time is printed on each line */
protected final boolean logtime;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/** */
public DefaultLogger(String name)
{
	logtime = Configuration.contains(name + "." + PAR_TIME);
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/**
 * Print the string prefixed by the log identifier, and then
 * terminates the line. If parameter {@value #PAR_TIME} is 
 * defined, prints also the current time.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void println(String logId, String s)
{
	System.out.print(logId + ": ");
	if (logtime) {
		System.out.print("TIME " + CommonState.getTime() + " ");
	}
	System.out.println(s);
}

/**
 * Print the string prefixed by the log identifier. If parameter
 * {@value #PAR_TIME} is defined, prints also the current
 * time.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void print(String logId, String s)
{
	System.out.print(logId + ": ");
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
