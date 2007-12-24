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

package peersim.extras.am.id;

import peersim.core.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class IDHolderImpl implements IDHolder, Protocol
{

/** Identifier */
private long id;

/**
 * 
 */
public IDHolderImpl(String prefix)
{
}

private IDHolderImpl()
{
}

public Object clone()
{
	return new IDHolderImpl();
}

// Comment inherited from interface
public long getID()
{
	return id;
}

// Comment inherited from interface
public void setID(long key)
{
	this.id = key;
}
}
