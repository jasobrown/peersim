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

package distributions;

import peersim.core.*;

/**
 * The task of this protocol is to store a single double value and make it
 * available through the SingleValue interface.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class SingleValueHolder 
implements SingleValue, Protocol
{

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------
	
/** Value held by this protocol */
private double value;
	

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Builds a new (not initialized) value holder.
 */
public SingleValueHolder(String prefix)
{
}

//--------------------------------------------------------------------------

/**
 * Clones the value holder.
 */
public Object clone() throws CloneNotSupportedException
{
	return super.clone();
}

//--------------------------------------------------------------------------
//methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public double getValue()
{
	return value;
}

//--------------------------------------------------------------------------

// Comment inherited from interface
public void setValue(double value)
{
	this.value = value;
}

//--------------------------------------------------------------------------

/**
 * Returns the value as a string.
 */
public String toString() { return ""+value; }

}
