/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.dataaccess.datasource;

import java.sql.Connection;

import org.pentaho.database.model.IDatabaseConnection;

public interface IStagingDatabase {
  String getName();

  Connection getConnection() throws Exception;

  IDatabaseConnection getDatbaseMetadata();
}
