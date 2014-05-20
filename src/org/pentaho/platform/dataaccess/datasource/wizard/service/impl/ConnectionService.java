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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseConnectionPoolParameter;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseConnectionPoolParameter;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionPoolParameterList;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionPoolParameterList;

@Path("/data-access/api/connection")
public class ConnectionService {
  private ConnectionServiceImpl service;
  private DatabaseDialectService dialectService;
  GenericDatabaseDialect genericDialect = new GenericDatabaseDialect();
  
  public ConnectionService() {
    service = new ConnectionServiceImpl();
    this.dialectService = new DatabaseDialectService(true);
  }

  @GET
  @Path("/list")
  @Produces({APPLICATION_JSON})
  public IDatabaseConnectionList getConnections() throws ConnectionServiceException {
    IDatabaseConnectionList databaseConnections = new DefaultDatabaseConnectionList();
    databaseConnections.setDatabaseConnections(service.getConnections());
    return databaseConnections;
  }

  @GET
  @Path("/get")
  @Produces({APPLICATION_JSON})
  public IDatabaseConnection getConnectionByName(@QueryParam("name") String name) throws ConnectionServiceException {
    return service.getConnectionByName(name);
  }

  @GET
  @Path("/checkexists")
  @Produces({APPLICATION_JSON})
  public Response isConnectionExist(@QueryParam("name") String name) throws ConnectionServiceException {
    boolean exists = service.isConnectionExist(name);
    try {
      if (exists) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  /**
   * this is a method to return a response object with an error message
   * use getEntity(Connection.class) and getStatus() to determine success
   */
  @GET
  @Path("/getresponse")
  @Produces({APPLICATION_JSON})
  public Response getConnectionByNameWithResponse(@QueryParam("name") String name) throws ConnectionServiceException {
    IDatabaseConnection conn = null;
    Response response;
    try{
     conn =service.getConnectionByName(name);
     response = Response.ok().entity(conn).build();
    }catch(Exception ex){
      response =  Response.serverError().entity(ex.getMessage()).build();
    }    
     return response;
  }

  @POST
  @Path("/add")
  @Consumes({APPLICATION_JSON})
  public Response addConnection(DatabaseConnection connection) throws ConnectionServiceException {
    try {
      validateAccess();
      boolean success = service.addConnection(connection);
      if (success) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/update")
  @Consumes({APPLICATION_JSON})
  public Response updateConnection(DatabaseConnection connection) throws ConnectionServiceException {
    try {
      boolean success = service.updateConnection(connection);
      if (success) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/delete")
  @Consumes({APPLICATION_JSON})
  public Response deleteConnection(DatabaseConnection connection) throws ConnectionServiceException {
    try {
      boolean success = service.deleteConnection(connection);
      if (success) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/deletebyname")
  public Response deleteConnectionByName(@QueryParam("name")String name) throws ConnectionServiceException {
    try {
      boolean success = service.deleteConnection(name);
      if (success) {
      return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/test")
  @Consumes({APPLICATION_JSON})
  @Produces({TEXT_PLAIN})
  public Response testConnection(DatabaseConnection connection) throws ConnectionServiceException {
    boolean success = false;
    success = service.testConnection( connection );
    if (success) { 
      return Response.ok(Messages.getString("ConnectionServiceImpl.INFO_0001_CONNECTION_SUCCEED"
          , connection.getDatabaseName())).build();
    } else {
      return Response.serverError().entity(Messages.getErrorString("ConnectionServiceImpl.ERROR_0009_CONNECTION_FAILED"
          , connection.getDatabaseName())).build();  
    }
  }
    
  private static final DatabaseConnectionPoolParameter[] poolingParameters = new DatabaseConnectionPoolParameter[] {
    new DatabaseConnectionPoolParameter("defaultAutoCommit", "true", "The default auto-commit state of connections created by this pool."), 
    new DatabaseConnectionPoolParameter("defaultReadOnly", null, "The default read-only state of connections created by this pool.\nIf not set then the setReadOnly method will not be called.\n (Some drivers don't support read only mode, ex: Informix)"), 
    new DatabaseConnectionPoolParameter("defaultTransactionIsolation", null, "the default TransactionIsolation state of connections created by this pool. One of the following: (see javadoc)\n\n  * NONE\n  * READ_COMMITTED\n  * READ_UNCOMMITTED\n  * REPEATABLE_READ  * SERIALIZABLE\n"), 
    new DatabaseConnectionPoolParameter("defaultCatalog", null, "The default catalog of connections created by this pool."),
    
    new DatabaseConnectionPoolParameter("initialSize", "0", "The initial number of connections that are created when the pool is started."), 
    new DatabaseConnectionPoolParameter("maxActive", "8", "The maximum number of active connections that can be allocated from this pool at the same time, or non-positive for no limit."), 
    new DatabaseConnectionPoolParameter("maxIdle", "8", "The maximum number of connections that can remain idle in the pool, without extra ones being released, or negative for no limit."), 
    new DatabaseConnectionPoolParameter("minIdle", "0", "The minimum number of connections that can remain idle in the pool, without extra ones being created, or zero to create none."), 
    new DatabaseConnectionPoolParameter("maxWait", "-1", "The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception, or -1 to wait indefinitely."),
    
    new DatabaseConnectionPoolParameter("validationQuery", null, "The SQL query that will be used to validate connections from this pool before returning them to the caller.\nIf specified, this query MUST be an SQL SELECT statement that returns at least one row."), 
    new DatabaseConnectionPoolParameter("testOnBorrow", "true", "The indication of whether objects will be validated before being borrowed from the pool.\nIf the object fails to validate, it will be dropped from the pool, and we will attempt to borrow another.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string."), 
    new DatabaseConnectionPoolParameter("testOnReturn", "false", "The indication of whether objects will be validated before being returned to the pool.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string."), 
    new DatabaseConnectionPoolParameter("testWhileIdle", "false", "The indication of whether objects will be validated by the idle object evictor (if any). If an object fails to validate, it will be dropped from the pool.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string."), 
    new DatabaseConnectionPoolParameter("timeBetweenEvictionRunsMillis", null, "The number of milliseconds to sleep between runs of the idle object evictor thread. When non-positive, no idle object evictor thread will be run."),
    
    new DatabaseConnectionPoolParameter("poolPreparedStatements", "false", "Enable prepared statement pooling for this pool."), 
    new DatabaseConnectionPoolParameter("maxOpenPreparedStatements", "-1", "The maximum number of open statements that can be allocated from the statement pool at the same time, or zero for no limit."), 
    new DatabaseConnectionPoolParameter("accessToUnderlyingConnectionAllowed", "false", "Controls if the PoolGuard allows access to the underlying connection."), 
    new DatabaseConnectionPoolParameter("removeAbandoned", "false", "Flag to remove abandoned connections if they exceed the removeAbandonedTimout.\nIf set to true a connection is considered abandoned and eligible for removal if it has been idle longer than the removeAbandonedTimeout. Setting this to true can recover db connections from poorly written applications which fail to close a connection."), 
    new DatabaseConnectionPoolParameter("removeAbandonedTimeout", "300", "Timeout in seconds before an abandoned connection can be removed."), 
    new DatabaseConnectionPoolParameter("logAbandoned", "false", "Flag to log stack traces for application code which abandoned a Statement or Connection.\nLogging of abandoned Statements and Connections adds overhead for every Connection open or new Statement because a stack trace has to be generated."), 
  };
 
  @GET
  @Path("/poolingParameters")
  @Produces({APPLICATION_JSON})
  public IDatabaseConnectionPoolParameterList getPoolingParameters() {
    IDatabaseConnectionPoolParameterList value = new DefaultDatabaseConnectionPoolParameterList();
    List<IDatabaseConnectionPoolParameter> paramList = new ArrayList<IDatabaseConnectionPoolParameter>();
    for(DatabaseConnectionPoolParameter param : poolingParameters) {
      paramList.add(param);
    }
    value.setDatabaseConnectionPoolParameters(paramList);
    return value; 
  }

  @GET
  @Path("/createDatabaseConnection")
  @Produces({APPLICATION_JSON})
  public IDatabaseConnection createDatabaseConnection(@QueryParam("driver") String driver, @QueryParam("url") String url) {
    for (IDatabaseDialect dialect : dialectService.getDatabaseDialects()) {
      if (dialect.getNativeDriver() != null && 
          dialect.getNativeDriver().equals(driver)) {
        if (dialect.getNativeJdbcPre() != null && url.startsWith(dialect.getNativeJdbcPre())) {
          return dialect.createNativeConnection(url);
        }
      }
    }
    
    // if no native driver was found, create a custom dialect object.
    
    IDatabaseConnection conn = genericDialect.createNativeConnection(url);
    conn.getAttributes().put(GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS, driver);
    
    return conn;    
  }

  @POST
  @Path("/checkParams")
  @Consumes({APPLICATION_JSON})
  @Produces({APPLICATION_JSON})
  public StringArrayWrapper checkParameters(DatabaseConnection connection) {
    StringArrayWrapper array = null;
    String[] rawValues = DatabaseUtil.convertToDatabaseMeta(connection).checkParameters();
    if (rawValues.length > 0) {
      array = new StringArrayWrapper();
      array.setArray(rawValues);
    }
    return array;
  }
  
  /**
   * internal validation of authorization
   * @throws PentahoAccessControlException
   */
  private void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);
    boolean isAdmin = policy.isAllowed(RepositoryReadAction.NAME)
        && policy.isAllowed(RepositoryCreateAction.NAME)
        && (policy.isAllowed(AdministerSecurityAction.NAME)
        || policy.isAllowed(PublishAction.NAME));
    if (!isAdmin) {
      throw new PentahoAccessControlException("Access Denied");
    }
  }
}
