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
import org.pentaho.platform.dataaccess.catalog.impl.dto.DatasourceListDto;
import org.pentaho.platform.dataaccess.catalog.impl.dto.DatasourceTypesListDto;
import org.pentaho.platform.web.http.api.resources.JaxbList;

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

  public DatasourceCatalog( List<IDatasourceProvider> datasourceProviders ) {
    datasourceProviders.addAll( datasourceProviders );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog#getDatasources()
   */
  @Override
  public List<IDatasource> getDatasources() {
    List<IDatasource> values = new ArrayList<IDatasource>();

    for ( IDatasourceProvider provider : datasourceProviders ) {
      values.addAll( provider.getDatasources() );
    }

    return values;
  }

  /**
   * Returns a list of IDatasourceTypes that can be further used to lookup datasources of a specific type.
   * @return a JaxbList of IDatasourceType implementations (usually DatasourceType) suitable for serialization by Jersey
   */
  @GET
  @Path( "/getDatasources" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public DatasourceListDto getDatasourcesAsJaxbList() {
    List<IDatasource> values = getDatasources();
    if ( values != null ) {
      return new DatasourceListDto(values);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog#getDatasourcesOfType(org.pentaho.platform.dataaccess
   * .catalog.api.IDatasourceType)
   */
  @Override
  public List<IDatasource> getDatasourcesOfType( IDatasourceType datasourceType ) {
    for ( IDatasourceProvider provider : datasourceProviders ) {
      if ( provider.getType().getId().equals( datasourceType.getId() ) ) {
        return provider.getDatasources();
      }
    }
    return null;
  }

  /**
   * @param datasourceType
   *          - the type of the datasources to return (typically an instance of DatasourceType)
   * @return A JaxbList of concrete instances of the type 'IDatasourceType' (typically DatasourceType)
   */
  @POST
  @Path( "/getDatasourcesByType" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public DatasourceListDto getDatasourcesOfTypeAsJaxbList( IDatasourceType datasourceType ) {
    List<IDatasource> values = getDatasourcesOfType( datasourceType );
    if ( values != null ) {
      return new DatasourceListDto( values );
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog#getDatasourceTypes()
   */
  @Override
  public List<IDatasourceType> getDatasourceTypes() {
    List<IDatasourceType> values = new ArrayList<IDatasourceType>();

    for ( IDatasourceProvider provider : datasourceProviders ) {
      values.add( provider.getType() );
    }

    return values;
  }

  /**
   * @return A JaxbList of concrete instances of IDatasourceType (typically DatasourceType) suitable for serialization by Jersey
   */
  @GET
  @Path( "/getDatasourceTypes" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public DatasourceTypesListDto getDatasourceTypesAsJaxbList() {
    return new DatasourceTypesListDto( getDatasourceTypes() );
  }

  public void registerDatasourceProvider( IDatasourceProvider datasourceProvider ) {
    datasourceProviders.add( datasourceProvider );
  }

}
