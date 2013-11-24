/**
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.dataaccess.datasource.provider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.api.catalog.IDatasource;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceProvider;

public class MetadataDatasourceProviderTest {

  private static final String METADATA_TYPE = new MetadataDatasourceType().getId();

  IMetadataDomainRepository metadataDomainRepository;

  /**
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    // do nothing
  }

  @After
  public void tearDown() throws Exception {
    // do nothing
  }

  @Test
  public void testDatasourceProvider() throws Exception {

    metadataDomainRepository = mock( IMetadataDomainRepository.class );

    Set<String> idSet = new HashSet<String>();
    idSet.add( "1" );
    idSet.add( "2" );
    idSet.add( "3" );

    // mock metadataDomainRepository.getDomainIds return
    doReturn( idSet ).when( metadataDomainRepository ).getDomainIds();

    //create MetadataDatasourceProvider
    IDatasourceProvider metadataDatasourceProvider = new MetadataDatasourceProvider( metadataDomainRepository );
    List<IDatasource> datasources = metadataDatasourceProvider.getDatasources();

    assertEquals( 3, datasources.size() );

    for ( IDatasource datasource : datasources ) {
      assertEquals( METADATA_TYPE, datasource.getType().getId() );
    }
  }
}
