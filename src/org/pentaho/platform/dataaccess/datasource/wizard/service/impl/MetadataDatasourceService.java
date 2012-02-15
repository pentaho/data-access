package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
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

import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.CsvUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;

@Path("/data-access/api/metadata")
public class MetadataDatasourceService {

	private static final String LANG = "[a-z]{2}";
	private static final String LANG_CC = LANG + "_[A-Z]{2}";
	private static final String LANG_CC_EXT = LANG_CC + "_[^/]+";
	private static final List<String> ENCODINGS = Arrays.asList("", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-32BE", "UTF-32LE", "Shift_JIS", "ISO-2022-JP", "ISO-2022-CN", "ISO-2022-KR", "GB18030", "Big5", "EUC-JP", "EUC-KR", "ISO-8859-1", "ISO-8859-2", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "windows-1251", "windows-1256", "KOI8-R", "ISO-8859-9");

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
		IMetadataDomainRepository metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, PentahoSessionHolder.getSession());
		PentahoMetadataDomainRepository metadataImporter = new PentahoMetadataDomainRepository(PentahoSystem.get(IUnifiedRepository.class));
		CsvUtils csvUtils = new CsvUtils();
		boolean validPropertyFiles = true; 
		StringBuffer invalidFiles = new StringBuffer();
		try {
			String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "tmp" + File.separatorChar;
			String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
			FileInputStream metadataInputStream = new FileInputStream(sysTmpDir + File.separatorChar + metadataFile);
			metadataImporter.storeDomain(metadataInputStream, domainId, true);
			metadataDomainRepository.getDomain(domainId);

			StringTokenizer bundleEntriesParam = new StringTokenizer(localizeBundleEntries, ";");
			while (bundleEntriesParam.hasMoreTokens()) {
				String localizationBundleElement = bundleEntriesParam.nextToken();
				StringTokenizer localizationBundle = new StringTokenizer(localizationBundleElement, "=");
				String localizationFileName = localizationBundle.nextToken();
				String localizationFile = localizationBundle.nextToken();

				if (localizationFileName.endsWith(".properties")) {
					String encoding = csvUtils.getEncoding(localizationFile);
					if(ENCODINGS.contains(encoding)) {
						for (final Pattern propertyBundlePattern : patterns) {
							final Matcher propertyBundleMatcher = propertyBundlePattern.matcher(localizationFileName);
							if (propertyBundleMatcher.matches()) {
								FileInputStream bundleFileInputStream = new FileInputStream(sysTmpDir + File.separatorChar + localizationFile);
								metadataImporter.addLocalizationFile(domainId, propertyBundleMatcher.group(2), bundleFileInputStream, true);
								break;
							}
						}
					} else {
						validPropertyFiles =  false;
						invalidFiles.append(localizationFileName);
					}
				}
			}

			if(!validPropertyFiles) {
				return Response.serverError().entity(Messages.getErrorString("MetadataDatasourceService.ERROR_002_PROPERTY_FILES_ERROR") + invalidFiles.toString()).build();	
			}
			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			metadataImporter.removeDomain(domainId);
			return Response.serverError().entity(Messages.getErrorString("MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR")).build();
		}
	}
	
	
	@PUT
	@Path("/storeDomain")
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM, TEXT_PLAIN })
	@Produces("text/plain")
	public Response storeDomain(InputStream metadataFile, @QueryParam("domainId") String domainId) throws PentahoAccessControlException {
		try {
			PentahoMetadataDomainRepository metadataImporter = new PentahoMetadataDomainRepository(PentahoSystem.get(IUnifiedRepository.class));
			metadataImporter.storeDomain(metadataFile, domainId, true);
			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch(Exception e) {
			return Response.serverError().entity(Messages.getErrorString("MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR")).build();
		}
	}
	
	@PUT
	@Path("/addLocalizationFile")
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM, TEXT_PLAIN })
	@Produces("text/plain")
	public Response addLocalizationFile(@QueryParam("domainId") String domainId, @QueryParam("locale") String locale, InputStream propertiesFile) throws PentahoAccessControlException {
		try {
			PentahoMetadataDomainRepository metadataImporter = new PentahoMetadataDomainRepository(PentahoSystem.get(IUnifiedRepository.class));
			metadataImporter.addLocalizationFile(domainId, locale, propertiesFile, true);
			return Response.ok("SUCCESS").type(MediaType.TEXT_PLAIN).build();
		} catch(Exception e) {
			return Response.serverError().entity(Messages.getErrorString("MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR")).build();
		}
	}
}
