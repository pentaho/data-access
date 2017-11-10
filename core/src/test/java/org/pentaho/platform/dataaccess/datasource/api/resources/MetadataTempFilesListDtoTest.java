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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by Dmitriy Stepanov on 15/06/17.
 */
public class MetadataTempFilesListDtoTest {

  private String list = "{\"xmiFileName\":\"admin-4424584292793114069.tmp\"}";
  private String list2 =
      "{\"xmiFileName\":\"filename.tmp\",\"bundles\":[\"bundle-1-name.tmp\",\"bundle-N-name.tmp\"]}";

  @Test
  public void constructorParse() {
    try {
      MetadataTempFilesListDto dto = new MetadataTempFilesListDto( list );
      dto = new MetadataTempFilesListDto( list2 );
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testToJSONString() {
    try {
      MetadataTempFilesListDto dto = new MetadataTempFilesListDto( list );
      assertEquals( "admin-4424584292793114069.tmp", dto.getXmiFileName() );
      assertEquals( 0, dto.getBundles().size() );
      assertEquals( list, dto.toJSONString() );

      dto = new MetadataTempFilesListDto( list2 );
      assertEquals( "filename.tmp", dto.getXmiFileName() );
      assertEquals( 2, dto.getBundles().size() );
      assertEquals( "filename.tmp", dto.getBundles().get(0).getOriginalFileName() );
      assertEquals( "bundle-1-name.tmp", dto.getBundles().get(0).getTempFileName() );
      assertEquals( "filename.tmp", dto.getBundles().get(1).getOriginalFileName() );
      assertEquals( "bundle-N-name.tmp", dto.getBundles().get(1).getTempFileName() );
      assertEquals( list2, dto.toJSONString() );
    } catch ( Exception e ) {
      fail();
    }
  }
}