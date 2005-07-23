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

package peersim.config;

/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MissingParameterException
extends RuntimeException
{
	public MissingParameterException(String name)
	{
		super("Parameter \"" + name + "\" not found.");
	}

	public MissingParameterException(String name, String motivation)
	{
		super("Parameter \"" + name + "\" not found " + motivation);
	}

	public String getMessage() {
		
		StackTraceElement[] stack = getStackTrace();
		
		// Search the element that invoked Configuration
		// It's the first whose class is different from Configuration
		int pos;
		for (pos=0; pos < stack.length; pos++) {
			if (!stack[pos].getClassName().equals(Configuration.class.getName()))
				break;
		}

		return super.getMessage()+"\nAt "+
			getStackTrace()[pos].getClassName()+"."+
			getStackTrace()[pos].getMethodName()+":"+
			getStackTrace()[pos].getLineNumber();
	}
}
