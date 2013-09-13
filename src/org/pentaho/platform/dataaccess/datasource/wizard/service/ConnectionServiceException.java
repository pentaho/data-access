/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.io.Serializable;
import com.google.gwt.http.client.Response;

public class ConnectionServiceException extends Exception implements Serializable {


	private static final long serialVersionUID = 1L;
	protected int statusCode;
	
	public ConnectionServiceException()
	{
	  super();
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
	}

	public ConnectionServiceException(String message)
	{
		super(message);
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
	}

	public ConnectionServiceException(Throwable cause)
	{
		super(cause);
		this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
	}

  public ConnectionServiceException(String message, Throwable cause)
  {
    super(message, cause);
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
  }

  public ConnectionServiceException(int statusCode, String message, Throwable cause)
	{
		super(message, cause);
		this.statusCode = statusCode;
	}
	
  public ConnectionServiceException(int statusCode, String message)
  {
    this(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode(){
	  return statusCode;
	}

}
