/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ConnectionServiceHelper {
  private static final Log logger = LogFactory.getLog( ConnectionServiceHelper.class );
  @VisibleForTesting
  protected static IDatasourceMgmtService datasourceMgmtSvc;
  private static char PASSWORD_REPLACE_CHAR = '*';

  static {
    IPentahoSession session = PentahoSessionHolder.getSession();
    datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class, session );
  }

  public static String getConnectionPassword( String connectionName, String password )
    throws ConnectionServiceException {
    try {
      IDatabaseConnection datasource = datasourceMgmtSvc.getDatasourceByName( connectionName );
      if ( datasource != null && !hasPasswordChanged( password ) ) {
        return datasource.getPassword();
      } else {
        return password;
      }
    } catch ( Exception e ) {
      logger.error(
        Messages.getErrorString( "ConnectionServiceHelper.ERROR_0001_UNABLE_TO_GET_CONNECTION_PASSWORD", //$NON-NLS-1$
          connectionName, e.getLocalizedMessage() ) );
      throw new ConnectionServiceException( Messages.getErrorString(
        "ConnectionServiceHelper.ERROR_0001_UNABLE_TO_GET_CONNECTION_PASSWORD", connectionName,
        e.getLocalizedMessage() ), e ); //$NON-NLS-1$
    }
  }

  private static boolean hasPasswordChanged( String password ) {
    if ( password != null ) {
      if ( password.length() < 1 ) { // empty password change
        return true;
      }
      for ( char character : password.toCharArray() ) {
        if ( character != PASSWORD_REPLACE_CHAR ) {
          return true;
        }
      }
    }
    return false;
  }

  public static String encodePassword( String password ) {
    StringBuffer buffer;
    if ( password != null && password.length() > 0 ) {
      buffer = new StringBuffer( password.length() );
      for ( int i = 0; i < password.length(); i++ ) {
        buffer.append( PASSWORD_REPLACE_CHAR );
      }
    } else {
      buffer = new StringBuffer();
    }
    return buffer.toString();
  }

}
