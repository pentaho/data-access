/*! ******************************************************************************
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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionServiceHelperTest {

  private static final String VALID_CONNECTION = "validConnection";

  private static final String INVALID_CONNECTION = "invalidConnection";

  private static final String EXCEPTION_CONNECTION = "exceptionConnection";

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
    ConnectionServiceHelper.datasourceMgmtSvc = service;
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
}
