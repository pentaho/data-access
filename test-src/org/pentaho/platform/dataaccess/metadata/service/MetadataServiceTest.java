/**
 * 
 */
package org.pentaho.platform.dataaccess.metadata.service;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
/**
 * @author wseyler
 *
 */
public class MetadataServiceTest {
  MetadataService service;
  static PentahoSystemBoot microPlatform;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    microPlatform = new PentahoSystemBoot();
//    IMetadataDomainRepository mockMetadataDomainRepository = mock(IMetadataDomainRepository);
//    microPlatform.define( IMetadataDomainRepository.class, mockMetadataDomainRepository.class);
  }
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    service = new MetadataService();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#getDatasourcePermissions()}.
   */
  @Test
  public void testGetDatasourcePermissions() {
    String permission = service.getDatasourcePermissions();
    assertEquals( "EDIT", permission);
  }

  /**
   * Test method for {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#listBusinessModels(java.lang.String)}.
   */
  @Test
  public void testListBusinessModels() {
    fail( "Not yet implemented" );
  }

  /**
   * Test method for {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#loadModel(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testLoadModel() {
    fail( "Not yet implemented" );
  }

  /**
   * Test method for {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#doQuery(Query, Integer)}.
   */
  @Test
  public void testDoQuery() {
    fail( "Not yet implemented" );
  }

}
