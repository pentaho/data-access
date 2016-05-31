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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ConnectionServiceHelperTest {

  private static final String VALID_CONNECTION = "validConnection";

  private static final String INVALID_CONNECTION = "invalidConnection";

  private static final String EXCEPTION_CONNECTION = "exceptionConnection";

  private IPentahoObjectFactory pentahoObjectFactory;

  private IDatabaseConnection connection = mock( IDatabaseConnection.class );

  private final IDatasourceMgmtService service = mock( IDatasourceMgmtService.class );

  @Before
  public void setUp() throws Exception {
    when( service.getDatasourceByName( anyString() ) ).thenAnswer( new Answer<IDatabaseConnection>() {
      @Override
      public IDatabaseConnection answer( InvocationOnMock invocation ) throws Throwable {
        if ( invocation.getArguments()[0].equals( VALID_CONNECTION ) ) {
          return connection;
        }
        if ( invocation.getArguments()[0].equals( INVALID_CONNECTION ) ) {
          return null;
        }
        //throw exception  for check if we get exception from getting connection
        throw new DatasourceMgmtServiceException();
      }
    } );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) )
       .thenAnswer( new Answer<Object>() {
           @Override
           public Object answer( InvocationOnMock invocation ) throws Throwable {
             if ( invocation.getArguments()[0].equals( IDatasourceMgmtService.class ) ) {
               return service;
             }
             return null;
           }
         } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testEncodePassword_null() {
    testEncodePassword( null, "" );
  }

  @Test
  public void testEncodePassword_empty() {
    testEncodePassword( "", "" );
  }

  @Test
  public void testEncodePassword() {
    testEncodePassword( "password", "********" );
  }

  private void testEncodePassword( String passBeforeEncode, String passAfterEncode ) {
    String encodedPass = ConnectionServiceHelper.encodePassword( passBeforeEncode );
    assertEquals( passAfterEncode, encodedPass );
  }

  @Test
  public void testGetConnectionPassword() throws Exception {
    String password = ConnectionServiceHelper.getConnectionPassword( VALID_CONNECTION, "password" );
    assertEquals( "password", password );
    verify( connection, never() ).getPassword();
  }

  @Test
  public void testGetConnectionPassword_nullPassword() throws Exception {
    String password = ConnectionServiceHelper.getConnectionPassword( VALID_CONNECTION, null );
    assertNull( password );
  }

  @Test
  public void testGetConnectionPassword_replacePassword() throws Exception {
    String password = ConnectionServiceHelper.getConnectionPassword( VALID_CONNECTION, "*" );
    assertNull( password );
  }

  @Test
  public void testGetConnectionPassword_emptyPassword() throws Exception {
    String password = ConnectionServiceHelper.getConnectionPassword( VALID_CONNECTION, "" );
    assertEquals( "", password );
  }

  @Test
  public void testGetConnectionPassword_nullConnection() throws Exception {
    String password = ConnectionServiceHelper.getConnectionPassword( INVALID_CONNECTION, "password" );
    assertEquals( "password", password );
  }

  @Test
  public void testGetConnectionPassword_nullConnectionNullPass() throws Exception {
    String password = ConnectionServiceHelper.getConnectionPassword( INVALID_CONNECTION, null );
    assertNull( password );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testGetConnectionPassword_getErrorFromService() throws Exception {
    ConnectionServiceHelper.getConnectionPassword( EXCEPTION_CONNECTION, "password" );
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
}
