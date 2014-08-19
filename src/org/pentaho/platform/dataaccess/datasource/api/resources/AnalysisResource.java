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
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;

@Path( "/data-access/api/datasource/analysis" )
public class AnalysisResource {

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
    if ( !service.canAdminister() ) {
      return Response.status( UNAUTHORIZED ).build();
    }
    service.removeAnalysis( analysisId );
    return Response.ok().build();
  }
}
