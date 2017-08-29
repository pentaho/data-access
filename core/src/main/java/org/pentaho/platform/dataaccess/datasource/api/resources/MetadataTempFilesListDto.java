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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitriy Stepanov on 12/06/17.
 */

@XmlRootElement
public class MetadataTempFilesListDto implements Serializable {
  private static final long serialVersionUID = 595741423349391476L;
  String xmiFileName;
  List<MetadataTempFilesListBundleDto> bundles = new ArrayList<MetadataTempFilesListBundleDto>( 0 );
  String id;

  public MetadataTempFilesListDto() {
    super();
  }

  public MetadataTempFilesListDto( String fileList ) {
    this();
    JSONObject jsonResponse = null;
    try {
      jsonResponse = new JSONObject( fileList );


      xmiFileName = jsonResponse.getString( "xmiFileName" );
      JSONArray jsonBundles = null;
      try {
        jsonBundles = jsonResponse.getJSONArray( "bundles" );
      } catch ( JSONException e ) {
        // ignored
      }

      if ( jsonBundles != null ) {
        for ( int i = 0; i <= jsonBundles.length(); i++ ) {
          bundles.add( new MetadataTempFilesListBundleDto( (String) jsonBundles.get( i ), xmiFileName ) );
        }
      }
    } catch ( JSONException e ) {
      e.printStackTrace();
    }

  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "MetadataTempFilesListDto [id=" + id + ", xmiFileName=" + xmiFileName + ", bundles=" + bundles + "]";
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
