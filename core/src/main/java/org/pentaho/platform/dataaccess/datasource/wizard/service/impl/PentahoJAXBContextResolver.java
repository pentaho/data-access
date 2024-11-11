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
