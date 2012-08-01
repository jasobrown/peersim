/*
 * Copyright (c) 2007 The BISON Project
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

package peersim.extras.am.util;

/**
 * Utility class containing methods for array manipulation.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ArrayUtil
{

/**
 * Removes duplicates from the object array <code>objs</code>,
 * considering only the first <code>size</code> elements. Returns the new
 * size of the array (i.e., the number of remaining elements after
 * duplicate removal).
 */
public static int removeDups(Object[] objs, int size)
{
	int inew = 1;
	int iold = 1;
	while (iold < size) {
		if (objs[iold] != objs[inew - 1])
			objs[inew++] = objs[iold];
		iold++;
	}
	for (int i = inew; i < objs.length; i++) {
		objs[i] = null;
	}
	return inew;
}

/**
 * Removes duplicates from the object array <code>objs</code>. Returns
 * the new size of the array (i.e., the number of remaining elements after
 * duplicate removal).
 */
public static int removeDups(Object[] objs)
{
	return removeDups(objs, objs.length);
}

/**
 * Returns true if the specified object <code>obj</code> appears in the
 * object array <code>array</code>.
 */
public static boolean contains(Object[] array, int size, Object obj)
{
	for (int i = 0; i < size; i++)
		if (array[i] == obj)
			return true;
	return false;
}

/** 
 * Returns a new array of size newSize, contanining the first
 * oldSize elements of the original array.
 */
public static int[] resizeArray(int[] array, int oldSize, int newSize)
{
	if (array == null)
		return null;
	int[] temp = new int[newSize];
	System.arraycopy(array, 0, temp, 0, oldSize);
	return temp;
}

/** 
 * Returns a new array of size newSize, contanining the first
 * oldSize elements of the original array.
 */
public static int[][] resizeArray(int[][] array, int oldSize, int newSize)
{
	if (array == null)
		return null;
	int[][] temp = new int[newSize][];
	System.arraycopy(array, 0, temp, 0, oldSize);
	return temp;
}

}
