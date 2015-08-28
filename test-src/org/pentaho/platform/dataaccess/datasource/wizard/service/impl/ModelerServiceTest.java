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
* Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ModelerServiceTest extends DataAccessServiceTestBase {
  private final String TEST_CATALOG_NAME = "TEST_CATALOG_NAME";

  @Test
  public void testRemoveCatalogDuringSerializeModels() throws Exception {
    when( policy.isAllowed( anyString() ) ).thenReturn( Boolean.TRUE );
    when( pluginResourceLoader.getPluginSetting( (Class) anyObject(), anyString(), anyString() ) )
        .thenReturn( SimpleDataAccessPermissionHandler.class.getName() );
    MondrianCatalog mockCatalog = mock( MondrianCatalog.class );
    when( mondrianCatalogService.getCatalog( anyString(), (IPentahoSession) anyObject() ) ).thenReturn( mockCatalog );

    modelerService.serializeModels( domain, TEST_CATALOG_NAME );

    // verify removeCatalog is called
    verify( mondrianCatalogService, times( 1 ) ).removeCatalog( anyString(), (IPentahoSession) anyObject() );
  }
}
