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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;

import com.sun.jersey.multipart.FormDataParam;

@Path("/data-access/api/mondrian")
public class AnalysisDatasourceService {

  private static final String ACCESS_DENIED = "Access Denied";

  private static final String SUCCESS = "SUCCESS";

  private IMondrianCatalogService mondrianCatalogService;
  
  private IPlatformImporter importHandlerService;

  private final String ACTION_READ = "org.pentaho.repository.read";

  private final String ACTION_CREATE = "org.pentaho.repository.create";

  private final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity";

  private static final String DOMAIN_ID = "domain-id";

  private static final String TEXT_XMI_XML = "text/xmi+xml";

  private static final String UTF_8 = "UTF-8";

  private static final String mondrianMimeType = "application/vnd.pentaho.mondrian+xml";

  public AnalysisDatasourceService() {
    super();
    mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService",
        PentahoSessionHolder.getSession());
    
  }

  @PUT
  @Path("/import")
  @Consumes({ TEXT_PLAIN })
  @Produces("text/plain")
  public Response importAnalysisDatasource(String parameters, @QueryParam("analysisFile")
  String analysisFile, @QueryParam("databaseConnection")
  String databaseConnection) throws PentahoAccessControlException {
    try {
      validateAccess();
      String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "tmp" + File.separatorChar;
      String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
      File mondrianFile = new File(sysTmpDir + File.separatorChar + analysisFile);

      mondrianCatalogService.importSchema(mondrianFile, databaseConnection, parameters);
      return Response.ok(SUCCESS).type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @PUT
  @Path("/importSchema")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response importAnalysisSchemaFile(
      @FormDataParam("parameters") String parameters, 
      @FormDataParam("schemaFile") InputStream dataInputStream, 
      @FormDataParam("datasourceName") String datasourceName,
      @FormDataParam("overwrite") String overwrite,
      @FormDataParam("xmlaEnabledFlag") String xmlaEnabledFlag
      ) throws PentahoAccessControlException {
    try {     
      validateAccess();
      boolean overWriteInRepository = "True".equalsIgnoreCase(overwrite)?true:false;
      boolean xmlaEnabled = "True".equalsIgnoreCase(xmlaEnabledFlag)?true:false;
     
      File mondrianFile = createFileFromStream(dataInputStream);
      mondrianCatalogService.importSchema(mondrianFile, datasourceName, parameters);
      return Response.ok(SUCCESS).type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  private File createFileFromStream(InputStream dataInputStream) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(dataInputStream));
    String currentLine = null;
    while ((currentLine = in.readLine()) != null) {
      currentLine += currentLine;
    }
    File mondrianFile = new File(currentLine);
    return mondrianFile;
  }

  @PUT
  @Path("/addSchema")
  @Consumes({ MediaType.APPLICATION_OCTET_STREAM, TEXT_PLAIN })
  @Produces("text/plain")
  public Response addSchema(InputStream mondrianFile, @QueryParam("catalogName")
  String catalogName, @QueryParam("datasourceInfo")
  String datasourceInfo) throws PentahoAccessControlException {
    try {
      validateAccess();
      MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(
          PentahoSystem.get(IUnifiedRepository.class));
      helper.addSchema(mondrianFile, catalogName, datasourceInfo);

      // Flush the Mondrian cache to show imported datasources. 
      IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class,
          "IMondrianCatalogService", PentahoSessionHolder.getSession());
      mondrianCatalogService.reInit(PentahoSessionHolder.getSession());

      return Response.ok(SUCCESS).type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      return Response.serverError().entity(e.toString()).build();
    }
  }

  private void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);
    boolean isAdmin = policy.isAllowed(ACTION_READ) && policy.isAllowed(ACTION_CREATE)
        && policy.isAllowed(ACTION_ADMINISTER_SECURITY);
    if (!isAdmin) {
      throw new PentahoAccessControlException(ACCESS_DENIED);
    }
  }

  /**
   * Utility to build a bundle from the data input stream
   * @param dataInputStream
   * @param domainId
   * @return IPlatformImportBundle cast
   */
  private IPlatformImportBundle fileIImportBundle(InputStream dataInputStream, String domainId,String mimeType,boolean overWriteInRepository) {
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder().input(dataInputStream)
        .charSet(UTF_8).hidden(false).mime(mimeType).withParam(DOMAIN_ID, domainId).overwrite(overWriteInRepository);
   
    return (IPlatformImportBundle) bundleBuilder;

  }
}
