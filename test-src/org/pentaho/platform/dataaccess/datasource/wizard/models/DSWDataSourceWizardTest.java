package org.pentaho.platform.dataaccess.datasource.wizard.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.api.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWDataSource;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplate;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplateModel;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

/**
 * This class provides tests for the methods in the DSWDataSourceWizard class.
 * 
 * @author tkafalas
 */
public class DSWDataSourceWizardTest {
  private static MicroPlatform mp = new MicroPlatform();
  private static DSWDataSourceWizard dswWizard = new DSWDataSourceWizard();
  private static IMetadataDomainRepository mockMetadataDomainRepository;

  @BeforeClass
  public static void beforeClass() throws Exception {

    mockMetadataDomainRepository = mock( PentahoMetadataDomainRepository.class );

    mp.defineInstance( DSWDataSourceWizard.class, dswWizard );
    mp.defineInstance( IMetadataDomainRepository.class, mockMetadataDomainRepository );
  }

  @Test
  public void testStoreDataSource() throws Exception {
    String dataSourceName = "TestDSW";
    IDSWDataSource iDSWDataSource = setupMocksForDataSource( dataSourceName, "CSV", "CSV DisplayName" );

    doThrow( new DomainIdNullException( "Test null Exception" ) ).when( mockMetadataDomainRepository ).storeDomain(
        (Domain) isNull(), eq( false ) );

    // run the method we are testing
    dswWizard.storeDataSource( iDSWDataSource, false );

    try {
      dswWizard.storeDataSource( null, false );
      fail( "Store of null domain did not generate exception" );
    } catch ( DSWException e ) {
      // Should cause this exception
    }

    Mockito.verify( mockMetadataDomainRepository ).getDomain( dataSourceName );
    Mockito.verify( mockMetadataDomainRepository, times( 1 ) ).storeDomain( any( Domain.class ), eq( true ) );
    Mockito.verify( mockMetadataDomainRepository, times( 1 ) ).storeDomain( any( Domain.class ), eq( false ) );

  }

  @Test
  public void testLoadDataSource() throws Exception {
    String dataSourceName = "LoadDataSouceName";
    String templateID = "CSV";
    setupMocksForLoadTest( dataSourceName, templateID, "CSV DisplayName" );

    // run the method we are testing
    dswWizard.loadDataSource( dataSourceName );
    
    // run the method with invalid argument
    try {
      dswWizard.loadDataSource( null );
      fail("Load with null did not throw DSWException");
    } catch (DSWException e) {
      //Should throw this 
    }

    Mockito.verify( mockMetadataDomainRepository, times( 1 ) ).getDomain( dataSourceName );
  }

  @Test
  public void testGetTemplates() throws Exception {
    String[][] templateDefinition = setupMockTemplateList();
    // run the method we are testing
    List<IDSWTemplate> templateList = dswWizard.getTemplates();
    boolean found;
    for ( int i = 0; i < templateDefinition.length; i++ ) {
      found = false;
      for ( IDSWTemplate template : templateList ) {
        if ( templateDefinition[ i ][ 0 ].equals( template.getID() )
            && templateDefinition[ i ][ 1 ].equals( template.getDisplayName( Locale.getDefault() ) ) ) {
          if ( found == true ) {
            fail( "More than one instance of " + templateDefinition[ i ][ 0 ] + "/" + templateDefinition[ i ][ 1 ] );
          }
          found = true;
        }
      }
      if ( found == false ) {
        fail( "Could not find template entry for " + templateDefinition[ i ][ 0 ] + "/" + templateDefinition[ i ][ 1 ] );
      }
    }
  }

  @Test
  public void testGetTemplateByID() throws Exception {
    String[][] templateDefinition = setupMockTemplateList();
    for ( int i = 0; i < templateDefinition.length; i++ ) {
      IDSWTemplate iDSWTemplate = dswWizard.getTemplateByID( templateDefinition[ i ][ 0 ] );
      assertEquals( templateDefinition[ i ][ 0 ], iDSWTemplate.getID() );
      assertEquals( templateDefinition[ i ][ 1 ], iDSWTemplate.getDisplayName( Locale.getDefault() ) );
    }
  }

