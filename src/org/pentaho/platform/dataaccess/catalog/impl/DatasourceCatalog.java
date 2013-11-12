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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.catalog.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAnyElement;

import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;

/**
 * @author wseyler
 * 
 */
@Path( "/data-access-v2/api/datasourceCatalog" )
public class DatasourceCatalog implements IDatasourceCatalog {

  @XmlAnyElement
  private List<IDatasourceProvider> datasourceProviders = new ArrayList<IDatasourceProvider>();;

  public DatasourceCatalog() {
    super();
  }


  public DatasourceCatalog(List<IDatasourceProvider> datasourceProviders) {
    datasourceProviders.addAll( datasourceProviders );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog#getDatasources()
   */
  @Override
  @GET
  @Path( "/getDatasources" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<IDatasource> getDatasources() {
    List<IDatasource> values = new ArrayList<IDatasource>();

    for ( IDatasourceProvider provider : datasourceProviders ) {
      values.addAll( provider.getDatasources() );
    }

    return values;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog#getDatasourcesOfType(org.pentaho.platform.dataaccess
   * .catalog.api.IDatasourceType)
   */
  @Override
  @POST
  @Path( "/getDatasourcesByType" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<IDatasource> getDatasourcesOfType( IDatasourceType datasourceType ) {
    for ( IDatasourceProvider provider : datasourceProviders ) {
      if ( provider.getType().getId().equals( datasourceType.getId() ) ) {
        return provider.getDatasources();
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog#getDatasourceTypes()
   */
  @Override
  @GET
  @Path( "/getDatasourceTypes" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<IDatasourceType> getDatasourceTypes() {
    List<IDatasourceType> values = new ArrayList<IDatasourceType>();

    for ( IDatasourceProvider provider : datasourceProviders ) {
      values.add( provider.getType() );
    }

    return values;
  }

  public void registerDatasourceProvider( IDatasourceProvider datasourceProvider ) {
    datasourceProviders.add( datasourceProvider );
  }

}
