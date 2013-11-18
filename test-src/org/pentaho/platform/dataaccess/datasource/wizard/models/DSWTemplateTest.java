package org.pentaho.platform.dataaccess.datasource.wizard.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.api.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplate;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplateModel;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class DSWTemplateTest {
  final String TEMPLATE_ID = "templateID";
  
  private static MicroPlatform mp = new MicroPlatform();
  private static IMetadataDomainRepository mockMetadataDomainRepository;
  IDSWTemplate iDSWTemplate = new MockDSWTemplate( TEMPLATE_ID, "DefaultDisplayName" );
  IDSWTemplateModel iDSWTemplateModel = new MockDSWTemplateModel( TEMPLATE_ID );

  @BeforeClass
  public static void beforeClass() throws Exception {
    mockMetadataDomainRepository = mock( PentahoMetadataDomainRepository.class );
    mp.defineInstance( IMetadataDomainRepository.class, mockMetadataDomainRepository );
  }

  @Test
  public void testDeserialize() throws Exception {
    IDSWTemplateModel iDSWTemplateModel = iDSWTemplate.deserialize( "blabla" );
    assertEquals( TEMPLATE_ID, iDSWTemplateModel.getTemplateID() );

    try {
      iDSWTemplate.deserialize( null );
      fail( "Did not throw exception" );
    } catch ( DSWException e ) {
      // Should throw this exception
    }
  }

  @Test
  public void TestSerialize() throws Exception {
    String serializedValue = iDSWTemplate.serialize( iDSWTemplateModel );
    assertEquals( "serialized " + ( (MockDSWTemplateModel) iDSWTemplateModel ).getMockData(), serializedValue );

    try {
      iDSWTemplate.serialize( null );
      fail( "Did not throw exception" );
    } catch ( DSWException e ) {
      // Should throw this exception
    }
  }

  @Test
  public void TestCreateDatasource() throws Exception {
    // See DSWDataSourceWizardTest.testStoreDatasource()
  }
}
