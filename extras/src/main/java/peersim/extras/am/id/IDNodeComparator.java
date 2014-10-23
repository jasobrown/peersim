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
import java.util.*;

/**
 * This comparator class compares two node objects based on the value maintained
 * by one of its protocols. The protocol must implemente the IDHolder interface;
 * its identifier has to be specified when a new comparator is built.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class IDNodeComparator implements Comparator
{

/** Protocol to be be compared */
protected int pid = -1;

/**
 * Builds a new Comparator that compares the ids maintained by protocols
 * identified by <code>pid</pid>.
 */
public IDNodeComparator(int pid)
{
	this.pid = pid;
}

// Comment inherited from interface
public int compare(Object o1, Object o2)
{
	IDHolder s1 = (IDHolder) ((Node) o1).getProtocol(pid);
	IDHolder s2 = (IDHolder) ((Node) o2).getProtocol(pid);
	long diff = s1.getID() - s2.getID();
	if (s1.getID() < s2.getID())
		return -1;
	else if (s1.getID() == s2.getID())
		return 0;
	else
		return +1;
}
}