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


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.JaxbList;


@Path("/data-access/api/datasource")
public class DatasourceResource {

  private static final String MONDRIAN_CATALOG_REF = "MondrianCatalogRef"; //$NON-NLS-1$
  public static final String APPLICATION_ZIP = "application/zip"; //$NON-NLS-1$
  
  protected IMetadataDomainRepository metadataDomainRepository;
  protected IMondrianCatalogService mondrianCatalogService;
  IDSWDatasourceService dswService;
  IModelerService modelerService;
  public static final String METADATA_EXT = ".xmi"; //$NON-NLS-1$
  
  public DatasourceResource() {
    super();
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, PentahoSessionHolder.getSession());
    mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, PentahoSessionHolder.getSession());
    dswService = new DSWDatasourceServiceImpl();
    modelerService = new ModelerService();
    
  }

  /**
   * Get list of IDs of analysis datasource
   *
   * @return JaxbList<String> of analysis IDs
   */
  @GET
  @Path("/analysis/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getAnalysisDatasourceIds() {
    List<String> analysisIds = new ArrayList<String>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), false)) {
      String domainId = mondrianCatalog.getName() + METADATA_EXT;
      Set<String> ids = metadataDomainRepository.getDomainIds();
      if(ids.contains(domainId) == false){
        analysisIds.add(mondrianCatalog.getName());
      }
    }
    return new JaxbList<String>(analysisIds);
  }

  /**
   * Get the Metadata datasource IDs
   *
   * @return JaxbList<String> of metadata IDs
   */
  @GET
  @Path("/metadata/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getMetadataDatasourceIds() {
    List<String> metadataIds = new ArrayList<String>();
    try {
		Thread.sleep(100);
		for(String id:metadataDomainRepository.getDomainIds()) {
		    if(isMetadataDatasource(id)) {
		      metadataIds.add(id);
		    }
		}
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    return new JaxbList<String>(metadataIds);
  }
  
  private boolean isMetadataDatasource(String id) {
    Domain domain;
    try { 
      domain = metadataDomainRepository.getDomain(id);
      if(domain == null) return false;
    } catch (Exception e) { // If we can't load the domain then we MUST return false
      return false;
    }
    
    List<LogicalModel> logicalModelList = domain.getLogicalModels();
    if(logicalModelList != null && logicalModelList.size() >= 1) {
      for(LogicalModel logicalModel : logicalModelList) {
        // keep this check for backwards compatibility for now
        Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
        if(property != null) {
          return false;
        }

        // moving forward any non metadata generated datasource should have this property
    	  property = logicalModel.getProperty("WIZARD_GENERATED_SCHEMA"); //$NON-NLS-1$
    	  if(property != null) {
    		  return false;    
    	  }
      }
      return true;
    } else {
      return true;
    }
  }

  /**
   * Returns a list of datasource IDs from datasource wizard
   *
   * @return JaxbList<String> list of datasource IDs
   */
  @GET
  @Path("/dsw/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getDSWDatasourceIds() {
    List<String> datasourceList = new ArrayList<String>();
    try {
      nextModel: for(LogicalModelSummary summary:dswService.getLogicalModels(null)) {
        Domain domain = modelerService.loadDomain(summary.getDomainId());
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if(logicalModelList != null && logicalModelList.size() >= 1) {
          for(LogicalModel logicalModel : logicalModelList) {	
        	  Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
        	  if(property != null) {
        		  datasourceList.add(summary.getDomainId());
        		  continue nextModel;
        	  }
          }
        }
      }
    } catch (Throwable e) {
      return null;
    }
    return new JaxbList<String>(datasourceList);
  }

  /**
   * Download the metadata files for a given metadataId
   *
   * @param metadataId String Id of the metadata to retrieve
   *
   * @return Response containing the file data
   */
  @GET
  @Path("/metadata/{metadataId : .+}/download")
  @Produces(WILDCARD)
  public Response doGetMetadataFilesAsDownload(@PathParam("metadataId") String metadataId) {
    if(!canAdminister()) {
      return Response.status(UNAUTHORIZED).build();
    }
    if (! (metadataDomainRepository instanceof IPentahoMetadataDomainRepositoryExporter)) {
      return Response.serverError().build();
    }
    Map<String, InputStream> fileData = ((IPentahoMetadataDomainRepositoryExporter)metadataDomainRepository).getDomainFilesData(metadataId);
    return createAttachment(fileData, metadataId);
  }

  /**
   * Download the analysis files for a given analysis id
   *
   * @param analysisId String Id of the analysis data to retrieve
   *
   * @return Response containing the file data
   */
  @GET
  @Path("/analysis/{analysisId : .+}/download")
  @Produces(WILDCARD)
  public Response doGetAnalysisFilesAsDownload(@PathParam("analysisId") String analysisId) {
    if(!canAdminister()) {
      return Response.status(UNAUTHORIZED).build();
    }
    MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(PentahoSystem.get(IUnifiedRepository.class));
    Map<String, InputStream> fileData = helper.getModrianSchemaFiles(analysisId);
    parseMondrianSchemaName(analysisId, fileData);

    return createAttachment(fileData, analysisId);
  }

  /**
   * Download the data source wizard data for a given data source wizard ID
   *
   * @param dswId String Id of the data source wizard data to retrieve
   *
   * @return Response containing the file data
   */
  @GET
  @Path("/dsw/{dswId : .+}/download")
  @Produces(WILDCARD)
  public Response doGetDSWFilesAsDownload(@PathParam("dswId") String dswId) {
    if(!canAdminister()) {
      return Response.status(UNAUTHORIZED).build();
    }
    // First get the metadata files;
    Map<String, InputStream> fileData = ((IPentahoMetadataDomainRepositoryExporter)metadataDomainRepository).getDomainFilesData(dswId); 
  
    // Then get the corresponding mondrian files
    Domain domain = metadataDomainRepository.getDomain(dswId);
    ModelerWorkspace model = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
    model.setDomain(domain);
    LogicalModel logicalModel = model.getLogicalModel(ModelerPerspective.ANALYSIS);
    if (logicalModel == null) {
      logicalModel = model.getLogicalModel(ModelerPerspective.REPORTING);
    }
    if (logicalModel.getProperty(MONDRIAN_CATALOG_REF) != null) {
      MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(PentahoSystem.get(IUnifiedRepository.class));
      String catalogRef = (String)logicalModel.getProperty(MONDRIAN_CATALOG_REF);
      fileData.putAll(helper.getModrianSchemaFiles(catalogRef));
      parseMondrianSchemaName( dswId, fileData );
    }

    return createAttachment(fileData, dswId);
  }

  private void parseMondrianSchemaName( String dswId, Map<String, InputStream> fileData ) {
    final String keySchema = "schema.xml";//$NON-NLS-1$
    if ( fileData.containsKey( keySchema ) ) {
      final int xmiIndex = dswId.lastIndexOf( ".xmi" );//$NON-NLS-1$
      fileData.put( ( xmiIndex > 0 ? dswId.substring( 0, xmiIndex ) : dswId ) + ".mondrian.xml", fileData.get( keySchema ) );//$NON-NLS-1$
      fileData.remove( keySchema );
    }
  }

  /**
   * Get the data source wizard info (parameters) for a specific data source wizard id
   *
   * @param dswId String id for a data source wizard
   *
   * @return Response containing the parameter list
   */
  @GET
  @Path("/{dswId : .+}/getAnalysisDatasourceInfo")
  @Produces(WILDCARD)
  public Response getAnalysisDatasourceInfo(@PathParam("dswId") String dswId) {
	MondrianCatalog catalog = mondrianCatalogService.getCatalog(dswId, PentahoSessionHolder.getSession());
	String parameters = catalog.getDataSourceInfo();
	return Response.ok().entity(parameters).build();
  }  

  private Response createAttachment(Map<String, InputStream> fileData, String domainId) {
    String quotedFileName = null;
    final InputStream is;
    if (fileData.size() > 1) { // we've got more than one file so we want to zip them up and send them
      File zipFile = null;
      try {
        zipFile = File.createTempFile("datasourceExport", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
        zipFile.deleteOnExit();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        for (String fileName : fileData.keySet()) {
          InputStream zipEntryIs = null;
          try {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zipEntryIs = fileData.get(fileName);
            IOUtils.copy(zipEntryIs, zos);
          } catch (Exception e) {
            continue;
          } finally {
            zos.closeEntry();
            if (zipEntryIs != null) {
              zipEntryIs.close();
            }
          }
        }
        zos.close();
        is = new FileInputStream(zipFile);
      } catch (IOException ioe) {
        return Response.serverError().entity(ioe.toString()).build();
      }
      StreamingOutput streamingOutput = new StreamingOutput() {
        public void write(OutputStream output) throws IOException {
          IOUtils.copy(is, output);
        }
      };
      final int xmiIndex = domainId.lastIndexOf( ".xmi" );//$NON-NLS-1$
      quotedFileName = "\"" + ( xmiIndex > 0 ? domainId.substring( 0, xmiIndex ) : domainId ) + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      return Response.ok(streamingOutput, APPLICATION_ZIP).header("Content-Disposition", "attachment; filename=" + quotedFileName).build(); //$NON-NLS-1$ //$NON-NLS-2$
    } else if (fileData.size() == 1) {  // we've got a single metadata file so we just return that.
      String fileName = (String) fileData.keySet().toArray()[0];
      quotedFileName = "\"" + fileName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
      is = fileData.get(fileName);
      String mimeType = MediaType.TEXT_PLAIN;
      if (is instanceof RepositoryFileInputStream) {
        mimeType = ((RepositoryFileInputStream)is).getMimeType();
      }
      StreamingOutput streamingOutput = new StreamingOutput() {
        public void write(OutputStream output) throws IOException {
          IOUtils.copy(is, output);
        }
      };
      return Response.ok(streamingOutput, mimeType).header("Content-Disposition", "attachment; filename=" + quotedFileName).build(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return Response.serverError().build();
  }
  
  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem
        .get(IAuthorizationPolicy.class);
    return policy
        .isAllowed(RepositoryReadAction.NAME) && policy.isAllowed(RepositoryCreateAction.NAME)
        && (policy.isAllowed(AdministerSecurityAction.NAME));
  }
}
