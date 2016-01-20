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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.mockito.Mockito;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.junit.Test;

import java.util.ArrayList;


public class DSWDatasourceServiceImplTest {
  private static final String DOMAIN_ID = "DOMAIN_ID";
  private static final String MODEL_NAME = "modelName";

  private static final String LOGICAL_MODEL_ID_DEFAULT = "<def>";
  private static final String LOGICAL_MODEL_ID_REPORTING = "Reporting";
  private static final String LOGICAL_MODEL_ID_ANALYSIS = "Analysis";

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

    ModelerWorkspace workspace = Mockito.mock( ModelerWorkspace.class );
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

  private LogicalModel mockLogicalModel() {
    return mockLogicalModel( LOGICAL_MODEL_ID_DEFAULT );
  }

  private LogicalModel mockLogicalModel( final String id ) {
    LogicalModel logicalModel = Mockito.mock( LogicalModel.class );
    Mockito.when( logicalModel.getId() ).thenReturn( id );
    Mockito.when( logicalModel.getProperty( Mockito.anyString() ) ).thenReturn( null );
    return logicalModel;
  }

  private ModelerWorkspace mockModelerWorkspace() {
    ModelerWorkspace workspace = Mockito.mock( ModelerWorkspace.class );
    Mockito.when( workspace.getLogicalModel( ModelerPerspective.ANALYSIS ) ).thenReturn( analysisModel );
    Mockito.when( workspace.getLogicalModel( ModelerPerspective.REPORTING ) ).thenReturn( reportingModel );

    return workspace;
  }

  private IMetadataDomainRepository mockDomainRepository( Domain domainToReturn, String domainId ) {
    IMetadataDomainRepository domainRepository = Mockito.mock( IMetadataDomainRepository.class );
    Mockito.doReturn( domainToReturn ).when( domainRepository ).getDomain( domainId );

    return domainRepository;
  }

  private DSWDatasourceServiceImpl mockService( ModelerWorkspace workspace ) throws Exception {
    DSWDatasourceServiceImpl service = Mockito.mock( DSWDatasourceServiceImpl.class );

    Mockito.doReturn( true ).when( service ).hasDataAccessPermission();
    Mockito.doReturn( domainRepository ).when( service ).getMetadataDomainRepository();
    Mockito.doReturn( workspace ).when( service ).createModelerWorkspace();

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
}
