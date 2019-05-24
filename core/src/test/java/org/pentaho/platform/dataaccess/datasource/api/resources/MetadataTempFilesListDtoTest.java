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
 * Copyright (c) 2002-2019 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Dmitriy Stepanov on 15/06/17.
 */
public class MetadataTempFilesListDtoTest {


  @Test public void testRoundTrip() throws JSONException {
    JSONArray bundles = new JSONArray( Arrays.asList(
      new MetadataTempFilesListBundleDto(
        "messages_ja.properties", "sometempname.tmp" ).toJson(),
      new MetadataTempFilesListBundleDto(
        "messages_en.properties", "sometempname2.tmp" ).toJson() ) );
    JSONObject fileListJson = new JSONObject( ImmutableMap.of( "xmiFileName", "theXmiFileName",
      "bundles", bundles ) );

    MetadataTempFilesListDto fileList = new MetadataTempFilesListDto( fileListJson.toString() );

    JSONObject jsonFromFilesList = new JSONObject( fileList.toJSONString() );
    assertEquals( jsonFromFilesList.get( "xmiFileName" ), fileListJson.get( "xmiFileName" ) );

    MetadataTempFilesListDto rehydrated = new MetadataTempFilesListDto( fileList.toJSONString() );

    assertTrue( eq( fileList, rehydrated ) );
    assertEquals( "messages_ja.properties", fileList.getBundles().get( 0 ).getOriginalFileName() );
    assertEquals( "sometempname.tmp", fileList.getBundles().get( 0 ).getTempFileName() );

    assertEquals( "messages_en.properties", fileList.getBundles().get( 1 ).getOriginalFileName() );
    assertEquals( "sometempname2.tmp", fileList.getBundles().get( 1 ).getTempFileName() );
  }

  @Test public void testBundleRoundTrip() {
    MetadataTempFilesListBundleDto bundle = new MetadataTempFilesListBundleDto(
      "localeName", "fileName" );
    assertEquals( MetadataTempFilesListBundleDto.fromJson( bundle.toJson() ), bundle );
  }


  private boolean eq( MetadataTempFilesListDto thiz, MetadataTempFilesListDto that ) {
    return Objects.equals( thiz.xmiFileName, that.xmiFileName )
      && Objects.equals( thiz.getBundles(), that.getBundles() );
  }

}
