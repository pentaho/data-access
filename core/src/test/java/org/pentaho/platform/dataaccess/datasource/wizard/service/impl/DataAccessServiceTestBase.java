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


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
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
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
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

  private MicroPlatform platform;

  @Before
  public void setUp() throws Exception {
    platform = new MicroPlatform();

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

  @After
  public void tearDown() throws Exception {
    platform.stop();
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

    model.setId( "MODEL" );
    model.setName( new LocalizedString( locale, "My Model" ) );
    model.setDescription( new LocalizedString( locale, "A Description of the Model" ) );

    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setId( "BT_CUSTOMERS" );
    logicalTable.setPhysicalTable( table );

    LogicalModel logicalModel = new LogicalModel();
    logicalModel.getLogicalTables().add( logicalTable );
    logicalModel.setName( new LocalizedString( locale, "My Model" ) );
    logicalModel.setId( "LOGICAL_MODEL" );

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
