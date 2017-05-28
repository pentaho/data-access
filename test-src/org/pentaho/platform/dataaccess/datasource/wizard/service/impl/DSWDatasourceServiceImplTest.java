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

import org.mockito.Mockito;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.SqlQueriesNotSupportedException;
import org.pentaho.platform.engine.core.TestObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;

public class DSWDatasourceServiceImplTest {
  private static final String DOMAIN_ID = "DOMAIN_ID";
  private static final String MODEL_NAME = "modelName";

  private static final String LOGICAL_MODEL_ID_REPORTING = "Reporting";
  private static final String LOGICAL_MODEL_ID_ANALYSIS = "Analysis";

  private static final String STRING_DEFAULT = "<def>";

  private LogicalModel analysisModel;
  private LogicalModel reportingModel;

  private DSWDatasourceServiceImpl service;
  private Domain domain2Models;
  private IMetadataDomainRepository domainRepository;

  @Before
  public void init() throws Exception {
    analysisModel = mockLogicalModel( LOGICAL_MODEL_ID_ANALYSIS );
    reportingModel = mockLogicalModel( LOGICAL_MODEL_ID_REPORTING );
    domain2Models = createDomain2Models();
    domainRepository = mockDomainRepository( domain2Models, DOMAIN_ID );
    ModelerWorkspace workspace = mockModelerWorkspace();
    service = mockService( workspace );
  }


  @Test
  public void reportingAndAnalysisModelsRemoved() throws Exception {

    service.deleteLogicalModel( DOMAIN_ID, MODEL_NAME );

    Mockito.verify( domainRepository ).removeModel( DOMAIN_ID, LOGICAL_MODEL_ID_REPORTING );
    Mockito.verify( domainRepository ).removeModel( DOMAIN_ID, LOGICAL_MODEL_ID_ANALYSIS );
  }

  @Test
  public void reportingModelRemoved_WhenNoAnalysisModelExists() throws Exception {
    Domain domainOnlyReportingModel = new Domain();
    domainOnlyReportingModel.setId( DOMAIN_ID );
    domainOnlyReportingModel.setLogicalModels( new ArrayList<LogicalModel>() { {
        add( reportingModel );
      } } );

    mockDomainRepository( domainOnlyReportingModel, DOMAIN_ID );

    ModelerWorkspace workspace = org.mockito.Mockito.mock( ModelerWorkspace.class );
    Mockito.when( workspace.getLogicalModel( ModelerPerspective.REPORTING ) ).thenReturn( reportingModel );

    DSWDatasourceServiceImpl service = mockService( workspace );
    Mockito.doCallRealMethod().when( service ).deleteLogicalModel( DOMAIN_ID, MODEL_NAME );

    service.deleteLogicalModel( DOMAIN_ID, MODEL_NAME );

    Mockito.verify( domainRepository ).removeModel( DOMAIN_ID, LOGICAL_MODEL_ID_REPORTING );
  }

  @Test
  public void domainNotRemoved_WhenLogicalModelsExists() throws Exception {
    performModelsDeleting();
    domain2Models.getLogicalModels().add( mockLogicalModel() );

    service.deleteLogicalModel( DOMAIN_ID, MODEL_NAME );

    Assert.assertTrue( domain2Models.getLogicalModels().size() == 1 );
    Mockito.verify( domainRepository, Mockito.never() ).removeDomain( domain2Models.getId() );
  }

  @Test
  public void domainRemoved_WhenNoLogicalModelsExists() throws Exception {
    performModelsDeleting();

    service.deleteLogicalModel( DOMAIN_ID, MODEL_NAME );

    Assert.assertTrue( domain2Models.getLogicalModels().isEmpty() );
    Mockito.verify( domainRepository ).removeDomain( domain2Models.getId() );
  }


  @Test( expected = SqlQueriesNotSupportedException.class )
  public void testSqlQueries_AreNotSupported_PentahoDataServices() throws Exception {

    final String connNameDataService = "connToDataService";
    final String dbTypeIdDataService = "Pentaho Data Services";

    IDatabaseConnection connDataService = new DatabaseConnection();
    connDataService.setDatabaseType( new DatabaseType( dbTypeIdDataService, STRING_DEFAULT, new ArrayList
      <DatabaseAccessType>(), 0, STRING_DEFAULT ) );

    ConnectionServiceImpl connService = org.mockito.Mockito.mock( ConnectionServiceImpl.class );
    org.mockito.Mockito.doReturn( connDataService ).when( connService ).getConnectionByName( org.mockito.Matchers.eq( connNameDataService ) );
    DSWDatasourceServiceImpl service = new DSWDatasourceServiceImpl( connService );

    service.checkSqlQueriesSupported( connNameDataService );
  }

  @Test
  public void testSqlQueries_Supported_PostgresDb() throws Exception {
    final String connNamePostgres = "connToPostgresDb";
    final String dbTypeIdPostgres = "PostgresDb";

    IDatabaseConnection connDataService = new DatabaseConnection();
    connDataService.setDatabaseType( new DatabaseType( dbTypeIdPostgres, STRING_DEFAULT, new ArrayList
      <DatabaseAccessType>(), 0, STRING_DEFAULT ) );

    ConnectionServiceImpl connService = org.mockito.Mockito.mock( ConnectionServiceImpl.class );
    org.mockito.Mockito.doReturn( connDataService ).when( connService ).getConnectionByName( org.mockito.Matchers.eq( connNamePostgres ) );
    DSWDatasourceServiceImpl service = new DSWDatasourceServiceImpl( connService );

    service.checkSqlQueriesSupported( connNamePostgres );
  }

