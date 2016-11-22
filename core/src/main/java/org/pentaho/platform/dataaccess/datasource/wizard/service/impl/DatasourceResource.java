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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.WILDCARD;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.enunciate.Facet;
import org.pentaho.platform.dataaccess.datasource.api.resources.AnalysisResource;
import org.pentaho.platform.dataaccess.datasource.api.resources.DataSourceWizardResource;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataResource;
import org.pentaho.platform.dataaccess.datasource.utils.DataSourceInfoUtil;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import java.util.Map;

@Path( "/data-access/api/datasource" )
public class DatasourceResource {
  /**
   * Get the data source wizard info (parameters) for a specific data source wizard id
   * 
   * @param dswId
   *          String id for a data source wizard
   * 
   * @return Response containing the parameter list
   */
  @GET
  @Path( "/{catalogId : .+}/getAnalysisDatasourceInfo" )
  @Produces( WILDCARD )
  @Facet( name = "Unsupported" )
  public Response getAnalysisDatasourceInfo( @PathParam( "catalogId" ) String catalogId ) {
    IMondrianCatalogService mondrianCatalogService =
        PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
    MondrianCatalog catalog = mondrianCatalogService.getCatalog( catalogId, PentahoSessionHolder.getSession() );
    //dataSourceInfo can contain XML-escaped characters
    String parameters = prepareDataSourceInfo( catalog.getDataSourceInfo() );
    //after preparation, parameters have escaped only quotes
    return Response.ok().entity( parameters ).build();
  }

  static String prepareDataSourceInfo( String dataSourceInfo ) {
    StringBuilder sb = new StringBuilder();
    Map<String, String> parameters = DataSourceInfoUtil.parseDataSourceInfo( dataSourceInfo );
    parameters.forEach( ( key, value ) -> {
      String unescapedValue = StringEscapeUtils.unescapeXml( value );
      String valueWithEscapedQuotes = DataSourceInfoUtil.escapeQuotes( unescapedValue );
      sb.append( key );
      sb.append( "=\"" );
      sb.append( valueWithEscapedQuotes );
      sb.append( "\"" );
      sb.append( ";" );
    } );
    return sb.toString();
  }

  /**
   * Get list of IDs of analysis datasource
   *
   * @return JaxbList<String> of analysis IDs
   */
  public JaxbList<String> getAnalysisDatasourceIds() {
    return new AnalysisResource().getSchemaIds();
  }

  /**
   * Get the Metadata datasource IDs
   * 
   * @return JaxbList<String> of metadata IDs
   */
  public JaxbList<String> getMetadataDatasourceIds() {
    return new MetadataResource().listDomains();
  }

  /**
   * Download the metadata files for a given metadataId
   * 
   * @param metadataId
   *          String Id of the metadata to retrieve
   * 
   * @return Response containing the file data
   */
  @Facet( name = "Unsupported" )
  public Response doGetMetadataFilesAsDownload( @PathParam( "metadataId" ) String metadataId ) {
    return new MetadataResource().downloadMetadata( metadataId );
  }

  /**
   * Download the analysis files for a given analysis id
   * 
   * @param analysisId
   *          String Id of the analysis data to retrieve
   * 
   * @return Response containing the file data
   */
  @Facet( name = "Unsupported" )
  public Response doGetAnalysisFilesAsDownload( @PathParam( "analysisId" ) String analysisId ) {
    return new AnalysisResource().downloadSchema( analysisId );
  }
  /**
   * Remove the analysis data for a given analysis ID
   * 
   * @param analysisId
   *          String ID of the analysis data to remove
   * 
   * @return Response ok if successful
   */
  @Facet( name = "Unsupported" )
  public Response doRemoveAnalysis( @PathParam( "analysisId" ) String analysisId ) {
    return new AnalysisResource().downloadSchema( analysisId );
  }

  /**
   * Remove the datasource wizard data for a given datasource wizard ID
   * 
   * @param dswId
   *          String ID of the datasource wizard data to remove
   * 
   * @return Response ok if successful
   */
  @Facet( name = "Unsupported" )
  public Response doRemoveDSW( @PathParam( "dswId" ) String dswId ) {
    return new DataSourceWizardResource().remove( dswId );
  }
}
