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

package peersim.vector;

import peersim.core.*;
import java.util.*;

/**
 * This comparator class compares two node objects based on the value 
 * maintained by one of its protocols. The protocol must implemente the
 * SingleValue interface; its identifier has to be specified when a
 * new comparator is built.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class SingleValueComparator implements Comparator 
{

	/** Protocol to be be compared */
  private int pid;
  
  /**
   * Builds a new comparators that compares the single values maintained
   * by protocol identified by <code>pid</pid>.
   */
  public SingleValueComparator(int pid) { this.pid = pid; }
	
	// Comment inherited from interface
	public int compare(Object o1, Object o2)
	{
		SingleValue s1 = (SingleValue) ((Node) o1).getProtocol(pid);
		SingleValue s2 = (SingleValue) ((Node) o2).getProtocol(pid);
		return (int) (s1.getValue() - s2.getValue());
	}
	
}
