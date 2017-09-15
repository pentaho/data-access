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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import com.sun.jersey.multipart.FormDataParam;

/**
 * This service allows for listing, download, and removal of DSW data sources in the BA Platform.
 */
@Path( "/data-access/api/datasource/dsw" )
public class DataSourceWizardResource {
  private static final String DATASOURCE_ACL = "acl";

  protected DataSourceWizardService service;
  protected ResourceUtil resourceUtil;

  public DataSourceWizardResource() {
    service = createDataSourceWizardService();
    resourceUtil = new ResourceUtil();
  }

  protected DataSourceWizardService createDataSourceWizardService() {
    return new DataSourceWizardService();
  }

  /**
   * Export the DSW data source for the given DSW ID.  The response will be zipped if there is
   * more than one file.  The response will contain an XMI and/or a mondrian cube definition file.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/dsw/domain/jmeter-dsw-pentaho-test.xmi
   * </p>
   *
   * @param dswId The id of the DSW datasource to export
   *
   * @return A Response object containing the encrypted DSW data source files.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *          An encrypted .XMI file or a .zip with encoded .XMI files
   *    </pre>
   */
  @GET
  @Path( "/domain/{dswId : .+}" )
  @Produces( WILDCARD )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "DSW datasource export succeeded." ),
    @ResponseCode( code = 401, condition = "User is not authorized to export DSW datasource." ),
    @ResponseCode( code = 500, condition = "Failure to export DSW datasource." )
  } )
  public Response downloadDsw( @PathParam("dswId") String dswId ) {
    try {
      Map<String, InputStream> fileData = service.doGetDSWFilesAsDownload( dswId );
      return createAttachment( fileData, dswId );
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    }
  }

  /**
   * Remove the DSW data source for a given DSW ID.
   *
   * <p><b>Example Request:</b><br />
   *    DELETE pentaho/plugin/data-access/api/datasource/dsw/domain/jmeter-dsw-pentaho-test.xmi/remove
   *
   * @param dswId The id of the DSW datasource to remove
   *
   * @return A 200 response code representing the successful removal of the DSW datasource.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      This response does not contain data.
   *    </pre>
   */
  @DELETE
  @Path( "/domain/{dswId : .+}" )
  @Produces( WILDCARD )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "DSW datasource removed successfully." ),
    @ResponseCode( code = 401, condition = "User is not authorized to remove DSW datasource." )
  } )
  public Response remove( @PathParam( "dswId" ) String dswId ) {
    try {
      service.removeDSW( dswId );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      throw new WebApplicationException( Response.Status.UNAUTHORIZED );
    }
  }

  /**
   * Get the DSW datasource IDs.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/dsw/domain
   * </p>
   *
   * @return JaxbList<String> of DSW datasource IDs
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      {
   *        "Item": [
   *          {
   *            "@type": "xs:string",
   *            "$": "jmeter-dsw-pentaho-test.xmi"
   *          }
   *        ]
   *      }
   *    </pre>
   */
  @GET
  @Path( "/domain" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public JaxbList<String> getDSWDIds() {
    return createNewJaxbList( service.getDSWDatasourceIds() );
  }

  protected Response buildOkResponse() {
    return Response.ok().build();
  }

  protected Response buildOkResponse( String dswId ) {
    return Response.ok( dswId ).build();
  }

  protected Response buildServerErrorResponse() {
    return Response.serverError().build();
  }

  protected Response buildUnauthorizedResponse() {
    return Response.status( UNAUTHORIZED ).build();
  }

  protected Response buildBadRequestResponse( String message ) {
    return Response.status( Response.Status.BAD_REQUEST ).entity( message ).build();
  }

  protected Response buildConfilictResponse( String message ) {
    return Response.status( Response.Status.CONFLICT ).entity( message ).build();
  }

  protected Response createAttachment( Map<String, InputStream> fileData, String dswId ) {
    return resourceUtil.createAttachment( fileData, dswId );
  }

  protected JaxbList<String> createNewJaxbList( List<String> DSWDatasources ) {
    return new JaxbList<String>( DSWDatasources );
  }

  /**
   * Publish a DSW from a Metadata XMI file.
   *
   * <p><b>Example Request:</b><br />
   *    PUT pentaho/plugin/data-access/api/datasource/dsw/import
   * <br /><b>PUT data:</b>
   *  <pre function="syntax.xml">
   *
   *  </pre>
   * </p>
   *
   * @param domainId The domain to publish to. Must end in '.xmi'.
   * @param metadataFile InputStream with the DSW XMI file
   * @param overwrite Flag for overwriting existing version of the file
   * @param checkConnection Only publish if the required connection exists
   * @param acl acl information for the data source. This parameter is optional.
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *
   *    </pre>
   * TODO:  Change path to /datasource/dsw/{dswId:.+} replace Error Responses with WebApplicationException
   **/
  @PUT
  @Path( "/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( MediaType.TEXT_PLAIN )
  @Facet( name = "Unsupported" )
  public Response publishDsw(
      @FormDataParam( "domainId" ) final String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "overwrite" ) @DefaultValue( "false" ) boolean overwrite,
      @FormDataParam( "checkConnection" ) @DefaultValue( "false" ) boolean checkConnection,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl) {
    try {
      final String dswId = service.publishDsw( domainId, metadataFile, overwrite, checkConnection, acl );
      return buildOkResponse( dswId );
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    } catch ( IllegalArgumentException e ) {
      return buildBadRequestResponse( e.getMessage() );
    } catch ( DataSourceWizardService.DswPublishValidationException e ) {
      return buildConfilictResponse( e.getMessage() );
    } catch ( Exception e ) {
      return buildServerErrorResponse();
    }
  }
  
  /**
   * Returns a list of datasource IDs from datasource wizard
   *
   * @return JaxbList<String> list of datasource IDs
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public JaxbList<String> getDSWDatasourceIds() {
    return getDSWDIds();
  }

  /**
   * Export the DSW data source for the given DSW ID.  The response will be zipped if there is
   * more than one file.  The response will contain an XMI and/or a mondrian cube definition file.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/dsw/jmeter-dsw-pentaho-test.xmi/download
   * </p>
   *
   * @param dswId The id of the DSW datasource to export
   *
   * @return A Response object containing the encrypted DSW data source files.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *          An encrypted .XMI file or a .zip with encoded .XMI files
   *    </pre>
   */
  @GET
  @Path( "/{dswId : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "DSW datasource export succeeded." ),
      @ResponseCode( code = 401, condition = "User is not authorized to export DSW datasource." ),
      @ResponseCode( code = 500, condition = "Failure to export DSW datasource." )
  } )
  public Response doGetDSWFilesAsDownload( @PathParam( "dswId" ) String dswId ) {
    return downloadDsw( dswId );
  }

  /**
   * Remove the metadata for a given metadata ID
   * 
   * @param metadataId
   *          String ID of the metadata to remove
   * 
   * @return Response ok if successful
   */
  @POST
  @Path( "/{dswId : .+}/remove" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "DSW datasource removed successfully." ),
      @ResponseCode( code = 401, condition = "User is not authorized to remove DSW datasource." ),
  } )
  @Facet( name = "Unsupported" )
  public Response doRemoveMetadata( @PathParam( "dswId" ) String metadataId ) {
    return remove( metadataId );
  }

  /**
   * Get ACL for the DSW by name
   *
   * @param   dswId DSW data source name
   * @return  ACL or null if the data source doesn't have it
   * @throws  PentahoAccessControlException if the user doesn't have access
   */
  @GET
  @Path( "/{dswId : .+}/acl" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully got the ACL" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 404, condition = "ACL doesn't exist" ),
      @ResponseCode( code = 409, condition = "DSW doesn't exist" ),
      @ResponseCode(
          code = 500,
          condition = "ACL failed to be retrieved. This could be caused by an invalid path, or the file does not exist."
      )
      } )
      public RepositoryFileAclDto doGetDSWAcl( @PathParam( "dswId" ) String dswId ) {
    try {
      final RepositoryFileAclDto acl = service.getDSWAcl( dswId );
      if ( acl == null ) {
        throw new WebApplicationException( NOT_FOUND );
      }
      return acl;
    } catch ( FileNotFoundException e ) {
      throw new WebApplicationException( CONFLICT );
    } catch ( PentahoAccessControlException e ) {
      throw new WebApplicationException( UNAUTHORIZED );
    }
  }

  /**
   * Set ACL for the DSW
   *
   * @param dswId DSW name
   * @param acl   ACL to set
   * @return      response
   * @throws      PentahoAccessControlException if the user doesn't have access
   */
  @PUT
  @Path( "/{dswId : .+}/acl" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully updated the ACL" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 409, condition = "DSW doesn't exist" ),
      @ResponseCode( code = 500, condition = "Failed to save acls due to another error." )
      } )
      public Response doSetDSWAcl( @PathParam( "dswId" ) String dswId, RepositoryFileAclDto acl )
      throws PentahoAccessControlException {
    try {
      service.setDSWAcl( dswId, acl );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    } catch ( FileNotFoundException e ) {
      return Response.status( CONFLICT ).build();
    } catch ( Exception e ) {
      return buildServerErrorResponse();
    }
  }
}
