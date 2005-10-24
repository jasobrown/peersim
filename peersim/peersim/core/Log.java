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

/**
 * This utility class must be used by observers to report their data.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class Log
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * If specified, this parameter specifies the logging class to be used.
 * The logging class must implement interface {@link peersim.core.Logger}.
 * If absent, {@link peersim.core.DefaultLogger} is used. 
 * @config
 */
public static final String PAR_LOG = "log";

//--------------------------------------------------------------------------
//Static fields
//--------------------------------------------------------------------------

/** The actual logging facilities to be used */
private static Logger logger;

//--------------------------------------------------------------------------
//Static initializer
//--------------------------------------------------------------------------

static {
	try {
		logger = (Logger) Configuration.getInstance(PAR_LOG);
	} catch (MissingParameterException e) {
		logger = new DefaultLogger(PAR_LOG);
	}
}
  

//--------------------------------------------------------------------------
//Class methods
//--------------------------------------------------------------------------

/**
 * Prints the string <code>s</code> in the log stream specified 
 * by the identifier, and then terminates the line. The current 
 * logger free to modify the actual string that is printed, 
 * for example inserting the log identifier or other strings.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public static void println(String logId, String s)
{
	logger.println(logId, s);
}

/**
 * Prints the string <code>s</code> in the log stream specified 
 * by the identifier. The current logger is free to modify 
 * the actual string that is printed, for example inserting the 
 * log identifier or other strings.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public static void print(String logId, String s)
{
	logger.print(logId, s);
}

/**
 * Prints the string <code>s</code> in the log stream specified 
 * by the identifier. The current logger is supposed to
 * print the string "as it is", without additions or modifications.
 * Note: the 0 stands for "zero-modification".
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public static void print0(String logId, String s)
{
	logger.print0(logId, s);
}

}

