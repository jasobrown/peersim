/*
 * Copyright (c) 2003 The BISON Project
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

package aggregation;

import peersim.core.*;
import peersim.reports.*;
import peersim.util.Log;
import peersim.config.*;

/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MaxObserver implements Observer
{

	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////

	/** 
	 *  String name of the parameter used to determine the output style of
	 *  the observer. If style is equal to "iterations", the number of 
	 *  iterations needed to reach all nodes in the system is printed.
	 *  If style is equal to "detailed", the status at each cycle is
	 *  printed. 
	 */
	public static final String PAR_STYLE = "style";

	/** 
	 *  String name of the parameter used to select the protocol to operate on
	 */
	public static final String PAR_PROT = "protocol";

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

	/** The name of this observer in the configuration */
	private final String name;

	/** Output style */
	private final String style;

	/** Protocol identifier */
	private final int pid;

	////////////////////////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////////////////////////

	/**
	 *  Creates a new observer using clear()
	 */
	public MaxObserver(String name)
	{
		this.name = name;
		style = Configuration.getString(name + "." + PAR_STYLE, "iterations");
		if (!"iterations".equalsIgnoreCase(style) && 
		    !"detailed".equalsIgnoreCase(style)) {
		  throw new IllegalArgumentException(name + "." + PAR_STYLE + 
        " should be equal to \"detailed\" or \"iterations\"");    	
		}
		pid = Configuration.getInt(name + "." + PAR_PROT);
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////

	// Comment inherited from interface
	public boolean analyze()
	{
		int time = peersim.core.CommonState.getT();
		
		final int len = OverlayNetwork.size();
		double max = Double.MIN_VALUE;
		int count = 0;

		/* Count number of max  */
		for (int i = 0; i < len; i++)
		{
			Node node = OverlayNetwork.get(i);
			Aggregation protocol = (Aggregation) node.getProtocol(pid);
			double value = protocol.getValue();
			if (value > max)
			{
				max = value;
				count = 0;
			}
			if (value == max) 
			{
				count = count + 1;
			}
		}

    
    if ("iterations".equalsIgnoreCase(style)) {
    	if (count == OverlayNetwork.size()) 
    	{
    		Log.println(name, "" + time);
    		return true;
    	} else 
    	{
    	  return false;
			}
    } else if ("detailed".equalsIgnoreCase(style)) 
    {
			Log.println(name, time + " " + count);
			return (count == OverlayNetwork.size());
    } 
    // Never reached
    return true;
	}

} 
