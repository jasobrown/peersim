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

package peersim.util;

/**
 * Utility methods.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class Util 
{
  /**
   * Returns the unqualified name of the specified class.
   */
  public static String getName(Class clazz)
  {
    String name = clazz.getName();
    return name.substring(name.lastIndexOf(".")+1, name.length());
  }
  
  public static void main(String[] args)
  {
    System.out.println(getName(String.class));
    System.out.println(getName(java.io.Serializable.class));
  }

}
