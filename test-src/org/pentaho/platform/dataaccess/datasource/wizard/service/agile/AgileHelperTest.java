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
* Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class AgileHelperTest {

  private IPentahoObjectFactory pentahoObjectFactory;

  @Before
  public void setUp() throws SQLException, ObjectFactoryException {
    Connection connection = mock( Connection.class );
    DataSource dataSource = mock( DataSource.class );
    when( dataSource.getConnection() ).thenReturn( connection );

    final ICacheManager manager = mock( ICacheManager.class );
    when( manager.cacheEnabled( anyString() ) ).thenReturn( true );
    when( manager.getFromRegionCache( anyString(), any() ) ).thenReturn( dataSource );

    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            return manager;
          }
        } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    IApplicationContext context = mock( IApplicationContext.class );
    when( context.getSolutionPath( anyString() ) ).thenAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String) invocation.getArguments()[0];
      }
    } );
    PentahoSystem.setApplicationContext( context );
  }

  @Test
  public void testGenerateTableName() {
    String sampleFilename = "test.Generate.Table.Name";
    String expectedFilename = "test_Generate_Table_Name";
    String actualFilename = AgileHelper.generateTableName( sampleFilename );
    assertTrue( expectedFilename.equals( actualFilename ) );
  }

  @Test
  public void testGetCsvSampleRowSize() {
    int defaultRowSize = 100;
    int expected = Integer.MAX_VALUE;

    PentahoSystem.setSystemSettingsService( null );
    assertEquals( defaultRowSize, AgileHelper.getCsvSampleRowSize() );

    ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( anyString(), anyString(), anyString() ) ).thenReturn( null ).thenReturn(
        String.valueOf( expected ) );
    PentahoSystem.setSystemSettingsService( systemSettings );
    assertEquals( defaultRowSize, AgileHelper.getCsvSampleRowSize() );
    assertEquals( expected, AgileHelper.getCsvSampleRowSize() );
  }

  @Test
  public void testGetDatasourceSolutionStorage() {
    PentahoSystem.setSystemSettingsService( null );
    assertEquals( "admin", AgileHelper.getDatasourceSolutionStorage() );

    ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( anyString(), anyString(), anyString() ) ).thenReturn( null );
    PentahoSystem.setSystemSettingsService( systemSettings );
    assertNull( AgileHelper.getDatasourceSolutionStorage() );
  }

  @Test
  public void testGetSchemaName() {
    PentahoSystem.setSystemSettingsService( null );
    assertNull( AgileHelper.getSchemaName() );

    ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( anyString(), anyString(), anyString() ) ).thenReturn( null );
    PentahoSystem.setSystemSettingsService( systemSettings );
    assertNull( AgileHelper.getSchemaName() );
  }

  @Test
  public void testGetFolderPath() {
    PentahoSystem.setSystemSettingsService( null );
    assertNull( AgileHelper.getFolderPath( null ) );

    String sampleProject = AgileHelper.PLUGIN_NAME;
    String sampleFolderPath = "/etc/";
    ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( anyString(), anyString(), anyString() ) ).thenReturn( null ).thenReturn(
        sampleFolderPath );
    PentahoSystem.setSystemSettingsService( systemSettings );

    assertNull( AgileHelper.getFolderPath( sampleProject ) );
    assertTrue( ( sampleFolderPath + sampleProject ).equals( AgileHelper.getFolderPath( sampleProject ) ) );
  }

  @Test
  public void testGetTmpFolderPath() {
    PentahoSystem.setSystemSettingsService( null );
    assertNull( AgileHelper.getTmpFolderPath( null ) );

    String sampleProject = AgileHelper.PLUGIN_NAME;
    String sampleFolderPath = "/etc/";
    ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( anyString(), anyString(), anyString() ) )
      .thenReturn( null )
      .thenReturn( sampleFolderPath );
    PentahoSystem.setSystemSettingsService( systemSettings );

    assertNull( AgileHelper.getTmpFolderPath( sampleProject ) );
    assertTrue( ( sampleFolderPath + sampleProject ).equals( AgileHelper.getTmpFolderPath( sampleProject ) ) );
  }

  @Test
  public void testGetConnection() throws DBDatasourceServiceException, SQLException  {
    String jndiName = "HSQL";
    Connection connection =  AgileHelper.getConnection( jndiName );
    assertNotNull( connection );
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
