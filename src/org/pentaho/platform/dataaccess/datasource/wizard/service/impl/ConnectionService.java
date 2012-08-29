package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

@Path("/data-access/api/connection")
public class ConnectionService {

  private ConnectionServiceImpl service = new ConnectionServiceImpl();

  public ConnectionService() {
    service = new ConnectionServiceImpl();
  }

  @GET
  @Path("/list")
  @Produces({APPLICATION_JSON})
  public Connection[] getConnections() throws ConnectionServiceException {
    return service.getConnections().toArray(new Connection[]{});
  }

  @GET
  @Path("/get/{name}")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  public Connection getConnectionByName(@PathParam("name") String name) throws ConnectionServiceException {
    return service.getConnectionByName(name);
  }

  @POST
  @Path("/add")
  @Consumes({APPLICATION_JSON})
  public Response addConnection(Connection connection) throws ConnectionServiceException {
    try {
      boolean success = service.addConnection(connection);
      if (success) {
        return Response.ok().build();
      } else {
        return Response.serverError().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/update")
  @Consumes({APPLICATION_JSON})
  public Response updateConnection(Connection connection) throws ConnectionServiceException {
    try {
      boolean success = service.updateConnection(connection);
      if (success) {
        return Response.ok().build();
      } else {
        return Response.serverError().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/delete")
  @Consumes({APPLICATION_JSON})
  public Response deleteConnection(Connection connection) throws ConnectionServiceException {
    try {
      boolean success = service.deleteConnection(connection);
      if (success) {
        return Response.ok().build();
      } else {
        return Response.serverError().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/deletebyname")
  @Consumes({TEXT_PLAIN})
  public Response deleteConnectionByName(String name) throws ConnectionServiceException {
    try {
      boolean success = service.deleteConnection(name);
      if (success) {
      return Response.ok().build();
      } else {
        return Response.serverError().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/test")
  @Consumes({APPLICATION_JSON})
  public Response testConnection(Connection connection) throws ConnectionServiceException {
    try {
      boolean success = service.testConnection(connection);
      if (success) {
      return Response.ok().build();
      } else {
        return Response.serverError().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }
 
  @POST
  @Path("/convertFromConnection")
  @Consumes({APPLICATION_JSON})
  public DatabaseConnection convertFromConnection(Connection connection) throws ConnectionServiceException {
    return (DatabaseConnection) service.convertFromConnection(connection);
  }

  @POST
  @Path("/convertToConnection")
  @Consumes({APPLICATION_JSON})
  public Connection convertToConnection(DatabaseConnection dbConnection) throws ConnectionServiceException {
    return service.convertToConnection(dbConnection);
  }

}
