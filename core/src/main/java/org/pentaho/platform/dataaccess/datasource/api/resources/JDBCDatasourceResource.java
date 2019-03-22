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
 * Copyright (c) 2002-2019 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceImpl;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import java.util.List;

/**
 * This service provides methods for listing, creating, downloading, uploading, and removal of JDBC data sources.
 */
@Path( "/data-access/api/datasource/jdbc/connection" )
public class JDBCDatasourceResource {

  private ConnectionServiceImpl service;
  private static final Log logger = LogFactory.getLog( JDBCDatasourceResource.class );

  public JDBCDatasourceResource() {
    service = new ConnectionServiceImpl();
  }

  /**
   * Remove the JDBC data source for a given JDBC ID.
   *
   * <p><b>Example Request:</b><br />
   *    DELETE pentaho/plugin/data-access/api/datasource/jdbc/connection/TestDataSourceResource
   * </p>
   *
   * @param name The name of the JDBC datasource to remove
   *
   * @return A 204 response code representing the successful removal of the JDBC datasource.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      This response does not contain data.
   *    </pre>
   */
  @DELETE
  @Path( "/{name : .+}" )
  @StatusCodes( {
    @ResponseCode( code = 204, condition = "JDBC datasource removed successfully." ),
    @ResponseCode( code = 304,
      condition = "User is not authorized to remove the JDBC datasource or the connection does not exist." ),
    @ResponseCode( code = 500, condition = "An unexpected error occurred while deleting the JDBC datasource." )
  } )
  public Response deleteConnection( @PathParam( "name" ) String name ) {
    try {
      return service.deleteConnection( name ) ? Response.noContent().build() : Response.notModified().build();
    } catch ( Throwable t ) {
      return Response.serverError().build();
    }
  }

