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

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.File;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.repository2.unified.importexport.legacy.MondrianCatalogRepositoryHelper;

@Path("/data-access/api/mondrian")
public class AnalysisDatasourceService {

	private IMondrianCatalogService mondrianCatalogService;

	public AnalysisDatasourceService() {
		super();
		mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", PentahoSessionHolder.getSession());
	}

	@PUT
	@Path("/import")
	@Consumes({ TEXT_PLAIN })	
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
	
	@PUT
	@Path("/addSchema")
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM, TEXT_PLAIN })
	@Produces("text/plain")
	public Response addSchema(InputStream mondrianFile, @QueryParam("catalogName") String catalogName, @QueryParam("datasourceInfo") String datasourceInfo) throws PentahoAccessControlException {
		try {
			MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(PentahoSystem.get(IUnifiedRepository.class));
		    helper.addSchema(mondrianFile, catalogName, datasourceInfo);
		    
		    // Flush the Mondrian cache to show imported datasources. 
	        IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", PentahoSessionHolder.getSession());
		    mondrianCatalogService.reInit(PentahoSessionHolder.getSession());
		    
			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch(Exception e) {
			return Response.serverError().entity(e.toString()).build();
		}
	}
}
