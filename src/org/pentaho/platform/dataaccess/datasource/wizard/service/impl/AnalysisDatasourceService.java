/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Nov 12, 2011
 * @author Ezequiel Cuellar
 * 
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;

@Path("/data-access/api/mondrian")
public class AnalysisDatasourceService {

	private IMondrianCatalogService mondrianCatalogService;

	public AnalysisDatasourceService() {
		super();
		mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", PentahoSessionHolder.getSession());
	}

	@PUT
	@Path("/import")
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })	
	@Produces("text/plain")
	public Response importAnalysisDatasource(String parameters, @QueryParam("analysisFile") String analysisFile, @QueryParam("databaseConnection") String databaseConnection) throws PentahoAccessControlException {
		try {
		    String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "tmp" + File.separatorChar;
		    String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
		    File mondrianFile = new File(sysTmpDir + File.separatorChar + analysisFile);

		    mondrianCatalogService.importSchema(mondrianFile, databaseConnection, parameters);
			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.toString()).build();
		}
	}

}