  /**
   * Get a list of JDBC datasource IDs.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/jdbc/connection
   * </p>
   *
   * @return A list of JDBC datasource IDs.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      {
   *        "Item": [
   *          {
   *            "@type": "xs:string",
   *            "$": "AgileBI"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "Audit"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "SampleData"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "TestDataSourceResource"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "baseball connection"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "baseball connection"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "live_logging_info"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "pentaho_operations_mart"
   *          }
   *        ]
   *      }
   *    </pre>
   */
  @GET
  @Path( "/" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully retrieved the list of JDBC datasource IDs" ),
    @ResponseCode( code = 500, condition = "Internal error retrieving JDBC datasource IDs" )
  } )
  public JaxbList<String> getConnectionIDs() {
    List<String> connectionNames;

    try {
      connectionNames = service.getConnections().stream()
        .map( connection -> connection.getName() )
        .collect( toList() );
    } catch ( ConnectionServiceException e ) {
      logger.error( "Error " + e.getMessage() );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
    return new JaxbList<>( connectionNames );
  }

  /**
   * Export a JDBC datasource connection.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/jdbc/connection/TestDataSourceResource
   * </p>
   *
   * @param name The name of the JDBC datasource to retrieve
   *
   * @return A Response object containing the JDBC connection in XML or JSON form.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      {
   *        "SQLServerInstance": null,
   *        "accessType": "NATIVE",
   *        "accessTypeValue": "NATIVE",
   *        "attributes": {
   *          "PORT_NUMBER": "9001"
   *        },
   *        "changed": false,
   *        "connectSql": "",
   *        "connectionPoolingProperties": {},
   *        "dataTablespace": "",
   *        "databaseName": "SampleData",
   *        "databasePort": "9001",
   *        "databaseType": {
   *          "defaultDatabasePort": 9001,
   *          "extraOptionsHelpUrl": "http://hsqldb.sourceforge.net/doc/guide/ch04.html#N109DA",
   *          "name": "Hypersonic",
   *          "shortName": "HYPERSONIC"
   *        },
   *        "extraOptions": {},
   *        "forcingIdentifiersToLowerCase": false,
   *        "forcingIdentifiersToUpperCase": false,
   *        "hostname": "localhost",
   *        "id": "00ac4db3-7567-4019-8917-1b6f512ee162",
   *        "indexTablespace": "",
   *        "informixServername": "",
   *        "warehouse": "",
   *        "initialPoolSize": 0,
   *        "maximumPoolSize": 0,
   *        "name": "TestDataSourceResource",
   *        "partitioned": false,
   *        "password": "password",
   *        "quoteAllFields": false,
   *        "streamingResults": false,
   *        "username": "pentaho_user",
   *        "usingConnectionPool": true,
   *        "usingDoubleDecimalAsSchemaTableSeparator": false
   *        }
   *    </pre>
   */
  @GET
  @Path( "/{name : .+}" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully retrieved the JDBC datasource" ),
    @ResponseCode( code = 500, condition = "An error occurred retrieving the JDBC datasource" )
  } )
  public Response getConnection( @PathParam( "name" ) String name ) {
    try {
      IDatabaseConnection connection = service.getConnectionByName( name );
      connection.setPassword( null ); // don't return the password back to the user

      return Response.ok( connection ).build();
    } catch ( ConnectionServiceException e ) {
      logger.error( "Error " + e.getMessage() );
      return Response.serverError().build();
    }
  }

  /**
   * Add or update a JDBC datasource connection
   *
   * <p><b>Example Request:</b><br />
   *    PUT pentaho/plugin/data-access/api/datasource/jdbc/connection/TestDatasource
   * </p>
   * <br /><b>POST data:</b>
   *  <pre function="syntax.xml">
   *    {
   *      "changed": true,
   *      "usingConnectionPool": true,
   *      "connectSql": "",
   *      "databaseName": "SampleData",
   *      "databasePort": "9001",
   *      "hostname": "localhost",
   *      "name": "TestDataSourceResource",
   *      "password": "password",
   *      "username": "pentaho_user",
   *      "attributes": {},
   *      "connectionPoolingProperties": {},
   *      "extraOptions": {},
   *      "accessType": "NATIVE",
   *      "databaseType": {
   *        "defaultDatabasePort": 9001,
   *        "extraOptionsHelpUrl": "http://hsqldb.sourceforge.net/doc/guide/ch04.html#N109DA",
   *        "name": "Hypersonic",
   *        "shortName": "HYPERSONIC",
   *        "supportedAccessTypes": [
   *          "NATIVE",
   *          "ODBC",
   *          "JNDI"
   *        ]
   *      }
   *    }
   *  </pre>
   * </p>
   *
   * @param connection A DatabaseConnection in JSON representation
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      This response does not contain data.
   *    </pre>
   */
  @PUT
  @Path( "/{connectionId : .+}" )
  @Consumes( { APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = 204, condition = "JDBC datasource added successfully." ),
    @ResponseCode( code = 401, condition = "User is not authorized to add JDBC datasources." ),
    @ResponseCode( code = 304, condition = "Datasource was not modified" ),
    @ResponseCode( code = 500, condition = "An unexpected error occurred while adding the JDBC datasource." )
  } )
  public Response addOrUpdate( @PathParam( "connectionId" ) String connectionName, DatabaseConnection connection ) {
    try {
      DatasourceService.validateAccess();
      // Prefer the path name over the one in the DTO object
      connection.setId( connectionName );

      try {
        IDatabaseConnection old = service.getConnectionByName( connectionName );

        if ( StringUtils.isBlank( connection.getPassword() ) ) {
          connection.setPassword( old.getPassword() );
        }
        connection.setId( old.getId() );

        return service.updateConnection( connection ) ? Response.noContent().build() : Response.notModified().build();
      } catch ( ConnectionServiceException | NullPointerException e ) {
        // unfortunately getConnectionById throws an exception not returning null when the connection is not present.
      }

      return service.addConnection( connection ) ? Response.noContent().build() : Response.notModified().build();
    } catch ( PentahoAccessControlException t ) {
      return Response.status( Response.Status.UNAUTHORIZED ).build();
    } catch ( Throwable t ) {
      logger.error( "Error " + t.getMessage() );
      return Response.serverError().build();
    }
  }
}
