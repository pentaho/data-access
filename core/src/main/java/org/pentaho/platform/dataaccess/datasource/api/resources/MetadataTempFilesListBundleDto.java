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

import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by dstepanov on 12/06/17.
 */
@XmlRootElement
public class MetadataTempFilesListBundleDto implements Serializable {
  private static final long serialVersionUID = 4526978475946122862L;

  private static final String ORIG_NAME = "origName";
  private static final String TEMP_NAME = "tempName";
  private final String originalFileName;
  private final String tempFileName;

  public MetadataTempFilesListBundleDto( String localFileName, String fileName ) {
    this.originalFileName = localFileName;
    this.tempFileName = fileName;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public String getTempFileName() {
    return tempFileName;
  }

  JSONObject toJson() {
    Map<String, String> attrs = new HashMap<>();
    attrs.put( ORIG_NAME, originalFileName );
    attrs.put( TEMP_NAME, tempFileName );
    return new JSONObject( attrs );
  }

  static MetadataTempFilesListBundleDto fromJson( JSONObject json ) {
    try {
      return new MetadataTempFilesListBundleDto(
        json.getString( ORIG_NAME ), json.getString( TEMP_NAME ) );
    } catch ( JSONException e ) {
      throw new IllegalStateException( e );
    }
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    MetadataTempFilesListBundleDto that = (MetadataTempFilesListBundleDto) o;
    return Objects.equals( originalFileName, that.originalFileName )
      && Objects.equals( tempFileName, that.tempFileName );
  }

  @Override public int hashCode() {
    return Objects.hash( originalFileName, tempFileName );
  }
}
