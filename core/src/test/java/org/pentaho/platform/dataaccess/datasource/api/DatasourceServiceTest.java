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
 * Copyright (c) 2017-2024 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.metadata.IDataSourceAwareMetadataDomainRepository;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DatasourceServiceTest {

  private IAuthorizationPolicy authorizationPolicy;
  private DatasourceService datasourceService;

  private IPentahoObjectFactory pentahoObjectFactory;

  @Before
  public void setUpClass() throws ObjectFactoryException {
    datasourceService = spy( new DatasourceService() );
    datasourceService.pluginResourceLoader = mock( IPluginResourceLoader.class );
    authorizationPolicy = mock( IAuthorizationPolicy.class );
    when( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) ).thenReturn( true );
    when( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) ).thenReturn( true );

    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( any(), anyString(), Mockito.<IPentahoSession>any() ) )
      .thenAnswer( (Answer<Object>) invocation -> {
        if ( invocation.getArguments()[ 0 ].equals( IAuthorizationPolicy.class ) ) {
          return authorizationPolicy;
        }
        return null;
      } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
  }

  @After
  public void cleanup() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testConstructor_setsDataSourceAwareMetadataDomainRepository() {
    IDataSourceAwareMetadataDomainRepository dataSourceAwareMetadataDomainRepository =
            mock( IDataSourceAwareMetadataDomainRepository.class );
    IMondrianCatalogService mondrianCatalogService = mock( IMondrianCatalogService.class );
    RepositoryFileAclAdapter repositoryFileAclAdapter = mock( RepositoryFileAclAdapter.class );
    IPluginResourceLoader pluginResourceLoader = mock( IPluginResourceLoader.class );

    DatasourceService testInstance = new DatasourceService( dataSourceAwareMetadataDomainRepository,
            mondrianCatalogService, repositoryFileAclAdapter, pluginResourceLoader );

    assertNotNull( testInstance.dataSourceAwareMetadataDomainRepository );
  }

  @Test
  public void shouldAllowAccessForAdminWithAllPermissions() throws Exception {
    // given
    when( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true );
    when( authorizationPolicy.isAllowed( PublishAction.NAME ) ).thenReturn( true );

    //when
    try {
      DatasourceService.validateAccess();
    } catch ( PentahoAccessControlException e ) {
      fail();
    }
    // then no exception should be thrown
  }

  @Test
  public void shouldNotAllowAccessForAdminWithoutPublishPermission() throws Exception {
    // given
    when( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true );
    when( authorizationPolicy.isAllowed( PublishAction.NAME ) ).thenReturn( false );

    // when
    try {
      DatasourceService.validateAccess();
      fail();
    } catch ( PentahoAccessControlException e ) {
      // then exception should be thrown
    }
  }

  @Test
  public void shouldAllowAccessForNonAdminWithPublishPermission() throws Exception {
    // given
    when( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( false );
    when( authorizationPolicy.isAllowed( PublishAction.NAME ) ).thenReturn( true );

    // when
    try {
      DatasourceService.validateAccess();
    } catch ( PentahoAccessControlException e ) {
      fail();
    }
    // then no exception should be thrown
  }

  @Test
  public void shouldNotAllowAccessForNonAdminWithoutPublishPermission() throws Exception {
    // given
    when( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( false );
    when( authorizationPolicy.isAllowed( PublishAction.NAME ) ).thenReturn( false );

    // when
    try {
      DatasourceService.validateAccess();
      fail();
    } catch ( PentahoAccessControlException e ) {
      // then exception should be thrown
    }
  }

  @Test
  public void isMetadataDomainTest() throws Exception {
    // given
    Domain domain = mock( Domain.class );
    List<LogicalModel> logicalModelList = new ArrayList<>(  );
    LogicalModel model = new LogicalModel();
    LogicalModel model2 = new LogicalModel();
    // when
    assertFalse( DatasourceService.isMetadataDatasource( (Domain) null ) );
    assertTrue( DatasourceService.isMetadataDatasource( domain ) );

    logicalModelList.add( model );
    when( domain.getLogicalModels() ).thenReturn( logicalModelList );
    assertTrue( DatasourceService.isMetadataDatasource( domain ) );

    model.setProperty( "AGILE_BI_GENERATED_SCHEMA", true );
    assertFalse( DatasourceService.isMetadataDatasource( domain ) );

    model2.setProperty( "WIZARD_GENERATED_SCHEMA", true );
    logicalModelList.clear();
    logicalModelList.add( model2 );
    assertFalse( DatasourceService.isMetadataDatasource( domain ) );

  }

  @Test
  public void isDSWDatasourceTest() throws Exception {
    // given
    Domain domain = mock( Domain.class );
    List<LogicalModel> logicalModelList = new ArrayList<>();
    LogicalModel model = new LogicalModel();
    LogicalModel model2 = new LogicalModel();
    // when
    assertFalse( DatasourceService.isDSWDatasource( (Domain) null ) );
    assertFalse( DatasourceService.isDSWDatasource( domain ) );

    logicalModelList.add( model );
    when( domain.getLogicalModels() ).thenReturn( logicalModelList );
    assertFalse( DatasourceService.isDSWDatasource( domain ) );

    model.setProperty( "AGILE_BI_GENERATED_SCHEMA", true );
    assertTrue( DatasourceService.isDSWDatasource( domain ) );

    model2.setProperty( "WIZARD_GENERATED_SCHEMA", true );
    logicalModelList.clear();
    logicalModelList.add( model2 );
    assertTrue( DatasourceService.isDSWDatasource( domain ) );

  }

  @Test
  public void testGetDatasourceLoadThreadCount() throws IllegalArgumentException {
    String threadCountAsString = "4";
    when( datasourceService.pluginResourceLoader.getPluginSetting( any(), anyString() ) )
        .thenReturn( threadCountAsString );
    int response = datasourceService.getDatasourceLoadThreadCount();
    assertEquals( Integer.parseInt( threadCountAsString ), response );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetDatasourceLoadThreadCountError() throws IllegalArgumentException {
    String threadCountAsString = "-4";
    when( datasourceService.pluginResourceLoader.getPluginSetting( any(), anyString() ) )
        .thenReturn( threadCountAsString );
    datasourceService.getDatasourceLoadThreadCount();
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetDatasourceLoadThreadCountInvalidInput() throws IllegalArgumentException {
    String threadCountAsString = "t";
    when( datasourceService.pluginResourceLoader.getPluginSetting( any(), anyString() ) )
        .thenReturn( threadCountAsString );
    datasourceService.getDatasourceLoadThreadCount();
  }
}
