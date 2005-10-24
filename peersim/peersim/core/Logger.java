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

/**
 * This is the generic interface to be implemented by 
 * logging facilities in peersim. Each line to be logged
 * is tagged with a log identifier of type String. 
 * This identifier can be used, for example, to 
 * separate the output in different streams based on
 * the identifier. Currently, all the observers in
 * the peersim code use their own prefix as identifier.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface Logger
{

/**
 * Prints the string <code>s</code> in the log stream specified 
 * by the identifier, and then terminates the line. Implementing 
 * classes are free to modify the actual string that is printed, 
 * for example inserting the log identifier or other strings.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void println(String logId, String s);

/**
 * Prints the string <code>s</code> in the log stream specified 
 * by the identifier. Implementing classes are free to modify 
 * the actual string that is printed, for example inserting the 
 * log identifier or other strings.
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void print(String logId, String s);

/**
 * Prints the string <code>s</code> in the log stream specified 
 * by the identifier. Implementing classes are supposed to
 * print the string "as it is", without additions or modifications.
 * Note: the 0 stands for "zero-modification". 
 * 
 * @param logId the log identifier
 * @param s the string to be printed
 */
public void print0(String logId, String s);

}
