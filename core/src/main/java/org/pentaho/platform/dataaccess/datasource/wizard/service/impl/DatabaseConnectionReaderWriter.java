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


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Reads and writes Database Connection objects using flexjson so that they won't lose map values when converting
 * to/from autobeans
 */
@Provider
@Produces( APPLICATION_JSON )
public class DatabaseConnectionReaderWriter
  implements MessageBodyReader<DatabaseConnection>, MessageBodyWriter<DatabaseConnection> {

  @Override
  public long getSize( DatabaseConnection t, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType ) {
    return -1;
  }

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return IDatabaseConnection.class.isAssignableFrom( type );
  }

  @Override
  public void writeTo( DatabaseConnection t, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
    throws IOException, WebApplicationException {
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter( entityStream );
    try {
      new JSONSerializer().exclude( "*.class" ).serialize( t, outputStreamWriter );
    } finally {
      outputStreamWriter.close();
    }
  }

  @Override
  public boolean isReadable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return IDatabaseConnection.class.isAssignableFrom( type );
  }

  @Override
  public DatabaseConnection readFrom( Class<DatabaseConnection> type, Type genericType, Annotation[] annotations,
                                      MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                      InputStream entityStream ) throws IOException,
    WebApplicationException {
    JSONDeserializer<DatabaseConnection> jsonD = new JSONDeserializer<DatabaseConnection>();
    jsonD.use( "databaseType", DatabaseType.class );
    return jsonD.deserialize( new InputStreamReader( entityStream ), DatabaseConnection.class );
  }

}
