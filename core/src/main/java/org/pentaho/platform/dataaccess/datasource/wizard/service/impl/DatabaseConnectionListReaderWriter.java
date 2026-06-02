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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionList;

/**
 * Reads and writes Database Connection objects using Jackson so that they won't lose map values when converting
 * to/from autobeans
 */
@Provider
@Produces( APPLICATION_JSON )
public class DatabaseConnectionListReaderWriter
  implements MessageBodyReader<IDatabaseConnectionList>, MessageBodyWriter<IDatabaseConnectionList> {

  private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = JacksonObjectMapperUtil.createObjectMapper();
    SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
    resolver.addMapping( IDatabaseConnection.class, DatabaseConnection.class );
    resolver.addMapping( IDatabaseType.class, DatabaseType.class );
    SimpleModule module = new SimpleModule();
    module.setAbstractTypes( resolver );
    mapper.registerModule( module );
    return mapper;
  }

  @Override
  public long getSize( IDatabaseConnectionList t, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType ) {
    return -1;
  }

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return IDatabaseConnectionList.class.isAssignableFrom( type );
  }

  @Override
  public void writeTo( IDatabaseConnectionList t, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
    throws IOException, WebApplicationException {
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter( entityStream );
    try {
      OBJECT_MAPPER.writeValue( outputStreamWriter, t );
    } finally {
      outputStreamWriter.close();
    }
  }

  @Override
  public boolean isReadable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return IDatabaseConnectionList.class.isAssignableFrom( type );
  }

  @Override
  public IDatabaseConnectionList readFrom( Class<IDatabaseConnectionList> type, Type genericType,
                                           Annotation[] annotations,
                                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                           InputStream entityStream ) throws IOException,
    WebApplicationException {
    return OBJECT_MAPPER.readValue( new InputStreamReader( entityStream ), DefaultDatabaseConnectionList.class );
  }
}
