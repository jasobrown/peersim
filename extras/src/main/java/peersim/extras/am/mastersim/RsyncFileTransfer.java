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

package peersim.extras.am.mastersim;

import java.io.*;
import java.util.*;


public class RsyncFileTransfer implements FileTransfer
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/**
 * The temporary directory in the remote hosts to be used
 * as buffer for transferred files. Defaults to ".tmp".
 * @config
 */
private static final String PAR_TMP = "tempdir";


//---------------------------------------------------------------------
// Constants
//---------------------------------------------------------------------

/** Default directory */
private static final String TMP_DEFAULT = ".tmp.";

//---------------------------------------------------------------------
// Fields
//---------------------------------------------------------------------

/** Temporary directory */
private final String tmpdir = TMP_DEFAULT;

//---------------------------------------------------------------------
// Initialization
//---------------------------------------------------------------------

public RsyncFileTransfer(String prefix)
{
}

public String transfer(Set done, String host, String domain, File file)
{
	if (!file.exists()) {
		throw new IllegalArgumentException("File " + file + " does not exist");
	}
	if (file.isDirectory()) {
		throw new IllegalArgumentException("File " + file + " is a directory");
	}
	
	String tmpfile = createTmpName(domain, file);
	if (done.contains(tmpfile))
		return tmpfile;
	else
		done.add(tmpfile);
	
	ArrayList<String> list = new ArrayList<String>(20);
	
	// Launching mechanism
	list.add("rsync");
	list.add("-az");
	list.add("-e");
	list.add("ssh");
	list.add(file.getAbsolutePath());
	list.add(domain+":"+tmpfile);
	
	// Prepare the argument array for process forking
	String[] newargs = list.toArray(new String[list.size()]);
	System.out.println(list);

	// Fork an external process
	Process p = null;
	try {
		ProcessBuilder pb = new ProcessBuilder(list.toArray(newargs));
		pb.redirectErrorStream(true);
		p = pb.start();
	} catch (IOException e1) {
		System.err.println("Unable to fork process");
		System.err.println(e1.getMessage());
		return null;
	}

	// Read the output from the process and redirect it to System.out
	// and System.err.
	BufferedReader toprint = new BufferedReader(
			new InputStreamReader(p.getInputStream()));
	String line;
	while ((line = getLine(toprint)) != null) {
		// If there is something to print, it is cause
		// by an error.
		System.err.println(line);
	}
	
	// Wait for the process termination
	Integer ret = null;
	while (ret == null) {
		try {
			ret = p.waitFor();
		} catch (InterruptedException e) { }
	}

	if (ret < 0) {
		System.err.println("Exit code rsync: " + ret);
		return null;
	}

	return tmpfile;
}

private String createTmpName(String host, File file)
{
	return tmpdir + host + file.getAbsolutePath().replace('/','.');
}

private static String getLine(BufferedReader toprint)
{
	try {
		return toprint.readLine();
	} catch (IOException e) {
		e.printStackTrace();
		return null; 
	}
}
}