  @Test
  public void testGetTemplateByDatasource() {
    String dataSourceName = "TestDSW";
    IDSWDataSource iDSWDataSource = setupMocksForDataSource( dataSourceName, "CSV", "CSV DisplayName" );

    IDSWTemplate iDSWTemplate = dswWizard.getTemplateByDatasource( iDSWDataSource );
    assertNotNull( iDSWTemplate );
    assertEquals( "CSV", iDSWTemplate.getID() );

    // The message file overrides the display name as intended
    assertEquals( "CSV File", iDSWTemplate.getDisplayName( Locale.getDefault() ) );
  }

  private IDSWDataSource setupMocksForDataSource( String dataSourceName, String templateID, String templateDisplayName ) {
    reset( mockMetadataDomainRepository );
    Domain mockDomain = mock( Domain.class );
    LogicalModel mockLogicalModel = MockLogicalModel.buildLogicalModelWithTemplateModel();
    List<LogicalModel> mockLogicalModelList = new ArrayList<LogicalModel>();
    mockLogicalModelList.add( mockLogicalModel );
    when( mockDomain.getLogicalModels() ).thenReturn( mockLogicalModelList );
    when( mockMetadataDomainRepository.getDomain( anyString() ) ).thenReturn( mockDomain );

    IDSWTemplate mockTemplate = setupMockTemplate( templateID, templateDisplayName );
    dswWizard.setTemplates( Arrays.asList( mockTemplate ) );

    IDSWTemplateModel mockTemplateModel = new MockDSWTemplateModel( templateID );
    DSWDataSource dataSource = new DSWDataSource( dataSourceName, mockTemplate, mockTemplateModel );
    return dataSource;
  }

  private void setupMocksForLoadTest( String dataSourceName, String templateID, String templateDisplayName ) {
    reset( mockMetadataDomainRepository );
    Domain mockDomain = mock( Domain.class );
    LogicalModel mockLogicalModel = MockLogicalModel.buildLogicalModelWithTemplateModel();
    List<LogicalModel> mockLogicalModelList = new ArrayList<LogicalModel>();
    mockLogicalModelList.add( mockLogicalModel );
    when( mockDomain.getLogicalModels() ).thenReturn( mockLogicalModelList );
    when( mockMetadataDomainRepository.getDomain( (String) isNotNull() ) ).thenReturn( mockDomain );
    when( mockMetadataDomainRepository.getDomain( (String) isNull() ) ).thenThrow(
        new RuntimeException( "test load failure" ) );
    dswWizard.setTemplates( Arrays.asList( setupMockTemplate( templateID, templateDisplayName ) ) );
  }

  private String[][] setupMockTemplateList() {
    final String[][] templateDefinition =
        new String[][] { { "dummy1", "dummy1Name" },
          { "dummy2", "dummy2Name" }, { "SQL", "name_gets_replaced" } };
    ArrayList<IDSWTemplate> mockTemplateList = new ArrayList<IDSWTemplate>();
    for ( int i = 0; i < templateDefinition.length; i++ ) {
      mockTemplateList.add( setupMockTemplate( templateDefinition[ i ][ 0 ], templateDefinition[ i ][ 1 ] ) );
    }
    dswWizard.setTemplates( mockTemplateList );

    // modify the array to contain the expected results. The display name of the SQL entry
    // should be overwritten by the value in the message file
    templateDefinition[ 2 ][ 1 ] = "SQL Query";
    return templateDefinition;
  }

  private IDSWTemplate setupMockTemplate( String templateID, String templateDisplayName ) {
    IDSWTemplate mockTemplate = new MockDSWTemplate( templateID, templateDisplayName );
    return mockTemplate;
  }

}