  private LogicalModel mockLogicalModel() {
    return mockLogicalModel( STRING_DEFAULT );
  }

  private LogicalModel mockLogicalModel( final String id ) {
    LogicalModel logicalModel = org.mockito.Mockito.mock( LogicalModel.class );
    Mockito.when( logicalModel.getId() ).thenReturn( id );
    Mockito.when( logicalModel.getProperty( Mockito.anyString() ) ).thenReturn( null );
    return logicalModel;
  }

  private ModelerWorkspace mockModelerWorkspace() {
    ModelerWorkspace workspace = org.mockito.Mockito.mock( ModelerWorkspace.class );
    Mockito.when( workspace.getLogicalModel( ModelerPerspective.ANALYSIS ) ).thenReturn( analysisModel );
    Mockito.when( workspace.getLogicalModel( ModelerPerspective.REPORTING ) ).thenReturn( reportingModel );

    return workspace;
  }

  private IMetadataDomainRepository mockDomainRepository( Domain domainToReturn, String domainId ) {
    IMetadataDomainRepository domainRepository = org.mockito.Mockito.mock( IMetadataDomainRepository.class );
    org.mockito.Mockito.doReturn( domainToReturn ).when( domainRepository ).getDomain( domainId );

    return domainRepository;
  }

  private DSWDatasourceServiceImpl mockService( ModelerWorkspace workspace ) throws Exception {
    DSWDatasourceServiceImpl service = org.mockito.Mockito.mock( DSWDatasourceServiceImpl.class );

    org.mockito.Mockito.doReturn( true ).when( service ).hasDataAccessPermission();
    org.mockito.Mockito.doReturn( domainRepository ).when( service ).getMetadataDomainRepository();
    org.mockito.Mockito.doReturn( workspace ).when( service ).createModelerWorkspace();

    Mockito.doCallRealMethod().when( service ).deleteLogicalModel( DOMAIN_ID, MODEL_NAME );

    return service;
  }

  private Domain createDomain2Models() {
    Domain domain = new Domain();
    domain.setId( DOMAIN_ID );

    domain.setLogicalModels( new ArrayList<LogicalModel>() { {
        add( analysisModel );
        add( reportingModel );
      } } );

    return domain;
  }


  // So as not to call real remove method,
  // perform remove by this callback
  private void performModelsDeleting() throws Exception {
    Mockito.doAnswer( new Answer() {
      @Override
      public Void answer( InvocationOnMock invocation ) throws Throwable {
        final String modelId = (String) invocation.getArguments()[ 1 ];
        final LogicalModel modelToRemove = domain2Models.findLogicalModel( modelId );
        domain2Models.getLogicalModels().remove( modelToRemove );

        return null;
      }
    } ).when( domainRepository ).removeModel( Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testDeSerializeModelStateValidString() throws Exception {
    PentahoSystem.registerObjectFactory( new TestObjectFactory() );

    DatasourceModel datasourceModel = new DatasourceModel();
    datasourceModel.setDatasourceName( "testDatasource" );
    datasourceModel.setDatasourceType( DatasourceType.CSV );

    DatasourceDTO dto = DatasourceDTO.generateDTO( datasourceModel );
    Assert.assertNotNull( dto );

    org.mockito.Mockito.doCallRealMethod().when( service ).serializeModelState( org.mockito.Matchers.any( DatasourceDTO.class ) );
    String serializedDTO = service.serializeModelState( dto );

    org.mockito.Mockito.doCallRealMethod().when( service ).deSerializeModelState( org.mockito.Matchers.anyString() );
    service.deSerializeModelState( serializedDTO );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDeSerializeModelStateInvalidString() throws Exception {
    String notSafeString = "<com.malicious.DatasourceDTO>\n"
      + "  <datasourceName>testDatasource</datasourceName>\n"
      + "  <datasourceType>CSV</datasourceType>\n"
      + "  <csvModelInfo>\n"
      + "    <fileInfo>\n"
      + "      <delimiter>,</delimiter>\n"
      + "      <enclosure>&quot;</enclosure>\n"
      + "      <headerRows>1</headerRows>\n"
      + "      <currencySymbol></currencySymbol>\n"
      + "      <decimalSymbol>.</decimalSymbol>\n"
      + "      <groupSymbol>,</groupSymbol>\n"
      + "      <ifNull>---</ifNull>\n"
      + "      <nullStr></nullStr>\n"
      + "    </fileInfo>\n"
      + "    <stageTableName>testdatasource</stageTableName>\n"
      + "    <validated>false</validated>\n"
      + "    <csvInputErrors/>\n"
      + "    <tableOutputErrors/>\n"
      + "  </csvModelInfo>\n"
      + "  <connectionName>SampleData</connectionName>\n"
      + "  <version>2.0</version>\n"
      + "</com.malicious.DatasourceDTO>";

    org.mockito.Mockito.doCallRealMethod().when( service ).deSerializeModelState( org.mockito.Matchers.anyString() );
    service.deSerializeModelState( notSafeString );
  }

}
