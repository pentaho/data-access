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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.web.http.api.resources.JaxbList;

@Path( "/data-access/api" )
public class DataSourceWizardResource {

  private static final String MONDRIAN_CATALOG_REF = "MondrianCatalogRef"; //$NON-NLS-1$
  private DataSourceWizardService service;
  
  private IMetadataDomainRepository metadataDomainRepository;

  public DataSourceWizardResource() {
    service = new DataSourceWizardService();
    metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
  }

  /**
   * Download the data source wizard data for a given data source wizard ID
   *
   * @param dswId String Id of the data source wizard data to retrieve
   *
   * @return Response containing the file data
   */
  @GET
  @Path( "/datasource/dsw/{dswId : .+}/download" )
  @Produces(WILDCARD)
  public Response doGetDSWFilesAsDownload( @PathParam( "dswId" ) String dswId ) {
    if ( !DatasourceService.canAdminister() ) {
      return Response.status (UNAUTHORIZED ).build();
    }
    // First get the metadata files;
    Map<String, InputStream> fileData = ( (IPentahoMetadataDomainRepositoryExporter) metadataDomainRepository ).getDomainFilesData( dswId ); 
  
    // Then get the corresponding mondrian files
    Domain domain = metadataDomainRepository.getDomain( dswId );
    ModelerWorkspace model = new ModelerWorkspace( new GwtModelerWorkspaceHelper() );
    model.setDomain( domain );
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( logicalModel == null ) {
      logicalModel = model.getLogicalModel( ModelerPerspective.REPORTING );
    }
    if ( logicalModel.getProperty( MONDRIAN_CATALOG_REF ) != null) {
      MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper( PentahoSystem.get( IUnifiedRepository.class ) );
      String catalogRef = (String) logicalModel.getProperty( MONDRIAN_CATALOG_REF );
      fileData.putAll( helper.getModrianSchemaFiles( catalogRef ) );
      ResourceUtil.parseMondrianSchemaName( dswId, fileData );
    }

    return ResourceUtil.createAttachment( fileData, dswId );
  }  
  
  /**
   * Remove the datasource wizard data for a given datasource wizard ID
   *
   * @param dswId
   *          String ID of the datasource wizard data to remove
   *
   * @return Response ok if successful
   */
  @POST
  @Path( "/datasource/dsw/{dswId : .+}/remove" )
  @Produces( WILDCARD )
  public Response doRemoveDSW( @PathParam( "dswId" ) String dswId ) {
    try {
      service.removeDSW( dswId );
      return Response.ok().build();
    } catch ( PentahoAccessControlException e ) {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Returns a list of datasource IDs from datasource wizard
   *
   * @return JaxbList<String> list of datasource IDs
   */
  @GET
  @Path( "/datasource/dsw/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public JaxbList<String> getDSWDatasourceIds() {
    return new JaxbList<String>( service.getDSWDatasourceIds() );
  }
}
