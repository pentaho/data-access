package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.dataaccess.datasource.IStagingDatabase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.DatabaseHelper;


@Path("/data-access/api/stagingDatabase")
public class PlatformStagingDatabaseResource {

  IStagingDatabase stagingDatabase;
  
  public PlatformStagingDatabaseResource() {
    stagingDatabase = PentahoSystem.get( IStagingDatabase.class );
  }

  public PlatformStagingDatabaseResource(final IStagingDatabase stagingDatabase) {
    this.stagingDatabase = stagingDatabase;
  }
  
  @GET
  @Path("/databaseMeta")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public IDatabaseConnection getDatabaseMeta() {
    DatabaseMeta meta = stagingDatabase.getDatbaseMeta();
    DatabaseHelper helper = new DatabaseHelper(PentahoSystem.get( IDatabaseDialectService.class ));
    return helper.databaseMetaToDatabaseConnection( meta );
  }
  
}
