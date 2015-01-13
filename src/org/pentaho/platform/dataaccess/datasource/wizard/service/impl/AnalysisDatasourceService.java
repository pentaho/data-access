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
* Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;

@Path( "/data-access/api/mondrian" )
public class AnalysisDatasourceService {

  private static final Log logger = LogFactory.getLog( AnalysisDatasourceService.class );

  private static final String XMLA_ENABLED_FLAG = "xmlaEnabledFlag";
  private static final String CATALOG_NAME = "catalogName";
  private static final String ORIG_CATALOG_NAME = "origCatalogName";
  private static final String DATASOURCE_NAME = "datasourceName";
  private static final String UPLOAD_ANALYSIS = "uploadAnalysis";
  private static final String PARAMETERS = "parameters";
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final String DATASOURCE_ACL = "acl";
  private static final int SUCCESS = 3;

  /**
   * This is used by PUC to use a Jersey put to import a Mondrian Schema XML into PUR
   * 
   * @param dataInputStream
   * @param schemaFileInfo
   * @param catalogName
   * @param datasourceName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @param acl acl information for the data source. This parameter is optional.
   * @return this method returns a response of "3" for success, 8 if exists, etc.
   * @throws PentahoAccessControlException
   */
  @PUT
  @Path( "/putSchema" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @Facet( name = "Unsupported" )
  public Response putMondrianSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream dataInputStream,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_NAME ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl )
    throws PentahoAccessControlException {
    Response response = null;
    int statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    try {
      AnalysisService service = new AnalysisService();
      boolean overWriteInRepository = "True".equalsIgnoreCase( overwrite ) ? true : false;
      boolean xmlaEnabled = "True".equalsIgnoreCase( xmlaEnabledFlag ) ? true : false;
      service.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, datasourceName,
          overWriteInRepository, xmlaEnabled, parameters, acl );
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

  /**
   * This is used by PUC to use a form post to import a Mondrian Schema XML into PUR
   * 
   * @param dataInputStream
   * @param schemaFileInfo
   * @param catalogName
   * @param datasourceName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @param acl acl information for the data source. This parameter is optional.
   * @return this method returns a response of "SUCCESS" for success, 8 if exists, 2 for general error,etc.
   * @throws PentahoAccessControlException
   */
  @POST
  @Path( "/postAnalysis" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( { "text/plain", "text/html" } )
  @Facet( name = "Unsupported" )
  public Response postMondrainSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream dataInputStream,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_NAME ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl )
    throws PentahoAccessControlException {
    // use existing Jersey post method - but translate into text/html for PUC Client
    ResponseBuilder responseBuilder;
    Response response =
        this.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, datasourceName,
            overwrite, xmlaEnabledFlag, parameters, acl );
    responseBuilder = Response.ok();
    responseBuilder.entity( String.valueOf( response.getStatus() ) );
    responseBuilder.status( 200 );
    return responseBuilder.build();
  }
}
