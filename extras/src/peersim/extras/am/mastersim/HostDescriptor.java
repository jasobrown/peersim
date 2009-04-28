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


public class HostDescriptor implements Comparable<HostDescriptor>
{

/** The host name of the machine */
private String hostname;

/** The max amount of memory that can be used by this machine */
private String memory;

/** The java command to be executed on the remote machine; defaults to java */
private String command;


/** 
 * The address of one machine representative of an entire domain;
 * files common to several machines will be sent just once.
 * 
 */
private String domain;

/** The number of failures  */
private int counter;


/**
 * @return Returns the memory.
 */
public String getMemory()
{
	return memory;
}

public String getDomain()
{
	return domain;
}

public HostDescriptor(String linedesc)
{
	String[] parts = linedesc.split(" ");
	if (parts.length != 3 || parts.length != 4) {
	  hostname = parts[0];
	  memory = parts[1];
	  domain = parts[2];
	  if (parts.length == 4)
		  command = parts[3];
	  else 
		  domain = "java";
	  counter = 0;
	}
}

public int compareTo(HostDescriptor o)
{
	if (counter < o.counter)
		return -1;
	else if (counter == o.counter)
		return 0;
	else 
		return +1;
}

/**
 * @return Returns the counter.
 */
public int getCounter()
{
	return counter;
}

/**
 * @param counter The counter to set.
 */
public void setCounter(int counter)
{
	this.counter = counter;
}

/**
 * @return Returns the hostname.
 */
public String getHostname()
{
	return hostname;
}


/**
 * @return the command
 */
public String getCommand()
{
	return command;
}

}
