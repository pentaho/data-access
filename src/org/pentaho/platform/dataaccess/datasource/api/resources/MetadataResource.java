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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.pentaho.platform.dataaccess.datasource.api.DatasourceService.UnauthorizedAccessException;
import org.pentaho.platform.dataaccess.datasource.api.MetadataService;
import org.pentaho.platform.web.http.api.resources.JaxbList;

@Path( "/data-access/api/datasource/metadata" )
public class MetadataResource {

  private MetadataService service;

  public MetadataResource() {
    service = new MetadataService();
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
  @Path( "/{metadataId : .+}/remove" )
  @Produces( WILDCARD )
  public Response doRemoveMetadata( @PathParam( "metadataId" ) String metadataId ) {
    try {
      service.removeMetadata( metadataId );
      return Response.ok().build();
    } catch ( UnauthorizedAccessException e ) {
      return Response.status( e.getStatus() ).build();
    }
  }

  /**
   * Get the Metadata datasource IDs
   *
   * @return JaxbList<String> of metadata IDs
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public JaxbList<String> getMetadataDatasourceIds() {
    return new JaxbList<String>( service.getMetadataDatasourceIds() );
  }
}
