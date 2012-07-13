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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/data-access/api/mondrian")
public class AnalysisDatasourceService {

  private static final String ACCESS_DENIED = "Access Denied";

  private static final String SUCCESS = "3";//SUCCESS

  private IMondrianCatalogService mondrianCatalogService;

  private final String ACTION_READ = "org.pentaho.repository.read";

  private final String ACTION_CREATE = "org.pentaho.repository.create";

  private final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity";

  private static final String DOMAIN_ID = "domain-id";

  private static final String TEXT_XMI_XML = "text/xmi+xml";

  private static final String UTF_8 = "UTF-8";

  private static final String MONDRIAN_MIME_TYPE = "application/vnd.pentaho.mondrian+xml";

  public static final IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);

  public AnalysisDatasourceService() {
    super();
    mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService",
        PentahoSessionHolder.getSession());

  }

  /**
   * This is used by the Pentaho User Console Manage Existing Import 
   * @param parameters
   * @param analysisFile
   * @param databaseConnection
   * @return
   * @throws PentahoAccessControlException
   * @depricated
   */
  @PUT
  @Path("/import")
  @Consumes({ TEXT_PLAIN })
  @Produces("text/plain")
  public Response importAnalysisDatasource(String parameters, @QueryParam("analysisFile")
  String analysisFile, @QueryParam("databaseConnection")
  String databaseConnection) throws PentahoAccessControlException {
    try {
      validateAccess();
      System.out.println("importAnalysisDatasource " + analysisFile);
      String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "tmp" + File.separatorChar;
      String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
      File mondrianFile = new File(sysTmpDir + File.separatorChar + analysisFile);

      mondrianCatalogService.importSchema(mondrianFile, databaseConnection, parameters);
      return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  /**
   * This is used by the Schema Workbench to import a mondrian XML analysis
   * @param parameters
   * @param dataInputStream
   * @param datasourceName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @return A single numeric value for both success and failure
   * @throws PentahoAccessControlException
   * 
   * @author: tband 
   * date: 7/10/12
   */
  @PUT
  @Path("/importSchema")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response importAnalysisSchemaFile(
      @FormDataParam("parameters") String parameters, 
      @FormDataParam("uploadAnalysis") InputStream dataInputStream, 
      @FormDataParam("uploadAnalysis")FormDataContentDisposition schemaFileInfo, 
      @FormDataParam("catalogName") String catalogName, 
      @FormDataParam("overwrite") String overwrite, 
      @FormDataParam("xmlaEnabledFlag") String xmlaEnabledFlag) throws PentahoAccessControlException {
    Response response = null;
    String statusCode = String.valueOf(PlatformImportException.PUBLISH_GENERAL_ERROR);
    try {
      validateAccess();
      boolean overWriteInRepository = "True".equalsIgnoreCase(overwrite) ? true : false;

      String fileName = schemaFileInfo.getFileName();
      IPlatformImportBundle bundle = createPlatformBundle(parameters, dataInputStream, catalogName,
          overWriteInRepository, fileName, xmlaEnabledFlag);

      importer.importFile(bundle);

      statusCode = SUCCESS;
      //TO DO - get better error handling return messages
    } catch (PentahoAccessControlException pac) {
      System.out.println(pac.getMessage());
      statusCode = String.valueOf(PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL);
    } catch (PlatformImportException pe) {
      System.out.println("Error importAnalysisSchemaFile " + pe.getMessage() + " status = " + pe.getErrorStatus());
      statusCode = String.valueOf(pe.getErrorStatus());
    } catch (Exception e) {
      System.out.println("Error importAnalysisSchemaFile " + e.getMessage());
      statusCode = String.valueOf(PlatformImportException.PUBLISH_GENERAL_ERROR);
    }

    response = Response.ok(statusCode).type(MediaType.TEXT_PLAIN).build();
    System.out.println("importAnalysisSchemaFile Response " + response);
    return response;
  }

  /**
   * New method used by PUC to pass the inputstream, catalogName (optional), and parameters
   * @param parameters
   * @param dataInputStream
   * @param schemaFileInfo
   * @param catalogName
   * @return
   * @throws PentahoAccessControlException
   */
  @POST
  @Path("/importAnalysis")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response importAnalysisFile(
      @FormDataParam("parameters")String parameters,
      @FormDataParam("uploadAnalysis")InputStream dataInputStream, 
      @FormDataParam("uploadAnalysis") FormDataContentDisposition schemaFileInfo, 
      @FormDataParam("catalogName") String catalogName) throws PentahoAccessControlException {

    Response response = null;
    String statusCode = String.valueOf(PlatformImportException.PUBLISH_GENERAL_ERROR);
    try {
      validateAccess();

      String fileName = schemaFileInfo.getFileName();
      IPlatformImportBundle bundle = createPlatformBundle(parameters, dataInputStream, catalogName, fileName);

      importer.importFile(bundle);

      statusCode = SUCCESS;
      //TO DO - get better error handling return messages
    } catch (PentahoAccessControlException pac) {
      System.out.println(pac.getMessage());
      statusCode = String.valueOf(PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL);
    } catch (PlatformImportException pe) {
      System.out.println("Error importAnalysisFile " + pe.getMessage() + " status = " + pe.getErrorStatus());
      statusCode = String.valueOf(pe.getErrorStatus());
    } catch (Exception e) {
      System.out.println("Error importAnalysisFile " + e.getMessage());
      statusCode = String.valueOf(PlatformImportException.PUBLISH_GENERAL_ERROR);
    }

    response = Response.ok(statusCode).type(MediaType.TEXT_PLAIN).build();
    System.out.println("importAnalysisFile Response " + response);
    return response;
  }

  private IPlatformImportBundle createPlatformBundle(String parameters, InputStream dataInputStream,
      String catalogName, String fileName) {
    String domainId  = null;
    boolean overWriteInRepository = "True".equalsIgnoreCase(getValue(parameters, "overwrite")) ? true : false;
    String xmlaEnabled = getValue(parameters, "xmlaEnabled");
    if(catalogName == null || "".equals(catalogName)){
      domainId = fileName;
    }
    if (domainId == null || "".equals(domainId)) {
      domainId = getValue(parameters, catalogName);
    }
    return createPlatformBundle(parameters, dataInputStream, domainId, overWriteInRepository, fileName, xmlaEnabled);
  }

  private IPlatformImportBundle createPlatformBundle(String parameters, InputStream dataInputStream,
      String catalogName, boolean overWriteInRepository, String fileName, String xmlaEnabled) {
    String datasource = getValue(parameters,"Datasource");
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder().input(dataInputStream)
        .charSet(UTF_8).hidden(false).name(fileName).overwrite(overWriteInRepository).mime(MONDRIAN_MIME_TYPE)
        .withParam("parameters", parameters)
        .withParam("Datasource", datasource)
        .withParam("xmlaEnabled", xmlaEnabled).withParam(DOMAIN_ID, catalogName);

    IPlatformImportBundle bundle = bundleBuilder.build();
    return bundle;
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

      reInitMondrainCache();

      return Response.ok(SUCCESS).type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      return Response.serverError().entity(e.toString()).build();
    }
  }

  private void reInitMondrainCache() {
    // Flush the Mondrian cache to show imported datasources. 
    IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class,
        "IMondrianCatalogService", PentahoSessionHolder.getSession());
    mondrianCatalogService.reInit(PentahoSessionHolder.getSession());
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
   * convert string to property to do a lookup "Provider=Mondrian;DataSource=Pentaho"
   * @param parameters
   * @param key
   * @return
   */
  private String getValue(String parameters, String key) {
    // convert  string list and lookup value
    String value = null;
    if (parameters != null && !"".equals(parameters)) {
      String[] pairs = StringUtils.split(parameters, ";");
      if (pairs != null) {
        for (int i = 0; i < pairs.length; i++) {
          String[] map = StringUtils.split(pairs[i], "=");
          if (map[0].equalsIgnoreCase(key)) {
            value = map[1];
            break;
          }
        }
      }
    }
    return value;
  }
}
