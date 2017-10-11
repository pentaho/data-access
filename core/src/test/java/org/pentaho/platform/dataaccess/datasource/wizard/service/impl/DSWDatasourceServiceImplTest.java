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

import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.SqlQueriesNotSupportedException;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.TestObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLMetaData;
import org.junit.Test;

public class DSWDatasourceServiceImplTest {

  private static final String CONNECTION_NAME = "connection";
  private static final String DB_TYPE = "jdbc";
  private static final String VALID_QUERY = "valid query";
  private static final String QUERY_COLUMN_ALREADY_EXIST = "invalid query";
  private static final String PREVIEW_LIMIT = "100";

  private static final String DOMAIN_ID_2MODELS = "DOMAIN_ID_2MODELS";
  private static final String DOMAIN_ID_DOES_NOT_EXIST = "DOMAIN_ID_DOESNOT_EXIST";
  private static final String MODEL_NAME = "modelName";

  private static final String LOGICAL_MODEL_ID_DEFAULT = "<def>";
  private static final String LOGICAL_MODEL_ID_REPORTING = "Reporting";
  private static final String LOGICAL_MODEL_ID_ANALYSIS = "Analysis";

  private static final String LOGICAL_MODEL_CONTEXTNAME = "contextName";

  private static final String STRING_DEFAULT = "<def>";

  private LogicalModel analysisModel;
  private LogicalModel reportingModel;

  private DSWDatasourceServiceImpl dswService;
  private Domain domain2Models;
  private final IMetadataDomainRepository domainRepository = mock( IMetadataDomainRepository.class );
  private ModelerWorkspace workspace2Models;

  private IPentahoObjectFactory pentahoObjectFactory;

  private final IMondrianCatalogService mondrianService = mock( IMondrianCatalogService.class );
  private final SQLConnection sqlConnection = mock( SQLConnection.class );
  private final Connection nativeConnection = mock( Connection.class );
  private final ModelerService modelerService = mock( ModelerService.class );

  private int[] columnTypes = new int[]{ Types.INTEGER };
  private Object[] columns = new Object[]{ "id" };

