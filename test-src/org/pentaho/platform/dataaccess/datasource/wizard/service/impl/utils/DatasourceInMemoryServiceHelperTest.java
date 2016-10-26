/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class DatasourceInMemoryServiceHelperTest {

  private static final String SAMPLE_VALID_QUERY = "validQuery";

  private static final String SAMPLE_INVALID_QUERY = "invalidQuery";

  private static final String CSV_FILE = "test-res/org/pentaho/platform/dataaccess/datasource/wizard/service/impl/utils/DatasourceInMemoryServiceHelperTest.csv";

  private static final String GENERIC_CONN_DRIVER_CLASS = "genericConnectionDriverClass";

  private static final String GENERIC_CONN_WITHOUT_DRIVER_CLASS = "genericConnectionWithoutDriverClass";

  private static final String GENERIC_CONN_NOT_FOUND_CLASS = "genericConnectionNotFoundClass";

  private static final String GENERIC_CONN_NOT_DRIVER_CLASS = "genericConnectionNotDriverClass";

  private static final String GENERIC_CONN_PRIVATE_DRIVER = "genericConnectionPrivateDriver";

  private static final String CONN_CAN_NOT_PREPARED = "connectionCanNotPrepared";

  private static final String CONN_THROW_EXCEPTION = "connectionThorwException";

  private IPentahoObjectFactory pentahoObjectFactory;

  private IPluginResourceLoader resLoader = mock( IPluginResourceLoader.class );

  private static Connection sqlConnection;

  private static Statement statment = mock( Statement.class );

  private static ResultSet resultSet = mock( ResultSet.class );

  private static ResultSetMetaData rsMetaData  = mock( ResultSetMetaData.class );

  private IDatabaseConnection genericConnWithClass = mock( IDatabaseConnection.class );

  private IDatabaseConnection genericConnWithoutClass = mock( IDatabaseConnection.class );

  private IDatabaseConnection genericConnWithNoExistClass = mock( IDatabaseConnection.class );

  private IDatabaseConnection genericConnWithNoDriverClass = mock( IDatabaseConnection.class );

  private IDatabaseConnection genericConnPrivateDriver = mock( IDatabaseConnection.class );

  private final IDatasourceMgmtService service = mock( IDatasourceMgmtService.class );

  private IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );

  private DatabaseType genericDBType = new DatabaseType(
      "TestDB",
      "GENERIC",
      Arrays.asList( DatabaseAccessType.NATIVE ),
      1000,
      "" );

  @Before
  public void setUp() throws Exception {
    sqlConnection = mock( Connection.class );

    when( resultSet.getMetaData() ).thenReturn( rsMetaData );
    when( statment.executeQuery( eq( SAMPLE_VALID_QUERY ) ) ).thenReturn( resultSet );
    when( sqlConnection.createStatement( anyInt(), anyInt() ) ).thenReturn( statment );

    Map<String, String> attributesPrivateDriver = new HashMap<>();
    attributesPrivateDriver.put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS,  PrivateSQLDriver.class.getName() );
    when( genericConnPrivateDriver.getAttributes() ).thenReturn( attributesPrivateDriver );
    when( genericConnPrivateDriver.getDatabaseType() ).thenReturn( genericDBType );

    Map<String, String> attributesWithNoExistClass = new HashMap<>();
    attributesWithNoExistClass.put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS,  "org.test.NoExistDriver" );
    when( genericConnWithNoExistClass.getAttributes() ).thenReturn( attributesWithNoExistClass );
    when( genericConnWithNoExistClass.getDatabaseType() ).thenReturn( genericDBType );

    Map<String, String> attributesNonDriverClass = new HashMap<>();
    attributesNonDriverClass.put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS,  String.class.getName() );
    when( genericConnWithNoDriverClass.getAttributes() ).thenReturn( attributesNonDriverClass );
    when( genericConnWithNoDriverClass.getDatabaseType() ).thenReturn( genericDBType );

    Map<String, String> attributesWithoutClass = new HashMap<>();
    attributesWithoutClass.put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS,  "" );
    when( genericConnWithoutClass.getAttributes() ).thenReturn( attributesWithoutClass );
    when( genericConnWithoutClass.getDatabaseType() ).thenReturn( genericDBType );

    Map<String, String> attributesWithClass = new HashMap<>();
    attributesWithClass.put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS,  TestSQLDriver.class.getName() );
    when( genericConnWithClass.getAttributes() ).thenReturn( attributesWithClass );
    when( genericConnWithClass.getDatabaseType() ).thenReturn( genericDBType );
    when( service.getDatasourceByName( anyString() ) ).thenAnswer( new Answer<IDatabaseConnection>() {
      @Override
      public IDatabaseConnection answer( InvocationOnMock invocation ) throws Throwable {
        if ( invocation.getArguments()[0].equals( GENERIC_CONN_PRIVATE_DRIVER ) ) {
          return genericConnPrivateDriver;
        }
        if ( invocation.getArguments()[0].equals( GENERIC_CONN_NOT_DRIVER_CLASS ) ) {
          return genericConnWithNoDriverClass;
        }
        if ( invocation.getArguments()[0].equals( GENERIC_CONN_NOT_FOUND_CLASS ) ) {
          return genericConnWithNoExistClass;
        }
        if ( invocation.getArguments()[0].equals( GENERIC_CONN_WITHOUT_DRIVER_CLASS ) ) {
          return genericConnWithoutClass;
        }
        if ( invocation.getArguments()[0].equals( GENERIC_CONN_DRIVER_CLASS ) ) {
          return genericConnWithClass;
        }
        if ( invocation.getArguments()[0].equals( CONN_CAN_NOT_PREPARED ) ) {
          return null;
        }
        // throw exception for check if we get exception from getting connection
        throw new DatasourceMgmtServiceException();
      }
    } );

    when( resLoader.getPluginSetting( this.anyClass(), anyString(), anyString() ) )
      .thenReturn( SimpleDataAccessPermissionHandler.class.getName() );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            if ( invocation.getArguments()[0].equals( IDatasourceMgmtService.class ) ) {
              return service;
            }
            if ( invocation.getArguments()[0].equals( IAuthorizationPolicy.class ) ) {
              return policy;
            }
            if ( invocation.getArguments()[0].equals( IPluginResourceLoader.class ) ) {
              return resLoader;
            }
            return null;
          }
        } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
  }

  @After
  public void tearDown() throws SQLException {
    sqlConnection = null;
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testGetConnection_ValidGeneric() throws DatasourceServiceException {
    assertNotNull( DatasourceInMemoryServiceHelper.getConnection( GENERIC_CONN_DRIVER_CLASS ) );
  }

  @Test
  public void testGetConnection_Invalid() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getConnection( CONN_CAN_NOT_PREPARED ) );
  }

  @Test
  public void testGetConnection_Exception() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getConnection( CONN_THROW_EXCEPTION ) );
  }

  @Test
  public void testGetCsvDataSample_withtHeader() {
    testGetCsvDataSample_Base( true, "1", 5, ";", CSV_FILE );
  }

  @Test
  public void testGetCsvDataSample_withoutHeader() {
    testGetCsvDataSample_Base( false, "id", 3, ";", CSV_FILE );
  }

  @Test
  public void testGetCsvDataSample_differentDelimetr() {
    testGetCsvDataSample_Base( false, "id;name;", 3, ":", CSV_FILE );
  }

  @Test
  public void testGetCsvDataSample_FileNotFound() {
    List<List<String>> actualList = DatasourceInMemoryServiceHelper.getCsvDataSample( "File  not found", true, "", "\"", 1 );
    assertNotNull( actualList );
    assertTrue( actualList.isEmpty() );
  }

  private void testGetCsvDataSample_Base( boolean headerExist, String listValue, int rowLimit, String delimeter, String file ) {
    List<List<String>> actualList = DatasourceInMemoryServiceHelper.getCsvDataSample( file, headerExist, delimeter, "\"", rowLimit );
    assertNotNull( actualList );
    assertFalse( actualList.isEmpty() );
    assertFalse( actualList.get( 0 ).isEmpty() );
    assertEquals( listValue, actualList.get( 0 ).get( 0 ) );
    if  ( headerExist ) {
      assertEquals( rowLimit - 1, actualList.size() );
    } else {
      assertEquals( rowLimit, actualList.size() );
    }
  }

  @Test
  public void testGetDataSourceConnection_ValidGeneric() throws DatasourceServiceException {
    assertNotNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( GENERIC_CONN_DRIVER_CLASS ) );
  }

  @Test
  public void testGetDataSourceConnection_Invalid() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( CONN_CAN_NOT_PREPARED ) );
  }

  @Test
  public void testGetDataSourceConnection_Exception() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( CONN_THROW_EXCEPTION ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGetDataSourceConnection_emptyDriverClass() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( GENERIC_CONN_WITHOUT_DRIVER_CLASS ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGetDataSourceConnection_nonExistClass() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( GENERIC_CONN_NOT_FOUND_CLASS ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGetDataSourceConnection_noDriverClass() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( GENERIC_CONN_NOT_DRIVER_CLASS ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGetDataSourceConnection_privateDriver() throws DatasourceServiceException {
    assertNull( DatasourceInMemoryServiceHelper.getDataSourceConnection( GENERIC_CONN_PRIVATE_DRIVER ) );
  }

  @Test
  public void testGetSerializeableResultSet() throws DatasourceServiceException, SQLException {
    SerializedResultSet rs =
        DatasourceInMemoryServiceHelper.getSerializeableResultSet( GENERIC_CONN_DRIVER_CLASS, SAMPLE_VALID_QUERY, 10, null );
    assertNotNull( rs );
    verify( sqlConnection ).close();
  }

  @Test
  public void testGetSerializeableResultSet_NullResultSet() throws SQLException {
    try {
      DatasourceInMemoryServiceHelper.getSerializeableResultSet( GENERIC_CONN_DRIVER_CLASS, SAMPLE_INVALID_QUERY, 10, null );
    } catch ( DatasourceServiceException e ) {
      verify( sqlConnection ).close();
      return;
    }
    fail( "We should get exception" );
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }

  public static class TestSQLDriver implements Driver {

    @Override
    public Connection connect( String url, Properties info ) throws SQLException {
      return sqlConnection;
    }

    @Override
    public boolean acceptsURL( String url ) throws SQLException {
      return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
      return null;
    }

    @Override
    public int getMajorVersion() {
      return 0;
    }

    @Override
    public int getMinorVersion() {
      return 0;
    }

    @Override
    public boolean jdbcCompliant() {
      return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }
  }

  private static class PrivateSQLDriver implements Driver {

    @Override
    public Connection connect( String url, Properties info ) throws SQLException {
      return null;
    }

    @Override
    public boolean acceptsURL( String url ) throws SQLException {
      return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
      return null;
    }

    @Override
    public int getMajorVersion() {
      return 0;
    }

    @Override
    public int getMinorVersion() {
      return 0;
    }

    @Override
    public boolean jdbcCompliant() {
      return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }

  }
}
