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

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import com.sun.jersey.multipart.FormDataParam;

public class DataSourceWizardResource {

  private DataSourceWizardService service;

  public DataSourceWizardResource() {
    service = new DataSourceWizardService();
  }

  /**
   *
   * @param dswId String Id of the data source wizard data to retrieve
   *
   * @return Response containing the file data
   */


  /**
   * Export the DSW data source for the given DSW ID.  The response will be zipped if there are
   * more than one file.  The response will contain an XMI and/or a mondrian cube definition file.
   *
   * <p><b>Example Request:</b><br/>
   *   GET /pentaho/plugin/data-access/api/datasource/dsw/MyDSWDS/download
   * </p>
   *
   * @param dswId The id of the DSW datasource to export
   *               <pre function="syntax.xml">
   *               {@code
   *               MyDSWDS
   *               }
   *               </pre>
   * @return A Response object containing the DSW data source files.
   */
  @GET
  @Path( "/datasource/dsw/{dswId : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "DSW datasource export succeeded." ),
    @ResponseCode( code = 401, condition = "User is not authorized to export DSW datasource." ),
    @ResponseCode( code = 500, condition = "Failure to export DSW datasource." )
  } )
  public Response download( @PathParam( "dswId" ) String dswId ) {
    try {
      Map<String, InputStream> fileData = service.doGetDSWFilesAsDownload( dswId );
      return ResourceUtil.createAttachment( fileData, dswId );
    } catch ( PentahoAccessControlException e ) {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Remove the DSW data source for a given DSW ID
   *
   * <p><b>Example Request:</b><br/>
   *   POST /pentaho/plugin/data-access/api/datasource/dsw/MyDSWDS/remove
   * </p>
   *
   * @param dswId The id of the DSW datasource to remove
   *               <pre function="syntax.xml">
   *               {@code
   *               MyDSWDS
   *               }
   *               </pre>
   */
  @POST
  @Path( "/datasource/dsw/{dswId : .+}/remove" )
  @Produces( WILDCARD )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "DSW datasource removed successfully." ),
    @ResponseCode( code = 401, condition = "User is not authorized to remove DSW datasource." ),
  } )
  public Response remove( @PathParam( "dswId" ) String dswId ) {
    try {
      service.removeDSW( dswId );
      return Response.ok().build();
    } catch ( PentahoAccessControlException e ) {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Get the DSW datasource IDs
   *
   * <p><b>Example Request:</b><br/>
   *   GET /pentaho/plugin/data-access/api/datasource/dsw/ids
   * </p>
   *
   * @return JaxbList<String> of DSW datasource IDs
   *               <pre function="syntax.xml">
   *               {@code
   *               {"Item":{"@type":"xs:string","$":"MyDSWDS"}}
   *               }
   *               </pre>
   */
  @GET
  @Path( "/datasource/dsw/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public JaxbList<String> getDSWDatasourceIds() {
    return new JaxbList<String>( service.getDSWDatasourceIds() );
  }

  /**
   * Publish a DSW from a Metadata XMI file
   *
   * <p><b>Example Request:</b>
   *  PUT /data-access/api/datasource/dsw/import
   * </p>
   *
   * @param domainId The domain to publish to. Must end in '.xmi'.
   * <pre function="syntax.xml">
   *  ADomain.xmi
   * </pre>
   * @param metadataFile InputStream with the DSW XMI file
   * @param overwrite Flag for overwriting existing version of the file
   * @param checkConnection Only publish if the required connection exists
   *
   * @return Response containing the success of the method and the published domain id
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   200 ADomain.xmi
   * </pre>
   **/
  @PUT
  @Path( "/datasource/dsw/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( MediaType.TEXT_PLAIN )
  public Response publishDsw(
      @FormDataParam( "domainId" ) final String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "overwrite" ) @DefaultValue( "false" ) boolean overwrite,
      @FormDataParam( "checkConnection" ) @DefaultValue( "false" ) boolean checkConnection ) {
    try {
      final String dswId = service.publishDsw( domainId, metadataFile, overwrite, checkConnection );
      return Response.ok( dswId ).build();
    } catch ( PentahoAccessControlException e ) {
      return Response.status( UNAUTHORIZED ).build();
    } catch ( IllegalArgumentException e ) {
      return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
    } catch ( DataSourceWizardService.DswPublishValidationException e ) {
      return Response.status( Response.Status.CONFLICT ).entity( e.getMessage() ).build();
    } catch ( Exception e ) {
      return Response.serverError().build();
    }
  }

}
