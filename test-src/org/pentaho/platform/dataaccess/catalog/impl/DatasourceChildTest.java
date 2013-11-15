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

package org.pentaho.platform.dataaccess.catalog.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasourceChildTest {
  DatasourceChild datasourceChild;

  @Before
  public void setUp() throws Exception {
    datasourceChild = new DatasourceChild(  );
    datasourceChild.setId( "1" );
    datasourceChild.setName( "testDatasource1" );

    DatasourceChild datasourceChild2 = new DatasourceChild(  );
    datasourceChild2.setId( "2" );
    datasourceChild2.setName( "testDatasource2" );

    DatasourceChild datasourceChild3 = new DatasourceChild(  );
    datasourceChild3.setId( "3" );
    datasourceChild3.setName( "testDatasource3" );

    List<DatasourceChild> datasourceChildList = new ArrayList<DatasourceChild>(
      Arrays.asList( datasourceChild2, datasourceChild3 ) );

    datasourceChild.setChildren( datasourceChildList );
  }

  @Test
  public void testGetChildren() throws Exception {
    assert ( datasourceChild.getChildren().size() == 2 );
  }
}
