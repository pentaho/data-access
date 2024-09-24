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

package org.pentaho.platform.dataaccess.datasource.wizard.service;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.ConnectionServiceHelper;

public class DataAccessDatabaseConnectionService extends DatabaseConnectionService {

  @Override
  public String testConnection( IDatabaseConnection connection ) {
    try {

      connection
        .setPassword( ConnectionServiceHelper.getConnectionPassword( connection.getName(), connection.getPassword() ) );
      return super.testConnection( connection );
    } catch ( ConnectionServiceException e ) {
      return super.testConnection( connection );
    }
  }
}
