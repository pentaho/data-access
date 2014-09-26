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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.resources.AnalysisResource;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.*;

@Path( "/data-access/api/datasource/analysis" )
public class AnalysisDatasourceService extends AnalysisResource {

  public AnalysisDatasourceService() {
    super();
  }

  /**
   * Get list of IDs of analysis datasource
   *
   * @return JaxbList<String> of analysis IDs
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public JaxbList<String> getAnalysisDatasourceIds() {
    return new AnalysisResource().getSchemaIds();
  }


  @GET
  @Path( "/{catalog : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully downloaded the analysis file" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 500, condition = "Unabled to download analysis file" )
  } )
  public Response doGetAnalysisFilesAsDownload( @PathParam( "catalog" ) String catalog ) {
    return new AnalysisResource().downloadSchema( catalog );
  }


  @PUT
  @Path( "/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @StatusCodes( {
      @ResponseCode( code = 200,
          condition = "Status code indicating a success or failure while importing Mondrian schema XML. A response of:\n"
              + "   *  2: Unspecified general error has occurred\n"
              + "   *  3: Success\n"
              + "   *  5: Authorization error" )
  } )
  public Response putMondrianSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream uploadAnalysis,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_ID ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters )
      throws PentahoAccessControlException {
    return super.importMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName, overwrite, xmlaEnabledFlag, parameters );
  }




  @POST
  @Path( "/{catalog : .+}/remove" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully removed the analysis data" ),
      @ResponseCode( code = 401, condition = "User is not authorized to delete the analysis datasource" ),
      @ResponseCode( code = 500, condition = "Unable to remove the analysis data." )
  } )
  public Response doRemoveAnalysis( @PathParam( "catalog" ) String catalog ) {
    try {
      service.removeAnalysis( catalog );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    }
  }

}
