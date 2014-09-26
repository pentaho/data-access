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
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataResource;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import java.io.InputStream;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

@Path( "/data-access/api/datasource/metadata" )
public class MetadataDatasourceService extends MetadataResource {

  public MetadataDatasourceService() {
    super();
  }


  /**
   * Get the Metadata datasource IDs
   *
   * @return JaxbList<String> of metadata IDs
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public JaxbList<String> getMetadataDatasourceIds() {
    return super.listDomains();
  }


  @GET
  @Path( "/{domainId : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Metadata datasource export succeeded." ),
      @ResponseCode( code = 401, condition = "User is not authorized to export Metadata datasource." ),
      @ResponseCode( code = 500, condition = "Failure to export Metadata datasource." )
  } )
  public Response doGetMetadataFilesAsDownload( @PathParam( "domainId" ) String domainId ) {
    return super.downloadMetadata( domainId );
  }


  @POST
  @Path( "/{domainId : .+}/remove" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Metadata datasource removed." ),
      @ResponseCode( code = 401, condition = "User is not authorized to delete the Metadata datasource." )
  } )
  public Response doRemoveMetadata( @PathParam( "domainId" ) String domainId ) {
    return super.deleteMetadata( domainId );
  }


  @PUT
  @Path( "/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Metadata datasource import succeeded. A response of:\n"
          + "   *  2: Unspecified general error has occurred\n"
          + "   *  3: Indicates successful import\n"
          + "   *  9: Content already exists (use overwrite flag to force)\n"
          + "   * 10: Import failed because publish is prohibited" ),
      @ResponseCode( code = 500,
          condition = "Metadata datasource import failed.  Error code or message included in response entity" )
  } )
  public Response importMetadataDatasource( @FormDataParam( "domainId" ) String domainId,
                                            @FormDataParam( "metadataFile" ) InputStream metadataFile,
                                            @FormDataParam( "metadataFile" ) FormDataContentDisposition metadataFileInfo,
                                            @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
                                            @FormDataParam( "localeFiles" ) List<FormDataBodyPart> localeFiles,
                                            @FormDataParam( "localeFiles" )
                                            List<FormDataContentDisposition> localeFilesInfo ) {
    return super.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo );
  }
}
