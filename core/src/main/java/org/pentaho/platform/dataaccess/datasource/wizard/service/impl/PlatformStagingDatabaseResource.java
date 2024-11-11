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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IStagingDatabase;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@Path( "/data-access/api/stagingDatabase" )
public class PlatformStagingDatabaseResource {

  IStagingDatabase stagingDatabase;

  public PlatformStagingDatabaseResource() {
    stagingDatabase = PentahoSystem.get( IStagingDatabase.class );
  }

  public PlatformStagingDatabaseResource( final IStagingDatabase stagingDatabase ) {
    this.stagingDatabase = stagingDatabase;
  }

  /**
   * Get the database metadata
   *
   * @return IDatabaseConnection containing the database meta
   */
  @GET
  @Path( "/databaseMetadata" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public IDatabaseConnection getDatabaseMetadata() {
    return stagingDatabase.getDatbaseMetadata();
  }

}
