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
		
package peersim.init;

/**
* Generic interface to initialize a simulation. It is designed to allow
* maximal flexibility therefore poses virtually no restrictions on the
* implementation. It can be used to imlpement initializations before the
* simulation that require global knowledge of the system.
*/
public interface Initializer {

	/**
	* Performs arbitrary initializations or modifications on the overlay
	* nework and protocols before the simulation.
	* Implementations will typically know many details of the
	* actual overlay network and protocols, but there will be general
	* purpose reusable initializers too for example to set up a topology.
	*/
	public void initialize();
}

