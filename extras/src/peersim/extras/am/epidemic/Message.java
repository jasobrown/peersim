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

package peersim.extras.am.epidemic;

import peersim.core.*;

/**
 * This interface represents a generic message used in the peersim.extras.am.epidemic
 * protocol. This interface enables to obtain the sender of the
 * message and to distinguish whether this is a request or 
 * response message in the pull-push protocol. 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public interface Message
{

/**
 * @return true if this message is a request.
 */
public abstract boolean isRequest();

/**
 * Sets the request status of this message.
 */
public abstract void setRequest(boolean isRequest);

/**
 * @return the sender of this message.
 */
public abstract Node getSender();

/**
 * Sets the sender of this message.
 */
public abstract void setSender(Node sender);

/**
 * Return the id of the process to which this message
 * is addressed. This is needed because a single
 * peersim.extras.am.epidemic maneger may manage several different
 * layers. 
 */
public abstract int getPid();

/** 
 * Sets the id of the process to which this message
 * is addressed.
 */
public abstract void setPid(int pid);

}