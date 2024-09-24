/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
