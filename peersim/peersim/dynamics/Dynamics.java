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
		
package peersim.dynamics;

/**
* Generic interface to modify the underlying network
* (accessibility, failures, node removal and addition) and the state
* of the protocols.
* It is designed to allow
* maximal flexibility therefore poses virtually no restrictions on the
* implementation.
* It is a time-based concept which works fine with both cycle based and
* event based simulation.
*/
public interface Dynamics {

	/**
	* Performs arbitrary modifications on the components.
	*/
	public void modify();
}


