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

import java.util.*;

/**
 * This comparator class compares two objects implementing the IDHolder
 * interface.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class IDComparator implements Comparator
{

/**
 * Builds a new comparator.
 */
public IDComparator()
{
}

// Comment inherited from interface
public int compare(Object o1, Object o2)
{
	IDHolder s1 = (IDHolder) o1;
	IDHolder s2 = (IDHolder) o2;
	long diff = s1.getID() - s2.getID();
	if (diff < 0)
		return -1;
	else if (diff == 0)
		return 0;
	else
		return +1;
}
}