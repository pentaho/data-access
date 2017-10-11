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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseType;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Reads and writes DatabaseType objects using flexjson so that they won't lose map values
 * when converting to/from autobeans
 */
@Provider
@Produces( APPLICATION_JSON )
public class DatabaseTypeReaderWriter implements MessageBodyReader<DatabaseType>, MessageBodyWriter<DatabaseType> {

  @Override
  public long getSize( DatabaseType t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return -1;
  }

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return IDatabaseType.class.isAssignableFrom( type );
  }

  @Override
  public void writeTo( DatabaseType t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream ) throws IOException, WebApplicationException {
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter( entityStream );
    try {
      new JSONSerializer().exclude( "*.class" ).serialize( t, outputStreamWriter );
    } finally {
      outputStreamWriter.close();
    }
  }

  @Override
  public boolean isReadable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return DatabaseType.class.isAssignableFrom( type );
  }

  @Override
  public DatabaseType readFrom( Class<DatabaseType> type, Type genericType, Annotation[] annotations,
                                MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream ) throws IOException,
    WebApplicationException {
    JSONDeserializer<DatabaseType> jsonD = new JSONDeserializer<DatabaseType>();
    return jsonD.deserialize( new InputStreamReader( entityStream ), DatabaseType.class );
  }

}
