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
 * This OutputStream uses an underlying stream to output
 * data. Each line (terminated with `\n`) is augmented
 * with a tag character. This is used to discriminate
 * among standard error and standard output. This 
 * feature is needed for launching new JVMs; it should
 * not be used for other purposes. 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class TaggedOutputStream extends BufferedOutputStream
{


/** 
 * This character is appended at the end of each line. 
 */
public static final int TAG = 1;

/**
 * Creates a tagged output stream that prints the tagged
 * output on the specified stream.
 */
public TaggedOutputStream(OutputStream out)
{
	super(out);
}

/**
 * Creates a tagged output stream that prints the tagged
 * output on the specified stream.
 */
public TaggedOutputStream(OutputStream out, int size)
{
	super(out, size);
}

// Comment inherited from interface
@Override
public synchronized void write(byte[] b, int off, int len) throws IOException
{
	int last = off+len;
	for (int i=off; i < last; i++) {
		if (b[i] == '\n')
			out.write(TAG);
		out.write(b[i]);
	}
}

// Comment inherited from interface
@Override
public synchronized void write(int b) throws IOException
{
	if (b == '\n')
		out.write(TAG);
	out.write(b);
}

}
