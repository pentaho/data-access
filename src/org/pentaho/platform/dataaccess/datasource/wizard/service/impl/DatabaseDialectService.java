/**
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    super(validateClasses);
  }

  @POST
  @Path( "/registerDatabaseDialect" )
  @Consumes( { APPLICATION_JSON } )
  public Response registerDatabaseDialectWS( IDatabaseDialect databaseDialect ) {
    return this.registerDatabaseDialectWS( databaseDialect, true );
  }

  /**
   * 
   * @param databaseDialect
   * @param validateClassExists
   */
  @POST
  @Path( "/registerDatabaseDialectWithValidation/{validateClassExists}" )
  @Consumes( { APPLICATION_JSON } )
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
  public Boolean validateJdbcDriverClassExistsWS( String classname ) {
    return super.validateJdbcDriverClassExists(  classname );
  }

  @GET
  @Path( "/getDatabaseTypes" )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseTypesList getDatabaseTypesWS() {
    DefaultDatabaseTypesList value = new DefaultDatabaseTypesList();
    value.setDbTypes( super.getDatabaseTypes() );
    return value;
  }

  @POST
  @Path( "/getDialectByType" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseDialect getDialectWS( IDatabaseType databaseType ) {
    return super.getDialect( databaseType );
  }

  @POST
  @Path( "/getDialectByConnection" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseDialect getDialect( IDatabaseConnection connection ) {
    return super.getDialect( connection );
  }

  @GET
  @Path( "/getDatabaseDialects" )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseDialectList getDatabaseDialectsWS() {
    IDatabaseDialectList value = new DefaultDatabaseDialectList();
    value.setDialects( super.getDatabaseDialects() );
    return value;
  }
}
