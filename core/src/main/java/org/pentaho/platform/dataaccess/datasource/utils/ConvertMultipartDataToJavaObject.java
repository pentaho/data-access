/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.utils;

import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;

public class ConvertMultipartDataToJavaObject {

  public static <T> T jsonMultipartDataToJava( FormDataBodyPart jsonPart, Class<T> clazz ) {
    if ( jsonPart == null )
      return null;

    jsonPart.setMediaType( MediaType.APPLICATION_JSON_TYPE );
    return jsonPart.getValueAs( clazz );
  }

}
