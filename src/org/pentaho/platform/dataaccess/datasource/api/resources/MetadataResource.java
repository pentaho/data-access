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
import java.util.List;
import java.util.Map;

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
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.api.MetadataService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.web.http.api.resources.FileResource;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path( "/data-access/api" )
public class MetadataResource {

  private static final Log logger = LogFactory.getLog( MetadataResource.class );
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final String SUCCESS = "3";

  private MetadataService service;
  private IMetadataDomainRepository metadataDomainRepository;
  
  public MetadataResource() {
    service = new MetadataService();
    metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
  }

  /**
   * Download the metadata files for a given metadataId
   *
   * @param metadataId String Id of the metadata to retrieve
   *
   * @return Response containing the file data
   */
  @GET
  @Path( "/datasource/metadata/{metadataId : .+}/download" )
  @Produces( WILDCARD )
  public Response doGetMetadataFilesAsDownload( @PathParam( "metadataId" ) String metadataId ) {
    if( !DatasourceService.canAdminister() ) {
      return Response.status( UNAUTHORIZED ).build();
    }
    if ( !( metadataDomainRepository instanceof IPentahoMetadataDomainRepositoryExporter ) ) {
      return Response.serverError().build();
    }
    Map<String, InputStream> fileData = ( (IPentahoMetadataDomainRepositoryExporter) metadataDomainRepository ).getDomainFilesData( metadataId );
    return ResourceUtil.createAttachment( fileData, metadataId );
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
  @Path( "/datasource/metadata/{metadataId : .+}/remove" )
  @Produces( WILDCARD )
  public Response doRemoveMetadata( @PathParam( "metadataId" ) String metadataId ) {
    try {
      service.removeMetadata( metadataId );
      return Response.ok().build();
    } catch ( PentahoAccessControlException e ) {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Get the Metadata datasource IDs
   *
   * @return JaxbList<String> of metadata IDs
   */
  @GET
  @Path( "/datasource/metadata/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public JaxbList<String> getMetadataDatasourceIds() {
    return new JaxbList<String>( service.getMetadataDatasourceIds() );
  }

  /**
   * @param domainId
   *          Unique identifier for the metadata datasource
   * @param metadataFile
   *          Input stream for the metadata.xmi
   * @param metadataFileInfo
   *          User selected name for the file
   * @param localeFiles
   *          List of local files
   * @param localeFilesInfo
   *          List of information for each local file
   * @param overwrite
   *          Flag for overwriting existing version of the file
   *
   * @return Response containing the success of the method
   *
   * @throws PentahoAccessControlException
   *           Thrown when validation of access fails
   */
  @PUT
  @Path( "/datasource/metadata/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  public Response importMetadataDatasource( @FormDataParam( "domainId" ) String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "metadataFile" ) FormDataContentDisposition metadataFileInfo,
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( "localeFiles" ) List<FormDataBodyPart> localeFiles,
      @FormDataParam( "localeFiles" ) List<FormDataContentDisposition> localeFilesInfo )
    throws PentahoAccessControlException {
    try {
      service.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
          localeFilesInfo );
      return Response.ok().status( new Integer( SUCCESS ) ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( PentahoAccessControlException e ) {
      return Response.serverError().entity( e.toString() ).build();
    } catch ( PlatformImportException e ) {
      if ( e.getErrorStatus() == PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR ) {
        FileResource fr = new FileResource();
        return Response.status( PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR ).entity(
            Messages.getString( "MetadataDatasourceService.ERROR_003_PROHIBITED_SYMBOLS_ERROR", domainId, (String) fr
                .doGetReservedCharactersDisplay().getEntity() ) ).build();
      } else {
        String msg = e.getMessage();
        logger.error( "Error import metadata: " + msg + " status = " + e.getErrorStatus() );
        Throwable throwable = e.getCause();
        if ( throwable != null ) {
          msg = throwable.getMessage();
          logger.error( "Root cause: " + msg );
        }
        int statusCode = e.getErrorStatus();
        Response response = Response.ok().status( statusCode ).type( MediaType.TEXT_PLAIN ).build();
        return response;
      }
    } catch ( Exception e ) {
      logger.error( e );
      return Response.serverError().entity(
          Messages.getString( "MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR" ) ).build();
    }
  }
}
