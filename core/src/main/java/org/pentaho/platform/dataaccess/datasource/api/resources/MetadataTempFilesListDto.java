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
