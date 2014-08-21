/*!
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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionService;
import org.pentaho.ui.database.event.IDatabaseConnectionList;

@Path( "/data-access/api/jdbc" )
public class JDBCDatasourceResource {

  private ConnectionService service;

  public JDBCDatasourceResource() {
    service = new ConnectionService();
  }

  /**
   * Delete an existing database connection by name
   * 
   * @param name
   *          String representing the name of the database connection to delete
   * @return Response indicating the success of this operation
   *
   * @throws ConnectionServiceException
   */
  @DELETE
  @Path( "/delete" )
  public Response deleteConnectionByName( @QueryParam( "name" ) String name ) throws ConnectionServiceException {
    return service.deleteConnectionByName( name );
  }

  /**
   * Returns the list of database connections
   *
   * @return List of database connections
   *
   * @throws ConnectionServiceException
   */
  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseConnectionList getConnections() throws ConnectionServiceException {
    return service.getConnections();
  }

  /**
   * Returns the list of database connections
   *
   * @param name
   *          String representing the name of the database to return
   * @return Database connection by name
   *
   * @throws ConnectionServiceException
   */
  @GET
  @Path( "/get" )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseConnection getConnectionByName( @QueryParam( "name" ) String name ) throws ConnectionServiceException {
    return service.getConnectionByName( name );
  }

  /**
   * Create a database connection
   *
   * @param driver
   *          String name of the driver to use
   * @param url
   *          String name of the url used to create the connection.
   *
   * @return IDatabaseConnection for the given parameters
   */
  @GET
  @Path( "/create" )
  @Produces( { APPLICATION_JSON } )
  public IDatabaseConnection createDatabaseConnection( @QueryParam( "driver" ) String driver,
      @QueryParam( "url" ) String url ) {
    return service.createDatabaseConnection( driver, url );
  }

  /**
   * Update an existing database connection
   *
   * @param connection
   *          Database connection object to update
   * @return Response indicating the success of this operation
   *
   * @throws ConnectionServiceException
   */
  @POST
  @Path( "/update" )
  @Consumes( { APPLICATION_JSON } )
  public Response updateConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    return service.updateConnection( connection );
  }
}
