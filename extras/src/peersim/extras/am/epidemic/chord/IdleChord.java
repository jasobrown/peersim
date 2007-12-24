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

package peersim.extras.am.epidemic.chord;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * This class contains an idle protocol implementing the {@link Chord} interface.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class IdleChord implements Protocol, Chord
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------
/**
 * Number of successors.
 * @config
 */
private static final String PAR_SUCC = "successors";

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------
/** Successors */
private Node[] leafs;

/** Fingers */
private Node[] fingers;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------
/**
 * Initializes an empty Chord node.
 */
public IdleChord(String prefix)
{
	leafs = new Node[Configuration.getInt(prefix + "." + PAR_SUCC)];
	fingers = new Node[ID.BITS];
}

/** Cloning constructor */
private IdleChord(IdleChord c)
{
	leafs = new Node[c.leafs.length];
	fingers = new Node[ID.BITS];
}

/**
 * Clone an empty Chord node.
 */
public Object clone()
{
	return new IdleChord(this);
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------
// Comment inherited from interface
public Node getSuccessor(int index)
{
	return leafs[index];
}

// Comment inherited from interface
public void setSuccessor(int index, Node node)
{
	leafs[index] = node;
}

// Comment inherited from interface
public Node getFinger(int index)
{
	return fingers[index];
}

// Comment inherited from interface
public void setFinger(int index, Node node)
{
	fingers[index] = node;
}

// Comment inherited from interface
public int successors()
{
	return leafs.length;
}
}
