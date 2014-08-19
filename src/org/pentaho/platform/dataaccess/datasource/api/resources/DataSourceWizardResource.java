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

import static javax.ws.rs.core.MediaType.WILDCARD;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService.UnauthorizedAccessException;

@Path( "/data-access/api/datasource/dsw" )
public class DataSourceWizardResource {

  private DataSourceWizardService service;

  public DataSourceWizardResource() {
    service = new DataSourceWizardService();
  }

  /**
   * Remove the datasource wizard data for a given datasource wizard ID
   *
   * @param dswId
   *          String ID of the datasource wizard data to remove
   *
   * @return Response ok if successful
   */
  @POST
  @Path( "/{dswId : .+}/remove" )
  @Produces( WILDCARD )
  public Response doRemoveDSW( @PathParam( "dswId" ) String dswId ) {
    try {
      service.removeDSW( dswId );
      return Response.ok().build();
    } catch ( UnauthorizedAccessException e ) {
      return Response.status( e.getStatus() ).build();
    }
  }
}
