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
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.ui.xul.XulServiceCallback;

public class ConnectionServiceDebugImpl implements IXulAsyncConnectionService {

  InMemoryConnectionServiceImpl SERVICE;
  
  public ConnectionServiceDebugImpl(){
    SERVICE = new InMemoryConnectionServiceImpl();
  }
 
  public void getConnections(XulServiceCallback<List<IConnection>> callback) {
    try {
      callback.success(SERVICE.getConnections());
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }
  public void getConnectionByName(String name, XulServiceCallback<IConnection> callback) {
    try {
      callback.success(SERVICE.getConnectionByName(name));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }
  public void addConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.addConnection(connection));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }
  public void updateConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.updateConnection(connection));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }
  public void deleteConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.deleteConnection(connection));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }
  public void deleteConnection(String name, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.deleteConnection(name));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }
  public void testConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.testConnection(connection));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }

  public void convertFromConnection(IConnection databaseConnection, XulServiceCallback<IDatabaseConnection> callback) {
    try {
      callback.success(SERVICE.convertFromConnection(databaseConnection));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }

  public void convertToConnection(IDatabaseConnection databaseConnection, XulServiceCallback<IConnection> callback) {
    try {
      callback.success(SERVICE.convertToConnection(databaseConnection));
    } catch (ConnectionServiceException e) {
      callback.error(e.getMessage(), e);
    }
  }  
}

  