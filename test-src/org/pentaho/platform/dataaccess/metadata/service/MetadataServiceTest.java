/*
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
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
    // IMetadataDomainRepository mockMetadataDomainRepository = mock(IMetadataDomainRepository);
    // microPlatform.define( IMetadataDomainRepository.class, mockMetadataDomainRepository.class);
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
   * Test method for {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#getDatasourcePermissions()}
   * .
   */
  @Test
  public void testGetDatasourcePermissions() {
    String permission = service.getDatasourcePermissions();
    assertEquals( "EDIT", permission );
  }

  /**
   * Test method for
   * {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#listBusinessModels(java.lang.String)}.
   */
  @Test
  public void testListBusinessModels() {
    fail( "Not yet implemented" );
  }

  /**
   * Test method for
   * {@link org.pentaho.platform.dataaccess.metadata.service.MetadataService#loadModel(java.lang.String, java.lang.String)}
   * .
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
