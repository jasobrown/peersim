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

package aggregation.multiple;

/**
 * This interface has to be implemented by protocol objects
 * implementing multiple instances of an aggregation function.
 * It enables to get and set the values to be aggregated.
 * 
 * Note that values are represented as doubles. Actual
 * implementations can store values as floats, in order to
 * reduce memory footprint.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface MultipleValues
{

/**
 * Gets the i-th instance of the value to be aggregated.
 */
public double getValue(int i);

/**
 * Sets the i-the instance of the value to be aggregated.
 */
public void setValue(int i, double value);

/** 
 * Returns the number of values stored in this array.
 */
public int size();

/**
 * Returns true if this node has just been created, and cannot 
 * partecipate in an aggregation protocol. If the protocol does
 * not support restarting, it should return false.
 */
public boolean isNew();

}
