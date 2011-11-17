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
 * Created July 23, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

public  class ConnectionServiceHelper {
  private static final Log logger = LogFactory.getLog(ConnectionServiceHelper.class);
  private static IDatasourceMgmtService datasourceMgmtSvc;
  private static char PASSWORD_REPLACE_CHAR = '*';

  static {
    IPentahoSession session = PentahoSessionHolder.getSession();
    datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session);    
  }

  public static String getConnectionPassword(String connectionName, String password) throws ConnectionServiceException {
    try {
      IDatabaseConnection datasource = datasourceMgmtSvc.getDatasourceByName(connectionName);
      if (datasource != null && !hasPasswordChanged(password)) {
        return datasource.getPassword();
      } else {
        return password;
      }
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceHelper.ERROR_0001_UNABLE_TO_GET_CONNECTION_PASSWORD", //$NON-NLS-1$
          connectionName, e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceHelper.ERROR_0001_UNABLE_TO_GET_CONNECTION_PASSWORD", connectionName, e.getLocalizedMessage()), e); //$NON-NLS-1$
    }
  }
  
  private static boolean hasPasswordChanged(String password) {
    if (password != null && password.length() > 0) {
      for (char character : password.toCharArray()) {
        if (character != PASSWORD_REPLACE_CHAR) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static String encodePassword(String password) {
    StringBuffer buffer;
    if (password != null && password.length() > 0) {
      buffer = new StringBuffer(password.length());
      for (int i = 0; i < password.length(); i++) {
        buffer.append(PASSWORD_REPLACE_CHAR);
      }
    } else {
      buffer = new StringBuffer();
    }
    return buffer.toString();
  }

}
