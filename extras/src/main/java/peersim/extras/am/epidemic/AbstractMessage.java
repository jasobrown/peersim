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

package peersim.extras.am.epidemic;

import peersim.core.*;


public class AbstractMessage implements Message
{

protected Node sender;

protected boolean isRequest;

protected short pid;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

public AbstractMessage()
{
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

public Node getSender()
{
	return sender;
}

public void setSender(Node sender)
{
	this.sender = sender;
}

//Comment inherited from interface
public boolean isRequest()
{
	return isRequest;
}

// Comment inherited from interface
public void setRequest(boolean isRequest)
{
	this.isRequest = isRequest;
}

//Comment inherited from interface
public int getPid()
{
	return pid;
}

//Comment inherited from interface
public void setPid(int pid)
{
	this.pid = (short) pid;
}

}
