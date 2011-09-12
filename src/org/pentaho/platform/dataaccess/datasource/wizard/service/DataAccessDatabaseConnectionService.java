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
 * Created June 30, 2009
 * @author wgorman
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.Iterator;
import java.util.List;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.ConnectionServiceHelper;

public class DataAccessDatabaseConnectionService extends DatabaseConnectionService {
  private static final byte[] lock = new byte[0];
  /**
   * This service method overrides it's parent and removes all the database types
   * other than native.
   */
  @Override
  public List<IDatabaseType> getDatabaseTypes() {
     synchronized (lock) {
       List<IDatabaseType> databaseTypes = super.getDatabaseTypes();
       for (IDatabaseType type : databaseTypes) {
         Iterator<DatabaseAccessType> iter = type.getSupportedAccessTypes().iterator();
         while (iter.hasNext()) {
           DatabaseAccessType accessType = iter.next();
           if (accessType != DatabaseAccessType.NATIVE) {
             iter.remove();
           }
         }
       }
       return databaseTypes;
     }
  }

  @Override
  public String testConnection(IDatabaseConnection connection) {
    try {
      
      connection.setPassword(ConnectionServiceHelper.getConnectionPassword(connection.getName(), connection.getPassword()));
      return super.testConnection(connection);
    } catch (ConnectionServiceException e) {
      return super.testConnection(connection);
    }
  }
}
