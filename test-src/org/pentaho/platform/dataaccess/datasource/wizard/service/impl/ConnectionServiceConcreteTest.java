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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceHelper;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

import org.mockito.Matchers;
import org.mockito.Mockito;

public class ConnectionServiceConcreteTest {

  private final IPluginResourceLoader loader = Mockito.mock( IPluginResourceLoader.class );

  private IPentahoObjectFactory pentahoObjectFactory = Mockito.mock( IPentahoObjectFactory.class );

  private final SQLConnection sqlConnection = Mockito.mock( SQLConnection.class );

  private IDBDatasourceService datasourceService = Mockito.mock( IDBDatasourceService.class );

  private final PooledDatasourceHelper pdh = Mockito.mock( PooledDatasourceHelper.class );

  @InjectMocks ConnectionServiceConcrete concrete;

  @Mock ConnectionServiceImpl service;
  @Mock IDatabaseConnection iConnection;
  @Mock DatabaseConnection connection;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks( this );
  }

  @Before
  public void setUp() throws ObjectFactoryException {
    Mockito.doReturn( SimpleDataAccessPermissionHandler.class.getName() ).when( loader ).getPluginSetting(
      this.anyClass(), Matchers.anyString(), Matchers.anyString() );
    Mockito.when( pentahoObjectFactory.objectDefined( Matchers.anyString() ) ).thenReturn( true );
    Mockito.when( pentahoObjectFactory.get( this.anyClass(), Matchers.anyString(),
      Matchers.any( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            if ( invocation.getArguments()[0].equals( IPluginResourceLoader.class ) ) {
              return loader;
            }
            if ( invocation.getArguments()[0].equals( IPentahoConnection.class ) ) {
              return sqlConnection;
            }
            if ( invocation.getArguments()[0].equals( IDBDatasourceService.class ) ) {
              return datasourceService;
            }
            if ( invocation.getArguments()[0].equals( PooledDatasourceHelper.class ) ) {
              return pdh;
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
  public void testUpdateConnection() throws Exception {
    String pass = "pass";

    Mockito.when( iConnection.getPassword() ).thenReturn( pass );

    Mockito.when( service.updateConnection( Matchers.any( IDatabaseConnection.class ) ) ).thenReturn( true );
    Mockito.when( service.getConnectionById( Matchers.anyString() ) ).thenReturn( iConnection );

    Mockito.when( connection.getPassword() ).thenReturn( "" );
    Mockito.when( connection.getId() ).thenReturn( "" );

    concrete.updateConnection( connection );

    Mockito.verify( connection ).setPassword( Matchers.eq( pass ) );
  }

  private Class<?> anyClass() {
    return Matchers.argThat( new ConnectionServiceConcreteTest.AnyClassMatcher() );
  }

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {

    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }
}
