/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * @author wseyler
 */
@Path( "/data-access/api/dbconnection" )
public class DatabaseConnectionService {

  /**
   * The class should have at least one method with JAX-RS annotations.
   * In this case, the getDatabaseConnection method is annotated with @GET,
   * indicating it responds to HTTP GET requests.
   */
  @GET
  public Response getDatabaseConnection() {
    return Response.ok( "No implementation" ).build();
  }
}
