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

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncConnectionService {
  void getConnections(XulServiceCallback<List<Connection>> callback);
  void getConnectionByName(String name, XulServiceCallback<Connection> callback);
  void addConnection(Connection connection, XulServiceCallback<Boolean> callback);
  void updateConnection(Connection connection, XulServiceCallback<Boolean> callback);
  void deleteConnection(Connection connection, XulServiceCallback<Boolean> callback);
  void deleteConnection(String name, XulServiceCallback<Boolean> callback);
  void testConnection(Connection connection, XulServiceCallback<Boolean> callback);
  void convertToConnection(IDatabaseConnection databaseConnection, XulServiceCallback<Connection> callback);
  void convertFromConnection(Connection databaseConnection, XulServiceCallback<IDatabaseConnection> callback);
}
