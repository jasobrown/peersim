/*
 * Copyright (c) 2008 M. Jelasity and N. Tolgyesi
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

package peersim.extras.mj.ednewscast;

/**
 * This class represents the event that signals the periodic execution of
 * a (for example, gossip) protocol. 
 */

class CycleMessage {

/** A singleton to be used instead of constructing
* new instances all the time. */
public static final CycleMessage inst = new CycleMessage();

}

