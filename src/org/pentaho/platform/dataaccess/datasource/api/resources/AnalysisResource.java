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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path( "/data-access/api/datasource/analysis" )
public class AnalysisResource {

  private static final String UPLOAD_ANALYSIS = "uploadAnalysis";
  private static final String CATALOG_NAME = "catalogName";
  private static final String ORIG_CATALOG_NAME = "origCatalogName";
  private static final String DATASOURCE_NAME = "datasourceName";
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final String XMLA_ENABLED_FLAG = "xmlaEnabledFlag";
  private static final String PARAMETERS = "parameters";
  private static final int SUCCESS = 3;
  private static final Log logger = LogFactory.getLog( AnalysisResource.class );

  private AnalysisService service;

  public AnalysisResource() {
    service = new AnalysisService();
  }

  /**
   * Remove the analysis data for a given analysis ID
   *
   * @param analysisId
   *          String ID of the analysis data to remove
   *
   * @return Response ok if successful
   */
  @POST
  @Path( "/{analysisId : .+}/remove" )
  @Produces( WILDCARD )
  public Response doRemoveAnalysis( @PathParam( "analysisId" ) String analysisId ) {
    try {
      service.removeAnalysis( analysisId );
      return Response.ok().build();
    } catch ( PentahoAccessControlException e ) {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Get list of IDs of analysis datasource
   *
   * @return JaxbList<String> of analysis IDs
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public JaxbList<String> getAnalysisDatasourceIds() {
    return new JaxbList<String>( service.getAnalysisDatasourceIds() );
  }

  /**
   * This is used by PUC to use a Jersey put to import a Mondrian Schema XML into PUR
   * 
   * @author: tband date: 7/10/12
   * @param dataInputStream
   * @param schemaFileInfo
   * @param catalogName
   * @param datasourceName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @return this method returns a response of "3" for success, 8 if exists, etc.
   * @throws PentahoAccessControlException
   */
  @PUT
  @Path( "/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  public Response putMondrianSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream dataInputStream,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_NAME ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters )
    throws PentahoAccessControlException {
    Response response = null;
    int statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    try {
      service.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, datasourceName,
          overwrite, xmlaEnabledFlag, parameters );
      statusCode = SUCCESS;
    } catch ( PentahoAccessControlException pac ) {
      logger.error( pac.getMessage() );
      statusCode = PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL;
    } catch ( PlatformImportException pe ) {
      statusCode = pe.getErrorStatus();
      logger.error( "Error putMondrianSchema " + pe.getMessage() + " status = " + statusCode );
    } catch ( Exception e ) {
      logger.error( "Error putMondrianSchema " + e.getMessage() );
      statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    }

    response = Response.ok().status( statusCode ).type( MediaType.TEXT_PLAIN ).build();
    logger.debug( "putMondrianSchema Response " + response );
    return response;
  }
}
