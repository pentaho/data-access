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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dmitriy Stepanov on 12/06/17.
 */

@XmlRootElement
public class MetadataTempFilesListDto implements Serializable {
  private static final long serialVersionUID = 595741423349391476L;
  private static final String BUNDLES_KEY = "bundles";
  String xmiFileName;
  private List<MetadataTempFilesListBundleDto> bundles = new ArrayList<>( 0 );
  String id;

  public MetadataTempFilesListDto() {
    super();
  }


  public MetadataTempFilesListDto( String fileListJson ) {
    this();
    try {
      JSONObject jsonResponse = new JSONObject( fileListJson );
      xmiFileName = jsonResponse.getString( "xmiFileName" );

      if ( jsonResponse.has( BUNDLES_KEY ) ) {
        JSONArray jsonBundles = jsonResponse.getJSONArray( BUNDLES_KEY );
        for ( int i = 0; i < jsonBundles.length(); i++ ) {
          bundles.add( MetadataTempFilesListBundleDto.fromJson( (JSONObject) jsonBundles.get( i ) ) );
        }
      }
    } catch ( JSONException e ) {
      throw new IllegalStateException( e );
    }

  }

  @Override
  public String toString() {
    return toJSONString();
  }

  public String toJSONString() {
    JSONObject obj = new JSONObject();
    try {
      if ( xmiFileName != null ) {
        obj.put( "xmiFileName", xmiFileName );
      }
      if ( !bundles.isEmpty() ) {
        obj.put( BUNDLES_KEY, new JSONArray(
          bundles.stream()
            .map( MetadataTempFilesListBundleDto::toJson )
            .collect( Collectors.toList() ) ) );
      }
    } catch ( JSONException e ) {
      throw new IllegalStateException( e );
    }
    return obj.toString();
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public void setXmiFileName( String xmiFileName ) {
    this.xmiFileName = xmiFileName;
  }

  public String getXmiFileName() {
    return xmiFileName;
  }

  public void setBundles( List<MetadataTempFilesListBundleDto> bundles ) {
    this.bundles = bundles;
  }

  public List<MetadataTempFilesListBundleDto> getBundles() {
    return bundles;
  }

}
