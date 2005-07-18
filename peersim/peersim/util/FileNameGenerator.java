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
		
package peersim.util;

import java.io.*;


/**
* Generates a series of filenames for classes that have to save eg
* snapshots regularly. 
*/
public class FileNameGenerator {


/**
* The number of filenames aready returned.
*/
private long counter = 0;


private final String prefix;

private final String ext;


// ==================== initialization ==============================
// ==================================================================


/**
* @param prefix all returned names will be prefixed by this
* @param ext will be appended to all returned names
*/
public FileNameGenerator(String prefix, String ext) {
	
	this.prefix=prefix;
	this.ext=ext;
}


// ===================== methods ====================================
// ==================================================================


/**
* Generates a name based on a counter. It will be filled in with leading
* zeros so that the number is always 8 characters long.
*/
public String nextCounterName() {
	
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	(new PrintStream(baos)).printf("%08d",counter);
	counter++;
	return prefix+baos.toString()+ext;
}

// ------------------------------------------------------------------

public static void main(String args[]) {
	
	FileNameGenerator fng = new FileNameGenerator(args[0],args[1]);
	for(int i=0; i<100; ++i) System.err.println(fng.nextCounterName()); 
}

}

