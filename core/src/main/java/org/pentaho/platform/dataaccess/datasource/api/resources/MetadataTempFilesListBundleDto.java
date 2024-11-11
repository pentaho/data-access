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
