package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.StringTokenizer;

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
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.pmd.PentahoMetadataDomainRepository;

@Path("/data-access/api/metadata")
public class MetadataDatasourceService {
	
	@PUT
	@Path("/import")
	@Consumes({ TEXT_PLAIN })	
	@Produces("text/plain")
	public Response importMetadataDatasource(String localizeBundleEntries, @QueryParam("domainId") String domainId, @QueryParam("metadataFile") String metadataFile) throws PentahoAccessControlException {
		try {
			
		    String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "tmp" + File.separatorChar;
		    String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
			PentahoMetadataDomainRepository metadataImporter = new PentahoMetadataDomainRepository(PentahoSystem.get(IUnifiedRepository.class));
			FileInputStream metadataInputStream = new FileInputStream(sysTmpDir + File.separatorChar + metadataFile);
			//metadataImporter.storeDomain(metadataInputStream, domainId, true);
			
			StringTokenizer bundleEntriesParam = new StringTokenizer(localizeBundleEntries, ";");
			while(bundleEntriesParam.hasMoreTokens()) {
				String localizationBundleElement = bundleEntriesParam.nextToken();
				StringTokenizer localizationBundle = new StringTokenizer(localizationBundleElement, "=");
				String fileName = localizationBundle.nextToken();
				String file = localizationBundle.nextToken();
				
				
				Locale locale = null;
				String[] languages = Locale.getISOLanguages();
				for(String language : languages) {
					if(fileName.endsWith(language + ".properties")) {
						locale = new Locale(language);
						break;
					} 
				}
				
				FileInputStream bundleFileInputStream = new FileInputStream(sysTmpDir + File.separatorChar + file);
				//metadataImporter.addLocalizationFile(domainId, locale.toString(), bundleFileInputStream, true);
			}
			
			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.toString()).build();
		}
	}
}
