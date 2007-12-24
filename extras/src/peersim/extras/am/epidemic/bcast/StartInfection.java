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

package peersim.extras.am.epidemic.bcast;

import peersim.config.*;
import peersim.core.*;


public class StartInfection implements Control
{

private static final String PAR_INFECTABLE = "infectable";

private final int pid;

public StartInfection(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_INFECTABLE); 
}

public boolean execute()
{
	Infectable i = (Infectable) Network.get(0).getProtocol(pid);
	i.setInfected(true);
	return false;
}

}
