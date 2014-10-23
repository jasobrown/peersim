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

package aggregation.secure;

import peersim.core.*;
import peersim.config.*;

/**
 * This {@link Control} object is used to periodically reset the history
 * of all nodes in the system.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ResetHistories
implements Control
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter that defines the protocol to initialize.
 * Parameter read will has the full name
 * <tt>prefix+"."+PAR_PROT</tt>
 */
public static final String PAR_PROTOCOL = "protocol";


//--------------------------------------------------------------------------
// Variables
//--------------------------------------------------------------------------

/** Protocol identifier */
private final int pid;


//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Read the configuration parameter.
 */
public ResetHistories(String prefix)
{
	pid = Configuration.getPid(prefix+"."+PAR_PROTOCOL);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean execute() 
{
	for(int i=0; i<Network.size(); ++i)
	{
		History history = (History)
			Network.get(i).getProtocol(pid);
		history.reset();
	}
	
	return false;
}

//--------------------------------------------------------------------------

}
