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

import org.codehaus.enunciate.Facet;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.web.http.api.resources.StringListWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Rowell Belen
 */
@Path( "/data-access/api/permissions" )
@Facet ( name = "Unsupported" )
public class DataAccessPermissionResource {
  private SimpleDataAccessPermissionHandler dataAccessPermHandler;
  private SimpleDataAccessViewPermissionHandler dataAccessViewPermHandler;

  public DataAccessPermissionResource() {
    dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
  }

  /**
   * Return response based on the boolean value of hasDataAccess
   *
   * @return Response based on the boolean value of hasDataAccess
   */
  @GET
  @Path( "/hasDataAccess" )
  @Facet ( name = "Unsupported" )
  @Produces( { APPLICATION_JSON } )
  public Response hasDataAccessPermission() {
    return Response.ok( "" + ( dataAccessPermHandler != null
      && dataAccessPermHandler.hasDataAccessPermission( PentahoSessionHolder.getSession() ) ) ).build();
  }

  /**
   * Return response based on the boolean value of the data access view permission
   *
   * @return Response based on the boolean value of the data access view permission
   */
  @GET
  @Path( "/hasDataAccessView" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response hasDataAccessViewPermission() {
    return Response.ok( "" + ( dataAccessViewPermHandler != null
      && dataAccessViewPermHandler.hasDataAccessViewPermission( PentahoSessionHolder.getSession() ) ) ).build();
  }

  /**
   * Get the list of permitted roles
   *
   * @return StringListWrapper containing the permitted roles
   */
  @GET
  @Path( "/permittedRoles" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public StringListWrapper getPermittedRoleList() {
    if ( dataAccessViewPermHandler == null ) {
      return new StringListWrapper();
    }
    return new StringListWrapper( dataAccessViewPermHandler.getPermittedRoleList( PentahoSessionHolder.getSession() ) );
  }

  /**
   * Get the list of permitted users
   *
   * @return StringListWrapper containing the permitted users
   */
  @GET
  @Path( "/permittedUsers" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public StringListWrapper getPermittedUserList() {
    if ( dataAccessViewPermHandler == null ) {
      return new StringListWrapper();
    }
    return new StringListWrapper( dataAccessViewPermHandler.getPermittedUserList( PentahoSessionHolder.getSession() ) );
  }
}
