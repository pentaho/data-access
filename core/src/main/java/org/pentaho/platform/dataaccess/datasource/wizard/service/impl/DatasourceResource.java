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

import static jakarta.ws.rs.core.MediaType.WILDCARD;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

  private static final Log logger = LogFactory.getLog( DatasourceResource.class );
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

    if ( catalog == null ) {
      logger.warn( "Catalog " + catalogId + " doesn't exist" );
      return Response.status( Response.Status.BAD_REQUEST ).build();
    }
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
