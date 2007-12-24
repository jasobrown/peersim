/*
 * Copyright (c) 2001 The Anthill Team
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

package peersim.extras.am.mastersim;

import java.util.*;

import java.util.Arrays;

/**
 *  Min Heap data structure. To obtain a Max Heap, it sufficient
 *  to change the comparator object to be used.
 *  
 *  @author Alberto Montresor
 *  @version $Revision$
 */
class Heap {

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** Default size */
private static final int DEFAULT_SIZE = 65536;

//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------


/** Event component of the heap */
private Comparable[] objs;

/** Number of elements */
private int size;


//--------------------------------------------------------------------------
// Contructor
//--------------------------------------------------------------------------

/**
 * Initializes a new heap with the default initial size.
 */
public Heap() {
	this(DEFAULT_SIZE);
}

//--------------------------------------------------------------------------

/**
 * Initializes a new heap with the specified initial size.
 */
public Heap(int maxsize) {
	objs = new Comparable[maxsize+1];
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * Returns the current number of event in the system.
 */
public int size()
{
	return size;
}

//--------------------------------------------------------------------------

/**
 * Add a new object to the heap.
 * 
 * @param c the object to be added
 */
public synchronized void add(Comparable c) 
{
	size++;
	int pos = size;
	objs[pos] = c;
//	System.out.println("pos " + pos);
//	System.out.print("--> "); for (int i=1; i <= size; i++) System.out.print(objs[i]+","); System.out.println("");
	while (pos > 1 && c.compareTo(objs[pos/2])<0) {
		swap(pos, pos / 2);
//		System.out.println("pos ->" + pos);
		pos = pos / 2;
	}
	this.notify();
}

//--------------------------------------------------------------------------

/**
 * Removes the first event in the heap and returns it.
 * Note that, to avoid garbage collection, a singleton instance of
 * the Event class is used. This means that data contained in the
 * returned event are overwritten when a new invocation of this
 * method is performed.
 * @return first event or null if size is zero
 */
public synchronized Comparable removeFirst() 
{
  while (size == 0) {
  	try {
			this.wait();
		} catch (InterruptedException e) {
		}
  }
	Comparable c = objs[1];
	swap(1, size);
	size--;
	minHeapify(1);
	return c;
}

//--------------------------------------------------------------------------

/** 
 *  Prints the time values contained in the heap.
 */
public synchronized String toString()
{
	StringBuffer buffer = new StringBuffer();
	buffer.append("[Size: " + size + " Objs: ");
	for (int i=1; i <= size; i++) {
		buffer.append(objs[i]+",");
	}
	buffer.append("]");
	return buffer.toString();
}

//--------------------------------------------------------------------------
// Private methods
//--------------------------------------------------------------------------

/**
 * 
 */
private void minHeapify(int index) 
{
	// Left, right children of the current index
	int l,r; 
	// The minimum time between val, lt, rt
	long mintime;
	// The index of the mininum time
	int minindex = index; 
	do {
		index = minindex;
		Comparable min = objs[index];
		l = index << 1;
		r = l + 1;
		if (l <= size && objs[l].compareTo(min)<0) {
			minindex = l;
			min = objs[l];
		}
		if (r <= size && objs[r].compareTo(min)<0) {
			minindex = r;
			min = objs[r];
		}
		if (minindex != index) {
			swap(minindex, index);
		}
	} while (minindex != index);
}

//--------------------------------------------------------------------------

/**
 * 
 */
private void swap(int i1, int i2) {
	
	Comparable te = objs[i1];
	objs[i1] = objs[i2];
	objs[i2] = te;
}

//--------------------------------------------------------------------------

/**
 * 
 */
private void doubleCapacity() {
	int oldsize = objs.length;
	int newsize = oldsize*2;
	Comparable[] newobjs = new Comparable[newsize];
	System.arraycopy(objs, 0, newobjs, 0, oldsize);
	objs = newobjs;
}

//--------------------------------------------------------------------------
// Testing
//--------------------------------------------------------------------------


public static void main(String[] args) {
	Random random = new Random();
	int rep = 1000000;
	Heap heap = new Heap(rep+1);
	Long[] values1 = new Long[rep];
	Long[] values2 = new Long[rep];
	for (int i = 0; i < rep; i++) {
		values1[i] = random.nextLong(); 
	}
	
	long time1 = System.currentTimeMillis();
	for (int i = 0; i < rep; i++) {
		heap.add(values1[i]);
	}
	long time2 = System.currentTimeMillis();
	System.out.println("Inserting: " + (time2-time1));
	
	time1 = System.currentTimeMillis();
	for (int i = 0; i < rep; i++) {
		values2[i] = (Long) heap.removeFirst();
	}
	time2 = System.currentTimeMillis();
	System.out.println("Removing: " + (time2-time1));
	
	Arrays.sort(values1);
	for (int i=0; i<rep; i++) {
		if (values1[i] != values2[i]) {
			System.out.println(i + " " + values1[i] + " " + values2[i]);
		}
	}
}

} // END Heap
