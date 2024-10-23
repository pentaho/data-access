/*
* Copyright 2002 - 2024 Hitachi Vantara.  All rights reserved.
* 
* This software was developed by Hitachi Vantara and is provided under the terms
* of the Mozilla Public License, Version 1.1, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
