package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

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
@Path("/data-access/api/permissions")
public class DataAccessPermissionResource
{
  private SimpleDataAccessPermissionHandler dataAccessPermHandler;
  private SimpleDataAccessViewPermissionHandler dataAccessViewPermHandler;

  public DataAccessPermissionResource(){
    dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
  }

  @GET
  @Path("/hasDataAccess")
  @Produces( {APPLICATION_JSON })
  public Response hasDataAccessPermission() {
    return Response.ok("" + (dataAccessPermHandler != null
       && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession()))).build();
  }

  @GET
  @Path("/hasDataAccessView")
  @Produces( {APPLICATION_JSON })
  public Response hasDataAccessViewPermission() {
    return Response.ok("" + (dataAccessViewPermHandler != null
       && dataAccessViewPermHandler.hasDataAccessViewPermission(PentahoSessionHolder.getSession()))).build();
  }

  @GET
  @Path("/permittedRoles")
  @Produces( {APPLICATION_JSON })
  public StringListWrapper getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      return new StringListWrapper();
    }
    return new StringListWrapper(dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession()));
  }

  @GET
  @Path("/permittedUsers")
  @Produces( {APPLICATION_JSON })
  public StringListWrapper getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      return new StringListWrapper();
    }
    return new StringListWrapper(dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession()));
  }
}
