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
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the GWT Asynchronous implementation of the IConnectionService API
 */
public interface IGwtConnectionServiceAsync {
  void getConnections(AsyncCallback<List<Connection>> callback);
  void getConnectionByName(String name, AsyncCallback<Connection> callback);
  void addConnection(Connection connection, AsyncCallback<Boolean> callback);
  void updateConnection(Connection connection, AsyncCallback<Boolean> callback);
  void deleteConnection(Connection connection, AsyncCallback<Boolean> callback);
  void deleteConnection(String name, AsyncCallback<Boolean> callback);
  void testConnection(Connection connection, AsyncCallback<Boolean> callback);
  void convertToConnection(IDatabaseConnection connection, AsyncCallback<Connection> callback);
  void convertFromConnection(Connection connection, AsyncCallback<IDatabaseConnection> callback);
}
