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

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.StringListWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Rowell Belen
 */
@Path("/data-access/api/permissions")
public class DataAccessPermissionResource {

  IAuthorizationPolicy policy;
  
  public DataAccessPermissionResource(){
    policy = PentahoSystem.get( IAuthorizationPolicy.class );
  }

  @GET
  @Path("/hasDataAccess")
  @Produces( {APPLICATION_JSON })
  public Response hasDataAccessPermission() {
    return Response.ok( "" + policy.isAllowed( "org.pentaho.platform.dataaccess.datasource.security.manage" ) ).build();
  }

  @GET
  @Path("/hasDataAccessView")
  @Produces( {APPLICATION_JSON })
  public Response hasDataAccessViewPermission() {
    return Response.ok( "" + policy.isAllowed( "org.pentaho.platform.dataaccess.datasource.security.view" ) ).build();
  }
}
