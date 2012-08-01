/*
 * Copyright (c) 2010 Alberto Montresor
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


public final class IntArray
{

private int[] A;
private int size;

public IntArray(int capacity)
{
	size=0;
	A = new int[capacity];
}

public Object clone()
{
	IntArray temp = new IntArray(size);
	temp.size = size;
	for (int i=0; i < size; i++) {
		temp.A[i] = A[i];
	}
	return temp;
}



public boolean contains(int v)
{
	for (int i=0; i < size; i++) {
		if (A[i] == v) {
			return true;
		}
	}
	return false;
}

public void append(int v) 
{
  if (size == A.length) {
  	  A = ArrayUtil.resizeArray(A, A.length, A.length*2);
  }
  A[size] = v;
  size++;
}

public int get(int i) 
{
	return A[i];
}

public int size()
{
	return size;
}

public void reset()
{
	size = 0;
}


}
