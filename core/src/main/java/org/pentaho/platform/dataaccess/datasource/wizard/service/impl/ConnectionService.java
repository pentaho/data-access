/*
 * ! ******************************************************************************
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

import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseConnectionPoolParameter;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseConnectionPoolParameter;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.api.ConnectionsApi;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.UtilHtmlSanitizer;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRuleException;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionPoolParameterList;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionPoolParameterList;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConnectionService implements ConnectionsApi {
  // IMPLEMENTATION NOTE: The exceptions thrown by the methods in this class 
  // are intentionally compatible with the legacy API, and are intentionally 
  // inconsistent to reach that goal. Be wary of changing the entity, 
  // content-type, or response code from the current behavior to something 
  // that looks more correct, as it may break compatibility with the legacy API.

  private static final Log logger = LogFactory.getLog( ConnectionService.class );
  protected static final String MEDIA_TYPE_JSON = "application/json";
  protected static final String MEDIA_TYPE_TEXT_PLAIN = "text/plain";

  private ConnectionServiceImpl connectionService;
  private DatabaseDialectService dialectService;
  GenericDatabaseDialect genericDialect = new GenericDatabaseDialect();
  protected UtilHtmlSanitizer sanitizer;

  public ConnectionService() {
    this( null, null, null );
  }

  public ConnectionService( ConnectionServiceImpl connectionService, DatabaseDialectService dialectService,
                            UtilHtmlSanitizer sanitizer ) {
    this.connectionService = connectionService != null ? connectionService : new ConnectionServiceImpl();
    this.dialectService = dialectService != null ? dialectService : new DatabaseDialectService( true );
    this.sanitizer = sanitizer != null ? sanitizer : UtilHtmlSanitizer.getInstance();
  }

  // Setters for test injection
  public void setConnectionService( ConnectionServiceImpl connectionService ) {
    this.connectionService = connectionService;
  }

  public void setDialectService( DatabaseDialectService dialectService ) {
    this.dialectService = dialectService;
  }

  public void setSanitizer( UtilHtmlSanitizer sanitizer ) {
    this.sanitizer = sanitizer;
  }

  /**
   * Helper method to handle exceptions thrown during REST API operations.
   * If the exception is an AuthorizationRuleException, throws a 403 Forbidden response.
   * For all other exceptions, throws a 500 Internal Server Error response.
   *
   * @param methodName The name of the method where the exception occurred
   * @param ex         The exception to handle
   * @throws WebApplicationException always throws with appropriate status code
   */
  private void handleException( String methodName, Exception ex ) {
    if ( ex instanceof AuthorizationRuleException ) {
      throw new WebApplicationException( Response.status( Response.Status.FORBIDDEN ).build() );
    } else {
      logger.error( "Unexpected error in " + methodName + ": " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Returns a response with id of a database connection
   *
   * @param name
   *          String representing the name of the database to search
   * @return String containing the id of the database connection if found, otherwise a 500 error
   *
   */
  @Override
  public String getConnectionIdByNameWithResponse( String name, String projectDir ) {
    try {
      IDatabaseConnection conn = connectionService.getConnectionByName( URLDecoder.decode( name,
        StandardCharsets.UTF_8 ) );
      if ( conn != null ) {
        return conn.getId();
      } else {
        throw new ConnectionServiceException( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
          Messages.getErrorString( "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", String.valueOf(
            name ) ) );
      }
    } catch ( ConnectionServiceException ex ) {
      int status = ex.getStatusCode();
      if ( status == Response.Status.NOT_FOUND.getStatusCode() || status == Response.Status.FORBIDDEN
        .getStatusCode() ) {
        status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
      }
      throw new WebApplicationException(
        Response.status( status )
          .entity( ex.getMessage() )
          .type( MEDIA_TYPE_JSON )
          .build() );
    } catch ( Exception ex ) {
      // no auth errors allowed, don't use handleException. 
      logger.error( "Unexpected error in getConnectionIdByNameWithResponse: " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  public String getConnectionIdByNameWithResponse( String name ) {
    return getConnectionIdByNameWithResponse( name, null );
  }

  /**
   * Returns validation messages for the given connection.
   * Performs parameter validation and returns validation messages wrapped in response object.
   * 
   * Throws WebApplicationException with 200 if connection has validation errors/warnings.
   * Throws WebApplicationException with 204 if connection is valid (no validation messages).
   *
   * @param databaseConnection
   *                           DatabaseConnection to validate
   * @return CheckParameters200Response with validation messages (never actually returns - always throws)
   * @throws WebApplicationException with status 200 if validation messages exist, 204 if valid
   */
  @Override
  public CheckParameters200Response checkParameters( IDatabaseConnection databaseConnection ) {
    String[] validationMessages = DatabaseUtil.convertToDatabaseMeta( databaseConnection ).checkParameters();

    if ( validationMessages != null && validationMessages.length > 0 ) {
      // Connection has validation errors/warnings - return 200 with wrapped messages
      CheckParameters200Response response = new CheckParameters200Response();
      response.setItems( Arrays.asList( validationMessages ) );

      throw new WebApplicationException(
        Response.status( Response.Status.OK )
          .entity( response )
          .type( MEDIA_TYPE_JSON )
          .build() );
    } else {
      // Connection is valid - return 204 No Content
      throw new WebApplicationException( Response.noContent().build() );
    }
  }

  /**
   * Create a database connection
   *
   * @param driver
   *               String name of the driver to use
   * @param url
   *               String name of the url used to create the connection.
   *
   * @return IDatabaseConnection for the given parameters
   */
  @Override
  public IDatabaseConnection createDatabaseConnection( String driver, String url, String projectDir ) {
    for ( IDatabaseDialect dialect : dialectService.getDatabaseDialects() ) {
      if ( dialect.getNativeDriver() != null && dialect.getNativeDriver().equals( driver ) ) {
        if ( dialect.getNativeJdbcPre() != null && url.startsWith( dialect.getNativeJdbcPre() ) ) {
          return dialect.createNativeConnection( url );
        }
      }
    }

    // if no native driver was found, create a custom dialect object.

    IDatabaseConnection conn = genericDialect.createNativeConnection( url );
    conn.getAttributes().put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS, driver );

    return conn;
  }

  public IDatabaseConnection createDatabaseConnection( String driver, String url ) {
    return createDatabaseConnection( driver, url, null );
  }

  /**
   * Returns a list of the database connection pool parameters
   *
   * @return IDatabaseConnectionPoolParameterList a list of the pooling parameters
   */
  @Override
  public IDatabaseConnectionPoolParameterList getPoolingParameters() {
    IDatabaseConnectionPoolParameterList value = new DefaultDatabaseConnectionPoolParameterList();
    List<IDatabaseConnectionPoolParameter> paramList = new ArrayList<IDatabaseConnectionPoolParameter>();
    for ( DatabaseConnectionPoolParameter param : poolingParameters ) {
      paramList.add( param );
    }
    value.setDatabaseConnectionPoolParameters( paramList );
    return value;
  }

  private static final DatabaseConnectionPoolParameter[] poolingParameters = new DatabaseConnectionPoolParameter[] {
    new DatabaseConnectionPoolParameter( "defaultAutoCommit", "true",
      Messages.getString( "ConnectionServiceImpl.INFO_0002_DEFAULT_AUTO_COMMIT" ) ),
    new DatabaseConnectionPoolParameter( "defaultReadOnly", null,
      Messages.getString( "ConnectionServiceImpl.INFO_0003_DEFAULT_READ_ONLY" ) ),
    new DatabaseConnectionPoolParameter( "defaultTransactionIsolation", null,
      Messages.getString( "ConnectionServiceImpl.INFO_0004_DEFAULT_TRANSACTION_ISOLATION" ) ),
    new DatabaseConnectionPoolParameter( "defaultCatalog", null,
      Messages.getString( "ConnectionServiceImpl.INFO_0005_DEFAULT_CATALOG" ) ),
    new DatabaseConnectionPoolParameter( "initialSize", "0",
      Messages.getString( "ConnectionServiceImpl.INFO_0006_INITAL_SIZE" ) ),
    new DatabaseConnectionPoolParameter( "maxActive", "8",
      Messages.getString( "ConnectionServiceImpl.INFO_0007_MAX_ACTIVE" ) ),
    new DatabaseConnectionPoolParameter( "maxIdle", "8",
      Messages.getString( "ConnectionServiceImpl.INFO_0008_MAX_IDLE" ) ),
    new DatabaseConnectionPoolParameter( "minIdle", "0",
      Messages.getString( "ConnectionServiceImpl.INFO_0009_MIN_IDLE" ) ),
    new DatabaseConnectionPoolParameter( "maxWait", "-1",
      Messages.getString( "ConnectionServiceImpl.INFO_0010_MAX_WAIT" ) ),
    new DatabaseConnectionPoolParameter( "validationQuery", null,
      Messages.getString( "ConnectionServiceImpl.INFO_0011_VALIDATION_QUERY" ) ),
    new DatabaseConnectionPoolParameter( "testOnBorrow", "true",
      Messages.getString( "ConnectionServiceImpl.INFO_0012_TEST_ON_BORROW" ) ),
    new DatabaseConnectionPoolParameter( "testOnReturn", "false",
      Messages.getString( "ConnectionServiceImpl.INFO_0013_TEST_ON_RETURN" ) ),
    new DatabaseConnectionPoolParameter( "testWhileIdle", "false",
      Messages.getString( "ConnectionServiceImpl.INFO_0014_TEST_WHILE_IDLE" ) ),
    new DatabaseConnectionPoolParameter( "timeBetweenEvictionRunsMillis", null,
      Messages.getString( "ConnectionServiceImpl.INFO_0015_TIME_BETWEEN_EVICTION_RUNS_MILLIS" ) ),
    new DatabaseConnectionPoolParameter( "poolPreparedStatements", "false",
      Messages.getString( "ConnectionServiceImpl.INFO_0016_POOL_PREPARED_STATEMENTS" ) ),
    new DatabaseConnectionPoolParameter( "maxOpenPreparedStatements", "-1",
      Messages.getString( "ConnectionServiceImpl.INFO_0017_MAX_OPEN_PREPARED_STATEMENTS" ) ),
    new DatabaseConnectionPoolParameter( "accessToUnderlyingConnectionAllowed", "false",
      Messages.getString( "ConnectionServiceImpl.INFO_0018_ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED" ) ),
    new DatabaseConnectionPoolParameter( "removeAbandoned", "false",
      Messages.getString( "ConnectionServiceImpl.INFO_0019_REMOVE_ABANDONED" ) ),
    new DatabaseConnectionPoolParameter( "removeAbandonedTimeout", "300",
      Messages.getString( "ConnectionServiceImpl.INFO_0020_REMOVE_ABANDONED_TIMEOUT" ) ),
    new DatabaseConnectionPoolParameter( "logAbandoned", "false",
      Messages.getString( "ConnectionServiceImpl.INFO_0021_LOGS_ABANDONED" ) ), };

  /**
   * Tests the database connection
   *
   * @param databaseConnection
   *                           Database connection object to test
   * @return String based on the boolean value of the connection test
   */
  @Override
  public String testConnection( IDatabaseConnection databaseConnection, String projectDir ) {
    try {
      applySavedPassword( databaseConnection, projectDir );
      boolean success = connectionService.testConnection( databaseConnection );
      if ( success ) {
        return Messages.getString( "ConnectionServiceImpl.INFO_0001_CONNECTION_SUCCEED", databaseConnection
          .getDatabaseName() );
      } else {
        String errorMsg = Messages.getErrorString( "ConnectionServiceImpl.ERROR_0009_CONNECTION_FAILED",
          databaseConnection.getDatabaseName() );
        throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR )
          .entity( errorMsg ).type( MEDIA_TYPE_TEXT_PLAIN ).build() );
      }
    } catch ( WebApplicationException e ) {
      throw e;
    } catch ( ConnectionServiceException ex ) {
      // for some reason this must be a plain 500 with no specific message. 
      throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build() );
    } catch ( Exception ex ) {
      logger.error( "Unexpected error in testConnection: " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build() );
    }
  }

  public String testConnection( IDatabaseConnection databaseConnection ) {
    return testConnection( databaseConnection, null );
  }

  /**
   * Update an existing database connection. Throws WebApplicationException with OK status on success.
   *
   * @param databaseConnection
   *          Database connection object to update
   * @param projectDir
   *          Optional project directory (used by subclasses)
   *
   */
  @Override
  public void updateConnection( IDatabaseConnection databaseConnection, String projectDir ) {
    sanitizer.sanitizeConnectionParameters( databaseConnection );
    try {
      applySavedPassword( databaseConnection );
      connectionService.updateConnection( databaseConnection );
      // explicitly return a 200 instead of 204 No Content
      throw new WebApplicationException( Response.ok().build() );
    } catch ( WebApplicationException e ) {
      throw e;
    } catch ( ConnectionServiceException cse ) {
      if ( cse.getStatusCode() == Response.Status.NOT_FOUND.getStatusCode() || cse.getStatusCode()
        == Response.Status.FORBIDDEN.getStatusCode() ) {
        throw new WebApplicationException(
          Response.status( Response.Status.INTERNAL_SERVER_ERROR )
            .build() );
      }
      // Preserve the exact status code from the exception (e.g., 403 for forbidden, 500 for server error)
      throw new WebApplicationException(
        Response.status( cse.getStatusCode() )
          .entity( cse.getMessage() )
          .type( MEDIA_TYPE_TEXT_PLAIN )
          .build() );
    } catch ( Exception ex ) {
      // no auth errors allowed, don't use handleException. 
      logger.error( "Unexpected error in updateConnection: " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Convenience method for backwards compatibility - delegates to two-parameter version with a null projectDir
   *
   * @param databaseConnection
   *          Database connection object to update
   */
  public void updateConnection( IDatabaseConnection databaseConnection ) {
    updateConnection( databaseConnection, null );
  }

  /**
   * If password is empty, that means connection sent from UI and user didn't change password. Since we cleaned password
   * during sending to UI, we need to use stored password.
   */
  protected void applySavedPassword( IDatabaseConnection conn, String projectDir ) throws ConnectionServiceException {
    applySavedPassword( conn );
  }

  private void applySavedPassword( IDatabaseConnection conn ) throws ConnectionServiceException {
    if ( StringUtils.isBlank( conn.getPassword() ) ) {
      IDatabaseConnection savedConn;
      if ( conn.getId() != null ) {
        savedConn = connectionService.getConnectionById( conn.getId() );
      } else {
        try {
          savedConn = connectionService.getConnectionByName( conn.getName() );
        } catch ( ConnectionServiceException e ) {
          logger.warn( e.getMessage() );
          savedConn = null;
        }
      }
      // The connection might not be in the database because this may be a new
      // hive connection that doesn't require a password
      if ( savedConn != null ) {
        conn.setPassword( savedConn.getPassword() );
      }
    }
  }

  /**
   * Delete an existing database connection. Throws WebApplicationException with OK status on success.
   *
   * @param databaseConnection
   *                           Database connection object to delete
   * @param projectDir
   *                           Optional project directory (used by subclasses)
   */
  @Override
  public void deleteConnection( IDatabaseConnection databaseConnection, String projectDir ) {
    try {
      connectionService.deleteConnection( databaseConnection );
      // explicitly return a 200 instead of 204 No Content
      throw new WebApplicationException( Response.ok().build() );
    } catch ( WebApplicationException e ) {
      throw e;
    } catch ( ConnectionServiceException cse ) {
      throw new WebApplicationException(
        Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build() );
    } catch ( Exception ex ) {
      // no auth errors allowed, don't use handleException. 
      logger.error( "Unexpected error in deleteConnection: " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Convenience method for backwards compatibility - delegates to two-parameter version with a null projectDir
   *
   * @param databaseConnection
   *          Database connection object to delete
   */
  public void deleteConnection( IDatabaseConnection databaseConnection ) {
    deleteConnection( databaseConnection, null );
  }

  /**
   * Delete an existing database connection by name
   *
   * @param name
   *          String representing the name of the database connection to delete
   *
   */
  @Override
  public void deleteConnectionByName( String name, String projectDir ) {
    try {
      String decodedName = URLDecoder.decode( name, StandardCharsets.UTF_8 );
      if ( StringUtils.isBlank( decodedName ) ) {
        // yes, 500. For consistency with the legacy API. 
        throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build() );
      }
      connectionService.deleteConnection( decodedName );
      // explicitly return a 200 instead of 204 No Content
      throw new WebApplicationException( Response.ok().build() );
    } catch ( WebApplicationException we ) {
      throw we;
    } catch ( ConnectionServiceException cse ) {
      if ( cse.getStatusCode() == Response.Status.FORBIDDEN.getStatusCode() ) {
        throw new WebApplicationException(
          Response.status( Response.Status.INTERNAL_SERVER_ERROR )
            .build() );
      }
      // Preserve the exact status code from the exception (e.g., 403 for forbidden, 500 for server error)
      throw new WebApplicationException(
        Response.status( cse.getStatusCode() )
          .build() );
    } catch ( Exception t ) {
      logger.error( "Unexpected error in deleteConnectionByName: " + t.getMessage(), t );
      // doesn't throw 403
      throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build() );
    }
  }

  public void deleteConnectionByName( String name ) {
    deleteConnectionByName( name, null );
  }

  /**
   * Add a database connection. Throws WebApplicationException with OK status on success.
   *
   * @param databaseConnection
   *          A database connection object to add
   * @param projectDir
   *          Optional project directory (used by subclasses)
   *
   */
  @Override
  public void addConnection( IDatabaseConnection databaseConnection, String projectDir ) {
    sanitizer.sanitizeConnectionParameters( databaseConnection );
    try {
      connectionService.addConnection( databaseConnection );
      // explicitly return a 200 instead of 204 No Content
      throw new WebApplicationException( Response.ok().build() );
    } catch ( WebApplicationException we ) {
      throw we;
    } catch ( ConnectionServiceException cse ) {
      // Preserve the exact status code from the exception (e.g., 403 for forbidden, 500 for server error)
      throw new WebApplicationException( Response.status( cse.getStatusCode() ).build() );
    } catch ( Exception t ) {
      handleException( "addConnection", t );
    }
  }

  /**
   * Convenience method for backwards compatibility - delegates to two-parameter version with a null projectDir
   *
   * @param databaseConnection
   *          A database connection object to add
   */
  public void addConnection( IDatabaseConnection databaseConnection ) {
    addConnection( databaseConnection, null );
  }

  /**
   * internal validation of authorization
   *
   * @throws PentahoAccessControlException
   */
  private void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    boolean isAdmin =
      policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) || policy.isAllowed( PublishAction.NAME ) );
    if ( !isAdmin ) {
      throw new PentahoAccessControlException( "Access Denied" );
    }
  }

  /**
   * Returns the list of database connections
   *
   * @return List of database connections
   *
   */
  @Override
  public IDatabaseConnectionList getConnections( String projectDir ) {
    try {
      IDatabaseConnectionList databaseConnections = new DefaultDatabaseConnectionList();
      List<IDatabaseConnection> conns = connectionService.getConnections( true );
      databaseConnections.setDatabaseConnections( conns );
      return databaseConnections;
    } catch ( ConnectionServiceException ex ) {
      throw new WebApplicationException( ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  public IDatabaseConnectionList getConnections() {
    return getConnections( null );
  }

  /**
   * Returns the list of database connections
   *
   * @param name
   *                   String representing the name of the database to return
   * @param mask
   *                   Whether to mask the password
   * @param projectDir
   *                   Optional project directory (used by subclasses)
   * @return Database connection by name
   */
  @Override
  public IDatabaseConnection getConnectionByName( String name, Boolean mask, String projectDir ) {
    if ( StringUtils.isBlank( name ) ) {
      // yes, 500. For consistency with the legacy API. 
      throw new WebApplicationException( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build() );
    }
    try {
      IDatabaseConnection conn = connectionService.getConnectionByName( URLDecoder.decode( name,
        StandardCharsets.UTF_8 ) );
      if ( mask ) {
        encryptPassword( conn );
      } else {
        hidePassword( conn );
      }
      return conn;
    } catch ( ConnectionServiceException ex ) {
      throw new WebApplicationException( ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( Exception ex ) {
      logger.error( "Error getting connection by name: " + ex.getMessage(), ex );
      throw new WebApplicationException( ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Convenience method for backwards compatibility - delegates to three-parameter version without a projectDir
   *
   * @param name
   *             String representing the name of the database to return
   * @param mask
   *             Whether to mask the password
   * @return Database connection by name
   */
  public IDatabaseConnection getConnectionByName( String name, Boolean mask ) {
    return getConnectionByName( name, mask, null );
  }

  /**
   * Returns the database connection details
   *
   * @param id
   *           String representing the name of the database to return
   * @return Database connection by name
   */
  @Override
  public IDatabaseConnection getConnectionById( String id, Boolean mask, String projectDir ) {
    try {
      IDatabaseConnection conn = connectionService.getConnectionById( id );
      if ( mask ) {
        encryptPassword( conn );
      } else {
        hidePassword( conn );
      }
      return conn;
    } catch ( ConnectionServiceException ex ) {
      throw new WebApplicationException( ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  public IDatabaseConnection getConnectionById( String id, Boolean mask ) {
    return getConnectionById( id, mask, null );
  }

  /**
   * Returns a response based on the existence of a database connection. Throws WebApplicationException with OK status
   * on success.
   *
   * @param name
   *             String representing the name of the database to check
   */
  @Override
  public void isConnectionExist( String name, String projectDir ) {
    try {
      boolean isExist = connectionService.isConnectionExist( URLDecoder.decode( name, StandardCharsets.UTF_8 ) );
      if ( isExist ) {
      // explicitly return a 200 instead of 204 No Content
        throw new WebApplicationException( Response.ok().build() );
      } else {
        throw new WebApplicationException( Response.Status.NOT_MODIFIED );
      }
    } catch ( WebApplicationException we ) {
      throw we;
    } catch ( ConnectionServiceException ex ) {
      throw new WebApplicationException( ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR );
    } catch ( Exception ex ) {
      // no auth errors allowed, don't use handleException. 
      logger.error( "Unexpected error in isConnectionExist: " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  public void isConnectionExist( String name ) {
    isConnectionExist( name, null );
  }

  /**
   * this is a method to return a response object with an error message use getEntity(Connection.class) and getStatus()
   * to determine success
   */
  @Override
  public IDatabaseConnection getConnectionByNameWithResponse( String name, String projectDir ) {
    try {
      IDatabaseConnection conn = connectionService.getConnectionByName( URLDecoder.decode( name,
        StandardCharsets.UTF_8 ) );
      sanitizer.unsanitizeConnectionParameters( conn );
      hidePassword( conn );
      return conn;
    } catch ( ConnectionServiceException ex ) {
      int status = ex.getStatusCode();
      String entity = null;
      if ( status == Response.Status.FORBIDDEN.getStatusCode() || status == Response.Status.NOT_FOUND
        .getStatusCode() ) {
        entity = ex.getMessage();
      }
      status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
      if ( entity != null ) {
        throw new WebApplicationException(
          Response.status( status )
            .entity( entity )
            .type( MEDIA_TYPE_JSON )
            .build() );
      } else {
        throw new WebApplicationException(
          Response.status( status )
            .build() );
      }
    } catch ( Exception ex ) {
      // no auth errors allowed, don't use handleException. 
      logger.error( "Unexpected error in getConnectionByNameWithResponse: " + ex.getMessage(), ex );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  public IDatabaseConnection getConnectionByNameWithResponse( String name ) {
    return getConnectionByNameWithResponse( name, null );
  }

  /**
   * Hides password for connections for return to user.
   */
  protected void hidePassword( IDatabaseConnection conn ) {
    conn.setPassword( null );
  }

  protected void encryptPassword( IDatabaseConnection conn ) {
    conn.setPassword( Encr.encryptPasswordIfNotUsingVariables( conn.getPassword() ) );
  }

}
