package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.repository.pmd.PentahoMetadataDomainRepository;

@Path("/data-access/api/metadata")
public class MetadataDatasourceService {

	private static final String LANG = "[a-z]{2}";
	private static final String LANG_CC = LANG + "_[A-Z]{2}";
	private static final String LANG_CC_EXT = LANG_CC + "_[^/]+";

	private static final Pattern[] patterns = new Pattern[] {
	    Pattern.compile("(" + LANG + ").properties$"),
	    Pattern.compile("(" + LANG_CC + ").properties$"),
	    Pattern.compile("(" + LANG_CC_EXT + ").properties$"),
	    Pattern.compile("([^/]+)_(" + LANG + ")\\.properties$"),
	    Pattern.compile("([^/]+)_(" + LANG_CC + ")\\.properties$"),
	    Pattern.compile("([^/]+)_(" + LANG_CC_EXT + ")\\.properties$"),
	};	
	
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
			metadataImporter.storeDomain(metadataInputStream, domainId, true);

			StringTokenizer bundleEntriesParam = new StringTokenizer(localizeBundleEntries, ";");
			while (bundleEntriesParam.hasMoreTokens()) {
				String localizationBundleElement = bundleEntriesParam.nextToken();
				StringTokenizer localizationBundle = new StringTokenizer(localizationBundleElement, "=");
				String localizationFileName = localizationBundle.nextToken();
				String localizationFile = localizationBundle.nextToken();

				if (localizationFileName.endsWith(".properties")) {
					for (final Pattern propertyBundlePattern : patterns) {
						final Matcher propertyBundleMatcher = propertyBundlePattern.matcher(localizationFileName);
						if (propertyBundleMatcher.matches()) {
							FileInputStream bundleFileInputStream = new FileInputStream(sysTmpDir + File.separatorChar + localizationFile);
							metadataImporter.addLocalizationFile(domainId, propertyBundleMatcher.group(2), bundleFileInputStream, true);
							break;
						}
					}
				}
			}

			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			return Response.serverError().entity(Messages.getInstance().getErrorString("MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR")).build();
		}
	}
}
