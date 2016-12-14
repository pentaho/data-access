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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.Before;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.pms.messages.util.LocaleHelper;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provide a base to stand platform and test services
 */
public class DataAccessServiceTestBase {

  protected DSWDatasourceServiceImpl datasourceService;
  protected Domain domain;
  protected IPluginResourceLoader pluginResourceLoader;
  protected IMondrianCatalogService mondrianCatalogService;
  protected ModelerService modelerService;

  protected static GeoContextConfigProvider config;

  protected static IMetadataDomainRepository metadataRepository;
  protected static IPlatformImporter importer;
  protected static IAuthorizationPolicy policy;
  protected static IAclAwareMondrianCatalogService catalogService;
  protected static IDataAccessPermissionHandler permissionHandler;
  protected static IUserRoleListService userRoleListService;
  protected static final RepositoryFileAclDto acl = new RepositoryFileAclDto();

  @Before
  public void setUp() throws Exception {
    MicroPlatform platform = new MicroPlatform();

    metadataRepository = mock( IMetadataDomainRepository.class );
    platform.defineInstance( IMetadataDomainRepository.class, metadataRepository );

    importer = mock( IPlatformImporter.class );
    platform.defineInstance( IPlatformImporter.class, importer );

    policy = mock( IAuthorizationPolicy.class );
    platform.defineInstance( IAuthorizationPolicy.class, policy );

    catalogService = mock( IAclAwareMondrianCatalogService.class );
    platform.defineInstance( IMondrianCatalogService.class, catalogService );

    permissionHandler = mock( IDataAccessPermissionHandler.class );
    platform.defineInstance( IDataAccessPermissionHandler.class, permissionHandler );

    userRoleListService = mock( IUserRoleListService.class );
    platform.defineInstance( IUserRoleListService.class, userRoleListService );

    pluginResourceLoader = mock( IPluginResourceLoader.class );
    platform.defineInstance( IPluginResourceLoader.class, pluginResourceLoader );

    mondrianCatalogService = mock( IMondrianCatalogService.class );
    platform.defineInstance( IMondrianCatalogService.class, mondrianCatalogService );

    final IUnifiedRepository unifiedRepository = new FileSystemBackedUnifiedRepository( "target/test-classes/solution1" );
    platform.defineInstance( IUnifiedRepository.class, unifiedRepository );
    platform.defineInstance( String.class, "admin" );

    config = new GeoContextPropertiesProvider( getGeoProps() );
    GeoContext geo = GeoContextFactory.create( config );

    platform.start();
    acl.setOwner( "owner" );
    acl.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    modelerService = new ModelerService();
    datasourceService = mock( DSWDatasourceServiceImpl.class );
    when( datasourceService.getGeoContext() ).thenReturn( geo );
    modelerService.setDatasourceService( datasourceService );

    domain = getDomain();
  }

  /**
   * Provide mock geo props to initialize geoContext
   *
   * @return
   */
  private Properties getGeoProps() {
    Properties geoProps = new Properties();
    geoProps.setProperty( "geo.roles", "continent, country, state, city, postal_code" );

    geoProps.setProperty( "geo.dimension.name", "Geography" );

    geoProps.setProperty( "geo.continent.aliases", "continent" );

    geoProps.setProperty( "geo.country.aliases", "country, ctry" );

    geoProps.setProperty( "geo.state.aliases", "state, province, st, stateprovince" );
    geoProps.setProperty( "geo.state.required-parents", "country" );

    geoProps.setProperty( "geo.city.aliases", "city, town" );
    geoProps.setProperty( "geo.city.required-parents", "country, state" );

    geoProps.setProperty( "geo.postal_code.aliases", "zip, postal code, zip code" );
    geoProps.setProperty( "geo.postal_code.required-parents", "country" );

    geoProps.setProperty( "geo.latitude.aliases", "lat, latitude" );
    geoProps.setProperty( "geo.longitude.aliases", "long, lng, longitude" );

    return geoProps;
  }

  /**
   * Provide a mock domain
   *
   * @return
   */
  private Domain getDomain() {
    LogicalColumn logicalColumn1;
    LogicalColumn logicalColumn2;

    String locale = LocaleHelper.getLocale().toString();

    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName( "SampleData" );
    model.setDatasource( dataSource );
    SqlPhysicalTable table = new SqlPhysicalTable( model );
    model.getPhysicalTables().add( table );
    table.setTargetTableType( TargetTableType.INLINE_SQL );
    table.setTargetTable( "select * from customers" );
    table.setId( "customers" );

    SqlPhysicalColumn column = new SqlPhysicalColumn( table );
    column.setTargetColumn( "customername" );
    column.setName( new LocalizedString( locale, "Customer Name" ) );
    column.setDescription( new LocalizedString( locale, "Customer Name Desc" ) );
    column.setDataType( DataType.STRING );
    column.setId( "cutomer_customername" );

    table.getPhysicalColumns().add( column );

    LogicalModel logicalModel = new LogicalModel();
    model.setId( "MODEL" );
    model.setName( new LocalizedString( locale, "My Model" ) );
    model.setDescription( new LocalizedString( locale, "A Description of the Model" ) );

    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setId( "BT_CUSTOMERS" );
    logicalTable.setPhysicalTable( table );

    logicalModel.getLogicalTables().add( logicalTable );
    logicalModel.setName( new LocalizedString( locale, "My Model" ) );

    logicalColumn1 = new LogicalColumn();
    logicalColumn1.setId( "LC_CUSTOMERNAME" );
    logicalColumn1.setPhysicalColumn( column );
    logicalColumn1.setAggregationType( AggregationType.COUNT );
    logicalColumn1.setLogicalTable( logicalTable );
    logicalColumn1.setDataType( DataType.STRING );

    logicalColumn2 = new LogicalColumn();
    logicalColumn2.setId( "LC_CUSTOMERNUMBER" );
    logicalColumn2.setAggregationType( AggregationType.COUNT );
    logicalColumn2.setPhysicalColumn( column );
    logicalColumn2.setLogicalTable( logicalTable );
    logicalColumn2.setDataType( DataType.NUMERIC );

    logicalTable.addLogicalColumn( logicalColumn1 );
    logicalTable.addLogicalColumn( logicalColumn2 );

    Domain thisDomain = new Domain();
    thisDomain.addPhysicalModel( model );
    thisDomain.addLogicalModel( logicalModel );

    return thisDomain;
  }

}
