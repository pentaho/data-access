/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created May 09, 2013
 * @author Bryan Rosander
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionList;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Reads and writes Database Connection objects using flexjson so that they won't lose map values
 * when converting to/from autobeans
 */
@Provider
@Produces(APPLICATION_JSON)
public class DatabaseConnectionListReaderWriter implements MessageBodyReader<IDatabaseConnectionList>, MessageBodyWriter<IDatabaseConnectionList> {

  @Override
  public long getSize(IDatabaseConnectionList t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return IDatabaseConnectionList.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(IDatabaseConnectionList t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(entityStream);
    try {
      new JSONSerializer().exclude("*.class").deepSerialize(t, outputStreamWriter);
    } finally {
      outputStreamWriter.close();
    }
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return IDatabaseConnection.class.isAssignableFrom(type);
  }

  @Override
  public IDatabaseConnectionList readFrom(Class<IDatabaseConnectionList> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
      WebApplicationException {
    JSONDeserializer<DefaultDatabaseConnectionList> jsonD = new JSONDeserializer<DefaultDatabaseConnectionList>();
    jsonD.use("databaseType", DatabaseType.class);
    return jsonD.deserialize(new InputStreamReader(entityStream), DefaultDatabaseConnectionList.class);
  }
}
