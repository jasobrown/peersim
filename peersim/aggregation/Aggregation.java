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
 
package aggregation;

/**
 *  This interface has to be implemented by protocol objects
 *  implementing an aggregation function. It enables the
 *  system to get and set the values to be aggregated.
 * 
 *  Note that values are represented as doubles. Actual
 *  implementations can store values as floats, in order to
 *  reduce memory footprint.
 *
 *  @author Alberto Montresor
 *  @version $Revision$
 */
public interface Aggregation 
{

   /**
    *  Get the value to be aggregated.
    */
   public double getValue();
   
  /**
   *  Set the value to be aggregated.
   */
   public void setValue(double value);

}
