/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.codehaus.enunciate.Facet;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.ui.database.event.DefaultDatabaseDialectList;
import org.pentaho.ui.database.event.DefaultDatabaseTypesList;
import org.pentaho.ui.database.event.IDatabaseDialectList;
import org.pentaho.ui.database.event.IDatabaseTypesList;

@Path( "/data-access/api/dialect" )
public class DatabaseDialectService extends org.pentaho.database.service.DatabaseDialectService {

  public DatabaseDialectService() {
    this( true );
  }

  public DatabaseDialectService( boolean validateClasses ) {
    super( validateClasses );
  }

  /**
   * Register a dialect, if the register fails it will return a server error.
   *
   * @param databaseDialect IDatabaseDialect object to register
   *
   * @return Response determines if the dialect was registered or not.
   */
  @POST
  @Path( "/registerDatabaseDialect" )
  @Consumes( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response registerDatabaseDialectWS( IDatabaseDialect databaseDialect ) {
    return this.registerDatabaseDialectWS( databaseDialect, true );
  }

  /**
   * Register a dialect, if the register fails it will return a server error.
   *
   * @param databaseDialect IDatabaseDialect object to register
   * @param validateClassExists
   *
   * @return Response determines if the dialect was registered or not.
   */
  @POST
  @Path( "/registerDatabaseDialectWithValidation/{validateClassExists}" )
  @Consumes( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response registerDatabaseDialectWS( IDatabaseDialect databaseDialect,
      @PathParam( "validateClassExists" ) Boolean validateClassExists ) {
    try {
      super.registerDatabaseDialect( databaseDialect );
    } catch ( Throwable e ) {
      Response.serverError().entity( e ).build();
    }
    return Response.ok().build();
  }

  /**
   * Attempt to load the JDBC Driver class. If it's not available, return false.
   * 
   * @param classname
   *          validate that this classname exists in the classpath
   * 
   * @return true if the class exists
   */
  @POST
  @Path( "/validateJdbcDriverClassExists" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Boolean validateJdbcDriverClassExistsWS( String classname ) {
    return super.validateJdbcDriverClassExists( classname );
  }

  /**
   * Get a list of the database types
   *
   * @return IDatabaseTypesList containing the database types
   */
  @GET
  @Path( "/getDatabaseTypes" )
  @Facet ( name = "Unsupported" )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseTypesList getDatabaseTypesWS() {
    DefaultDatabaseTypesList value = new DefaultDatabaseTypesList();
    value.setDbTypes( super.getDatabaseTypes() );
    return value;
  }

  /**
   * Get the dialect of the given IDatabaseType
   *
   * @param databaseType IDatabaseType object to get the dialect of
   * @return IDatabaseDialect containing the dialect of databaseType
   */
  @POST
  @Path( "/getDialectByType" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public IDatabaseDialect getDialectWS( IDatabaseType databaseType ) {
    return super.getDialect( databaseType );
  }

  /**
   * Get the dialect of a given IDatabaseConnection
   *
   * @param connection IDatabaseConnection object to get the dialect of
   *
   * @return IDatabaseDialect of the given connection
   */
  @POST
  @Path( "/getDialectByConnection" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public IDatabaseDialect getDialect( IDatabaseConnection connection ) {
    return super.getDialect( connection );
  }

  /**
   * Get a list of the database dialects
   *
   * @return IDatabaseDialectList containing the database dialects
   */
  @GET
  @Path( "/getDatabaseDialects" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public IDatabaseDialectList getDatabaseDialectsWS() {
    IDatabaseDialectList value = new DefaultDatabaseDialectList();
    value.setDialects( super.getDatabaseDialects() );
    return value;
  }
}