  @Before
  public void setUp() throws Exception {
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName( CONNECTION_NAME );
    SqlPhysicalTable sqlTable = new SqlPhysicalTable();
    sqlTable.setTargetTable( VALID_QUERY );
    SqlPhysicalModel sqlModel = new SqlPhysicalModel();
    sqlModel.addPhysicalTable( sqlTable );
    sqlModel.setDatasource( dataSource );

    analysisModel = new LogicalModel();
    analysisModel.setId( LOGICAL_MODEL_ID_ANALYSIS );
    analysisModel.setProperty( DSWDatasourceServiceImpl.LM_PROP_VISIBLE, LOGICAL_MODEL_CONTEXTNAME );
    reportingModel = new LogicalModel();
    reportingModel.setId( LOGICAL_MODEL_ID_REPORTING );

    domain2Models = new Domain();
    domain2Models.setId( DOMAIN_ID_2MODELS );
    domain2Models.addLogicalModel( analysisModel );
    domain2Models.addLogicalModel( reportingModel );
    domain2Models.setLocales( Arrays.asList( new LocaleType( "en_US", "Test locale" ) ) );
    domain2Models.addPhysicalModel( sqlModel );

    Set<String> domains = new TreeSet<String>();
    domains.add( DOMAIN_ID_2MODELS );
    doReturn( domain2Models ).when( domainRepository ).getDomain( DOMAIN_ID_2MODELS );
    doReturn( domains ).when( domainRepository ).getDomainIds();

    doAnswer( new Answer<Object>() {
      @Override
      public Void answer( InvocationOnMock invocation ) throws Throwable {
        final String modelId = (String) invocation.getArguments()[ 1 ];
        final LogicalModel modelToRemove = domain2Models.findLogicalModel( modelId );
        domain2Models.getLogicalModels().remove( modelToRemove );
        return null;
      }
    } ).when( domainRepository ).removeModel( anyString(), anyString() );

    workspace2Models = mock( ModelerWorkspace.class );
    when( workspace2Models.getLogicalModel( ModelerPerspective.ANALYSIS ) ).thenReturn( analysisModel );
    when( workspace2Models.getLogicalModel( ModelerPerspective.REPORTING ) ).thenReturn( reportingModel );

    dswService = spy( new DSWDatasourceServiceImpl( mock( ConnectionServiceImpl.class ) ) );
    doNothing().when( dswService ).checkSqlQueriesSupported( anyString() );
    dswService.setMetadataDomainRepository( domainRepository );

    Object[][] coumnHeaders = new Object[][]{ columns };
    SQLMetaData metadata = mock( SQLMetaData.class );
    when( metadata.getColumnHeaders() ).thenReturn( coumnHeaders );
    when( metadata.getJDBCColumnTypes() ).thenReturn( columnTypes );
    IPentahoResultSet resultSet = mock( IPentahoResultSet.class );
    when( resultSet.getMetaData() ).thenReturn( metadata );

    doReturn( resultSet ).when( sqlConnection ).executeQuery( matches( "(.*" + VALID_QUERY + ".*)" ) );
    when( sqlConnection.executeQuery( matches( "(.*" + QUERY_COLUMN_ALREADY_EXIST + ".*)" ) ) ).thenThrow(
        new SQLException( "Reason", "S0021", 21 ) );
    doReturn( nativeConnection ).when( sqlConnection ).getNativeConnection();

    MondrianCatalog catalog = mock( MondrianCatalog.class );
    doReturn( catalog ).when( mondrianService ).getCatalog( anyString(), any( IPentahoSession.class ) );

    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) )
       .thenAnswer( new Answer<Object>() {
           @Override
           public Object answer( InvocationOnMock invocation ) throws Throwable {
             if ( invocation.getArguments()[0].equals( IMondrianCatalogService.class ) ) {
               return mondrianService;
             }
             if ( invocation.getArguments()[0].equals( IPentahoConnection.class ) ) {
               return sqlConnection;
             }
             if ( invocation.getArguments()[0].equals( IMetadataDomainRepository.class ) ) {
               return domainRepository;
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
  public void testDeleteLogicalModel_allModelsRemoved() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();
    assertTrue( dswService.deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME ) );
    verify( domainRepository ).removeModel( DOMAIN_ID_2MODELS, LOGICAL_MODEL_ID_REPORTING );
    verify( domainRepository ).removeModel( DOMAIN_ID_2MODELS, LOGICAL_MODEL_ID_ANALYSIS );
  }

  @Test
  public void testDeleteLogicalModel_removeReportingModelIfAnalysisModelDoesNotExist() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    ModelerWorkspace workspace = mock( ModelerWorkspace.class );
    when( workspace.getLogicalModel( ModelerPerspective.REPORTING ) ).thenReturn( reportingModel );

    doReturn( workspace ).when( dswService ).createModelerWorkspace();
    doCallRealMethod().when( dswService ).deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME );

    assertTrue( dswService.deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME ) );
    verify( domainRepository ).removeModel( DOMAIN_ID_2MODELS, LOGICAL_MODEL_ID_REPORTING );
  }

  @Test
  public void testDeleteLogicalModel_keepDomainIfLogicalModelExist() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();

    LogicalModel logicalModel = new LogicalModel();
    logicalModel.setId( LOGICAL_MODEL_ID_DEFAULT );
    domain2Models.getLogicalModels().add( logicalModel );
    assertTrue( dswService.deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME ) );

    List<LogicalModel> logicalModels = domain2Models.getLogicalModels();
    assertNotNull( logicalModels );
    assertEquals( 1, logicalModels.size() );
    verify( domainRepository, never() ).removeDomain( domain2Models.getId() );
  }

  @Test
  public void testDeleteLogicalModel_removeDomainIfLogicalModelDoesNotExist() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();
    assertTrue( dswService.deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME ) );
    assertTrue( domain2Models.getLogicalModels().isEmpty() );
    verify( domainRepository ).removeDomain( domain2Models.getId() );
  }

  @Test
  public void testDeleteLogicalModel_DoNotDeleteAnyWithoutPermissions() throws Exception {
    doReturn( false ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();
    assertFalse( dswService.deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME ) );

    List<LogicalModel> logicalModels = domain2Models.getLogicalModels();
    assertNotNull( logicalModels );
    assertEquals( 2, logicalModels.size() );
    verify( domainRepository, never() ).removeDomain( domain2Models.getId() );
    verify( domainRepository, never() ).removeModel( DOMAIN_ID_2MODELS, LOGICAL_MODEL_ID_REPORTING );
  }

  @Test
  public void testDeleteLogicalModel_DomainDoesNotExist() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();
    //domain already deleted or does not exist, we return that delete operation was successful
    assertTrue( dswService.deleteLogicalModel( DOMAIN_ID_DOES_NOT_EXIST, MODEL_NAME ) );
  }

  @Test
  public void testDeleteLogicalModel_MondrianDeleted() throws Exception {
    String mondrianName = "mondrianRef";
    analysisModel.setProperty( DSWDatasourceServiceImpl.LM_PROP_MONDRIAN_CATALOG_REF, mondrianName );

    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();
    assertTrue( dswService.deleteLogicalModel( DOMAIN_ID_2MODELS, MODEL_NAME ) );
    verify( mondrianService ).removeCatalog( eq( mondrianName ), any( IPentahoSession.class ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDeleteLogicalModel_DomainStorageException() throws Exception {
    testDeleteLogicalModel_Exception( DomainStorageException.class );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDeleteLogicalModel_MondrianCatalogServiceException() throws Exception {
    testDeleteLogicalModel_Exception( MondrianCatalogServiceException.class );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDeleteLogicalModel_DomainIdNullException() throws Exception {
    testDeleteLogicalModel_Exception( DomainIdNullException.class );
  }

  private void testDeleteLogicalModel_Exception( Class<? extends Throwable> clazz ) throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( workspace2Models ).when( dswService ).createModelerWorkspace();
    doThrow( clazz ).when( domainRepository ).removeModel( anyString(), anyString() );
    dswService.deleteLogicalModel( null, MODEL_NAME );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testSaveLogicalModel__DoNotSaveAnyWithoutPermissions() throws Exception {
    doReturn( false ).when( dswService ).hasDataAccessPermission();
    assertFalse( dswService.saveLogicalModel( domain2Models, false ) );
    verify( domainRepository, never() ).storeDomain( any( Domain.class ), anyBoolean() );
  }

  @Test
  public void testSaveLogicalModel() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    assertTrue( dswService.saveLogicalModel( domain2Models, false ) );
    verify( domainRepository ).storeDomain( any( Domain.class ), anyBoolean() );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testSaveLogicalModel_DomainStorageException() throws Exception {
    testSaveLogicalModel_Exception( DomainStorageException.class );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testSaveLogicalModel_DomainAlreadyExistsException() throws Exception {
    testSaveLogicalModel_Exception( DomainAlreadyExistsException.class );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testSaveLogicalModel_DomainIdNullException() throws Exception {
    testSaveLogicalModel_Exception( DomainIdNullException.class );
  }

  private void testSaveLogicalModel_Exception( Class<? extends Throwable> clazz ) throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doThrow( clazz ).when( domainRepository ).storeDomain( any( Domain.class ), anyBoolean() );
    dswService.saveLogicalModel( domain2Models, false );
  }

  @Test
  public void testTestDataSourceConnection() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    assertTrue( dswService.testDataSourceConnection( CONNECTION_NAME ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testTestDataSourceConnection_NullNativeConnection() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( null ).when( sqlConnection ).getNativeConnection();
    assertTrue( dswService.testDataSourceConnection( CONNECTION_NAME ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testTestDataSourceConnection_CouldNotClose() throws Exception {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doThrow( new SQLException() ).when( nativeConnection ).close();
    assertTrue( dswService.testDataSourceConnection( CONNECTION_NAME ) );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testTestDataSourceConnection_DoesNotHavePermission() throws Exception {
    doReturn( false ).when( dswService ).hasDataAccessPermission();
    dswService.testDataSourceConnection( CONNECTION_NAME );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateLogicalModel_DoesNotHavePermission() throws DatasourceServiceException {
    doReturn( false ).when( dswService ).hasDataAccessPermission();
    dswService.generateLogicalModel( MODEL_NAME, CONNECTION_NAME, DB_TYPE, VALID_QUERY, PREVIEW_LIMIT );
  }

  @Test
  public void testGenerateLogicalModel() throws DatasourceServiceException {
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, VALID_QUERY, null, null );
  }

  @Test
  public void testGenerateLogicalModel_EmptyRoleList() throws DatasourceServiceException {
    List<String> roleList = new ArrayList<String>();
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, VALID_QUERY, roleList, null );
  }

  @Test
  public void testGenerateLogicalModel_NonEmptyRoleList() throws DatasourceServiceException {
    PentahoSessionHolder.setSession( mock( IPentahoSession.class ) );
    List<String> roleList = Arrays.asList( "systemRole" );
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, VALID_QUERY, roleList, null );
  }

  @Test
  public void testGenerateLogicalModel_EmptyUserList() throws DatasourceServiceException {
    List<String> userList = new ArrayList<String>();
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, VALID_QUERY, null, userList );
  }

  @Test
  public void testGenerateLogicalModel_NonEmptyUserList_NonEmptyRoles() throws DatasourceServiceException {
    PentahoSessionHolder.setSession( mock( IPentahoSession.class ) );
    List<String> userList = Arrays.asList( "systemUser" );
    List<String> roleList = Arrays.asList( "systemRole" );
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, VALID_QUERY, roleList, userList );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateLogicalModel_NonEmptyUserList_EmptyRoles() throws DatasourceServiceException {
    PentahoSessionHolder.setSession( mock( IPentahoSession.class ) );
    List<String> userList = Arrays.asList( "systemUser" );
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, VALID_QUERY, null, userList );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateLogicalModel_UNABLE_TO_GENERATE_MODEL_NULLNAME() throws DatasourceServiceException {
    testGenerateLogicalModel( null, CONNECTION_NAME, VALID_QUERY, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateLogicalModel_UNABLE_TO_GENERATE_MODEL_NULLConnection() throws DatasourceServiceException {
    testGenerateLogicalModel( MODEL_NAME, null, VALID_QUERY, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateLogicalModel_UNABLE_TO_GENERATE_MODEL_NULLQUERY() throws DatasourceServiceException {
    testGenerateLogicalModel( MODEL_NAME, CONNECTION_NAME, null, null, null );
  }

  private void testGenerateLogicalModel( String modelName, String connName, String query, List<String> roleList, List<String> userList )
      throws DatasourceServiceException {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( roleList ).when( dswService ).getPermittedRoleList();
    doReturn( userList ).when( dswService ).getPermittedUserList();
    doReturn( 1 ).when( dswService ).getDefaultAcls();
    BusinessData businessData = dswService.generateLogicalModel( modelName, connName, DB_TYPE, query, PREVIEW_LIMIT );
    assertNotNull( businessData );
    assertNotNull( businessData.getDomain() );
    assertNotNull( businessData.getData() );
  }

  @Test
  public void testDoPreview() throws DatasourceServiceException {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    SerializedResultSet result = dswService.doPreview( CONNECTION_NAME, VALID_QUERY, PREVIEW_LIMIT );
    assertNotNull( result );
    assertArrayEquals( columns, result.getColumns() );
    assertArrayEquals( columnTypes, result.getColumnTypes() );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDoPreview_DoesNotHavePermission() throws DatasourceServiceException {
    doReturn( false ).when( dswService ).hasDataAccessPermission();
    dswService.doPreview( CONNECTION_NAME, VALID_QUERY, PREVIEW_LIMIT );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDoPreview_NullConnection() throws DatasourceServiceException {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    dswService.doPreview( null, VALID_QUERY, PREVIEW_LIMIT );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testDoPreview_NullQuery() throws DatasourceServiceException {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    SerializedResultSet result = dswService.doPreview( CONNECTION_NAME, null, PREVIEW_LIMIT );
    assertNotNull( result );
    assertArrayEquals( columns, result.getColumns() );
    assertArrayEquals( columnTypes, result.getColumnTypes() );
  }

  @Test
  public void testGenerateQueryDomain() throws DatasourceServiceException {
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateQueryDomain_EmptyModelName() throws DatasourceServiceException {
    testGenerateQueryDomain( "", VALID_QUERY, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateQueryDomain_UnableToSerializeCommonCause() throws Exception {
    doThrow( new Exception() ).when( modelerService ).serializeModels( any( Domain.class ), anyString() );
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateQueryDomain_UnableToSerialize() throws Exception {
    when( modelerService.serializeModels( any( Domain.class ), anyString() ) ).thenThrow( new ModelerException() );
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateQueryDomain_SQLException() throws Exception {
    testGenerateQueryDomain( MODEL_NAME, QUERY_COLUMN_ALREADY_EXIST, null, null );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateQueryDomain_NullQuery() throws DatasourceServiceException {
    testGenerateQueryDomain( MODEL_NAME, null, null, null );
  }

  @Test
  public void testGenerateQueryDomain_EmptyRoleList() throws DatasourceServiceException {
    List<String> roleList = new ArrayList<String>();
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, roleList, null );
  }

  @Test
  public void testGenerateQueryDomain_NonEmptyRoleList() throws DatasourceServiceException {
    List<String> roleList = Arrays.asList( "systemRole" );
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, roleList, null );
  }

  @Test
  public void testGenerateQueryDomain_EmptyUserList() throws DatasourceServiceException {
    List<String> userList = new ArrayList<String>();
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, null, userList );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGenerateQueryDomain_NonEmptyUserList_EmptyRoles() throws DatasourceServiceException {
    List<String> userList = Arrays.asList( "systemUser" );
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, null, userList );
  }

  @Test
  public void testGenerateQueryDomain_NonEmptyUserList_NonEmptyRoles() throws DatasourceServiceException {
    List<String> userList = Arrays.asList( "systemUser" );
    List<String> roleList = Arrays.asList( "systemRole" );
    testGenerateQueryDomain( MODEL_NAME, VALID_QUERY, roleList, userList );
  }



  private void testGenerateQueryDomain( String modelName, String query, List<String> roleList, List<String> userList ) throws DatasourceServiceException {
    ModelInfo modelInfo = mock( ModelInfo.class );
    when( modelInfo.getFileInfo() ).thenReturn( mock( CsvFileInfo.class ) );
    DatasourceDTO datasourceDTO = new DatasourceDTO();
    datasourceDTO.setConnectionName( CONNECTION_NAME );
    datasourceDTO.setDatasourceName( CONNECTION_NAME );
    datasourceDTO.setCsvModelInfo( modelInfo );

    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( CONNECTION_NAME );
    connection.setDatabaseType( mock( IDatabaseType.class ) );

    doReturn( modelerService ).when( dswService ).createModelerService();
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( roleList ).when( dswService ).getPermittedRoleList();
    doReturn( userList ).when( dswService ).getPermittedUserList();
    doReturn( null ).when( dswService ).getGeoContext();
    doReturn( 1 ).when( dswService ).getDefaultAcls();
    QueryDatasourceSummary summary = dswService.generateQueryDomain( modelName, query, connection, datasourceDTO );
    assertNotNull( summary );
    assertNotNull( summary.getDomain() );
    assertEquals( CONNECTION_NAME, summary.getDomain().getId() );
  }

  @Test( expected = DatasourceServiceException.class )
  public void testGetLogicalModels_DoesNotHavePermissionViewPermission() throws DatasourceServiceException {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( false ).when( dswService ).hasDataAccessViewPermission();
    dswService.getLogicalModels( LOGICAL_MODEL_CONTEXTNAME );
  }

  @Test
  public void testGetLogicalModels_visibleToContext() throws DatasourceServiceException {
    testGetLogicalModels_NullContext( LOGICAL_MODEL_CONTEXTNAME );
  }

  @Test
  public void testGetLogicalModels_NonVisibleToContext() throws DatasourceServiceException {
    testGetLogicalModels_NullContext( "anotherContext" );
  }

  @Test
  public void testGetLogicalModels_NullContext() throws DatasourceServiceException {
    testGetLogicalModels_NullContext( null );
  }

  @Test
  public void testGetLogicalModels_EmptyContext() throws DatasourceServiceException {
    testGetLogicalModels_NullContext( "" );
  }

  private void testGetLogicalModels_NullContext( String context ) throws DatasourceServiceException {
    doReturn( true ).when( dswService ).hasDataAccessPermission();
    doReturn( true ).when( dswService ).hasDataAccessViewPermission();
    List<LogicalModelSummary> models = dswService.getLogicalModels( context );
    assertNotNull( models );
    for ( LogicalModelSummary logicalModelSummary : models ) {
      assertEquals( domain2Models.getId(), logicalModelSummary.getDomainId() );
    }
  }

  @Test
  public void testListDatasourceNames() throws Exception {
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getId() ).thenReturn( "SessionId" );
    when( session.getActionName() ).thenReturn( "ActionNAme" );
    when( session.getProcessId() ).thenReturn( "ProcessId" );
    PentahoSessionHolder.setSession( session );
    List<String> datasources = dswService.listDatasourceNames();
    assertNotNull( datasources );
    assertFalse( datasources.isEmpty() );
    assertTrue( datasources.contains( LOGICAL_MODEL_ID_REPORTING ) );
  }

  @Test
  public void testLoadBusinessData() throws DatasourceServiceException {
    BusinessData businessData = dswService.loadBusinessData( DOMAIN_ID_2MODELS, MODEL_NAME );
    assertNotNull( businessData );
    //should load the domain with expected id
    assertEquals( DOMAIN_ID_2MODELS, businessData.getDomain().getId() );
  }

  @Test
  public void testHasPermission_nullSession() {
    PentahoSessionHolder.setSession( null );
    assertFalse( dswService.hasPermission() );
  }

  //should create new modeler service without exception
  @Test
  public void testCreateModelerService() {
    ModelerService service = dswService.createModelerService();
    assertNotNull( service );
  }

  //should create new workspace without exception
  @Test
  public void testCreateModelerWorkspace() {
    ModelerWorkspace workspace = dswService.createModelerWorkspace();
    assertNotNull( workspace );
  }


  @Test( expected = SqlQueriesNotSupportedException.class )
  public void testSqlQueries_AreNotSupported_PentahoDataServices() throws Exception {
    String connNameDataService = "connToDataService";
    String dbTypeIdDataService = "Pentaho Data Services";

    DatabaseType  dbtype = new DatabaseType( dbTypeIdDataService, STRING_DEFAULT, null, 0, STRING_DEFAULT );
    IDatabaseConnection connDataService = new DatabaseConnection();
    connDataService.setDatabaseType( dbtype );

    ConnectionServiceImpl connService = mock( ConnectionServiceImpl.class );
    doReturn( connDataService ).when( connService ).getConnectionByName( eq( connNameDataService ) );
    DSWDatasourceServiceImpl service = new DSWDatasourceServiceImpl( connService );

    service.checkSqlQueriesSupported( connNameDataService );
  }

  @Test
  public void testSqlQueries_Supported_PostgresDb() throws Exception {
    String connNamePostgres = "connToPostgresDb";
    String dbTypeIdPostgres = "PostgresDb";

    IDatabaseConnection connDataService = new DatabaseConnection();
    connDataService.setDatabaseType( new DatabaseType( dbTypeIdPostgres, STRING_DEFAULT, null, 0, STRING_DEFAULT ) );

    ConnectionServiceImpl connService = mock( ConnectionServiceImpl.class );
    doReturn( connDataService ).when( connService ).getConnectionByName( eq( connNamePostgres ) );
    DSWDatasourceServiceImpl service = new DSWDatasourceServiceImpl( connService );

    service.checkSqlQueriesSupported( connNamePostgres );
  }

  @Test
  public void testDeSerializeModelStateValidString() throws Exception {
    PentahoSystem.registerObjectFactory( new TestObjectFactory() );

    DatasourceModel datasourceModel = new DatasourceModel();
    datasourceModel.setDatasourceName( "testDatasource" );
    datasourceModel.setDatasourceType( DatasourceType.CSV );

    DatasourceDTO dto = DatasourceDTO.generateDTO( datasourceModel );
    assertNotNull( dto );

    String serializedDTO = dswService.serializeModelState( dto );
    dswService.deSerializeModelState( serializedDTO );
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
    dswService.deSerializeModelState( notSafeString );
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
