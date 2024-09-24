/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
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
    doReturn( SimpleDataAccessPermissionHandler.class.getName() ).when( loader ).getPluginSetting( Mockito.<ClassLoader>any(), anyString(), anyString() );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( any(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
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
}