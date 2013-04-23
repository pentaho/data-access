package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

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
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public Boolean hasDataAccessPermission() {
    return dataAccessPermHandler != null
       && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }

  @GET
  @Path("/hasDataAccessView")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public Boolean hasDataAccessViewPermission() {
    return dataAccessViewPermHandler != null
       && dataAccessViewPermHandler.hasDataAccessViewPermission(PentahoSessionHolder.getSession());
  }

  @GET
  @Path("/permittedRoles")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return new JaxbList<String>(dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession()));
  }

  @GET
  @Path("/permittedUsers")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return new JaxbList<String>(dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession()));
  }
}
