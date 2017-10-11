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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionServiceConcreteTest {

  private final IPluginResourceLoader loader = mock( IPluginResourceLoader.class );

  private IPentahoObjectFactory pentahoObjectFactory = mock( IPentahoObjectFactory.class );

  private final SQLConnection sqlConnection = mock( SQLConnection.class );

  private IDBDatasourceService datasourceService = mock( IDBDatasourceService.class );

  private final PooledDatasourceHelper pdh = mock(PooledDatasourceHelper.class);

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
    doReturn( SimpleDataAccessPermissionHandler.class.getName() ).when( loader ).getPluginSetting( this.anyClass(), anyString(), anyString() );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
            (Answer<Object>) invocation -> {
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
            });
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

    when( iConnection.getPassword() ).thenReturn( pass );

    when( service.updateConnection( any( IDatabaseConnection.class ) ) ).thenReturn( true );
    when( service.getConnectionById( anyString() ) ).thenReturn( iConnection );

    when( connection.getPassword() ).thenReturn( "" );
    when( connection.getId() ).thenReturn( "" );

    concrete.updateConnection( connection );

    verify( connection ).setPassword( eq( pass ) );
  }

  private Class<?> anyClass() {
    return argThat( new ConnectionServiceConcreteTest.AnyClassMatcher() );
  }

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {

    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }
}