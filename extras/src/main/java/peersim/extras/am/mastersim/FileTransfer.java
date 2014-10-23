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


public interface FileTransfer
{

  /**
   * Transfers the specified file to the remote host.
   * The exact position where the file will be copied
   * is returned, which depends on the particular
   * implementation.
   *  
   * @param host the host file
   * @param file the file to be transfered
   * @return the position on the remote host where 
   *   the file has been copied.
   */
  public String transfer(Set done, String host, String domain, File file);

}
