/*
* Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
* 
* This software was developed by Hitachi Vantara and is provided under the terms
* of the Mozilla Public License, Version 1.1, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionPoolParameterList;
import org.pentaho.ui.database.event.DefaultDatabaseDialectList;
import org.pentaho.ui.database.event.DefaultDatabaseTypesList;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

/**
 * @author wseyler
 */
@Provider
public class PentahoJAXBContextResolver implements ContextResolver<JAXBContext> {
  private JAXBContext context;
  private Class[] types = { DatabaseConnection.class,
    DefaultDatabaseConnectionList.class,
    DefaultDatabaseDialectList.class,
    DatabaseType.class,
    DefaultDatabaseTypesList.class,
    DefaultDatabaseConnectionPoolParameterList.class };

  public PentahoJAXBContextResolver() throws JAXBException {
    this.context = new JSONJAXBContext( JSONConfiguration.natural().build(), types );
  }

  /* (non-Javadoc)
   * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
   */
  @Override
  public JAXBContext getContext( Class<?> objectType ) {
    for ( Class c : types ) {
      if ( c.equals( objectType ) ) {
        return ( context );
      }
    }
    return ( null );
  }
}
