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
 * Created July 14, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

/**
 * The IConnectionService interface is used as a GWT and XML webservice.  It 
 * is also used by the Datasource Service to map a name to an IConnection. 
 */
public interface IConnectionService {
  List<IDatabaseConnection> getConnections() throws ConnectionServiceException;
  IDatabaseConnection getConnectionByName(String name) throws ConnectionServiceException;
  boolean addConnection(IDatabaseConnection connection) throws ConnectionServiceException;
  boolean updateConnection(IDatabaseConnection connection) throws ConnectionServiceException;
  boolean deleteConnection(IDatabaseConnection connection) throws ConnectionServiceException;
  boolean deleteConnection(String name) throws ConnectionServiceException;
  boolean testConnection(IDatabaseConnection connection) throws ConnectionServiceException;
  boolean isConnectionExist(String connectionName) throws ConnectionServiceException;
}
