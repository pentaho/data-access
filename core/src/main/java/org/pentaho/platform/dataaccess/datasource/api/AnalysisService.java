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
 * Copyright (c) 2002-2020 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class AnalysisService extends DatasourceService {

  public static final String METADATA_EXT = ".xmi";
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final String DATASOURCE_NAME = "datasourceName";
  private static final String ENABLE_XMLA = "EnableXmla";
  private static final String PARAMETERS = "parameters";
  private static final String DOMAIN_ID = "domain-id";
  private static final String MONDRIAN_MIME_TYPE = "application/vnd.pentaho.mondrian+xml";
  private static final String CATALOG_NAME = "catalogName";
  private static final String UTF_8 = "UTF-8";
  private static final String ZIP_EXTENSION = ".zip";
  private static final String MONDRIAN_FILE_EXTENSION = ".mondrian.xml";
  private static final String ANNOTATIONS_FILE = "annotations.xml";
  private static final Log logger = LogFactory.getLog( AnalysisService.class );
  private static final String ANNOTATION_FOLDER = RepositoryFile.SEPARATOR + "etc"
      + RepositoryFile.SEPARATOR + "mondrian" + RepositoryFile.SEPARATOR;

  /*
   * register the handler in the PentahoSpringObjects.xml for MondrianImportHandler
   */
  protected IPlatformImporter importer;
  protected IAclAwareMondrianCatalogService aclAwareMondrianCatalogService;

  public AnalysisService() {
    importer = PentahoSystem.get( IPlatformImporter.class );
    if ( mondrianCatalogService instanceof IAclAwareMondrianCatalogService ) {
      aclAwareMondrianCatalogService = (IAclAwareMondrianCatalogService) mondrianCatalogService;
    }
  }

  public Map<String, InputStream> doGetAnalysisFilesAsDownload( String analysisId )
    throws PentahoAccessControlException {
    if ( !canManageACL() ) {
      throw new PentahoAccessControlException();
    }

    MondrianCatalogRepositoryHelper helper = createNewMondrianCatalogRepositoryHelper();

    Map<String, InputStream> fileData = helper.getModrianSchemaFiles( analysisId );
    super.parseMondrianSchemaName( analysisId, fileData );

    return fileData;
  }

  public void removeAnalysis( String analysisId ) throws PentahoAccessControlException {
    try {
      ensureDataAccessPermissionCheck();
    } catch ( ConnectionServiceException e ) {
      throw new PentahoAccessControlException();
    }
    mondrianCatalogService.removeCatalog( fixEncodedSlashParam( analysisId ), getSession() );
  }

  public List<String> getAnalysisDatasourceIds() {
    List<String> analysisIds = new ArrayList<String>();
    List<MondrianCatalog> mockMondrianCatalogList = mondrianCatalogService.listCatalogs( getSession(), false );
    Set<String> ids = metadataDomainRepository.getDomainIds();
    for ( MondrianCatalog mondrianCatalog : mockMondrianCatalogList ) {
      String domainId = mondrianCatalog.getName() + METADATA_EXT;
      if ( !ids.contains( domainId ) || !isDSWDatasource( domainId ) ) {
        analysisIds.add( mondrianCatalog.getName() );
      }
    }
    return analysisIds;

  }

  public void putMondrianSchema( InputStream dataInputStream, FormDataContentDisposition schemaFileInfo,
                                 String catalogName, // Optional
                                 String origCatalogName, // Optional
                                 String datasourceName, // Optional
                                 boolean overwrite, boolean xmlaEnabledFlag, String parameters,
                                 RepositoryFileAclDto acl )
    throws PentahoAccessControlException,
    PlatformImportException, Exception {

    accessValidation();
    String fileName = schemaFileInfo.getFileName();
    // sanity check to prevent common mistake - import of .xmi files.
    // See BISERVER-12815
    fileNameValidation( fileName );

    ZipInputStream zis = null;
    ByteArrayOutputStream mondrian = null;
    ByteArrayOutputStream annotations = null;
    if ( fileName.endsWith( ZIP_EXTENSION ) ) {
      zis = new ZipInputStream( dataInputStream );
      ZipEntry ze = null;
      int len = 0;
      while ( ( ze = zis.getNextEntry() ) != null ) {
        if ( ze.getName().endsWith( MONDRIAN_FILE_EXTENSION ) ) {
          IOUtils.copy( zis, mondrian = new ByteArrayOutputStream() );
        } else if ( ze.getName().equals( ANNOTATIONS_FILE ) ) {
          IOUtils.copy( zis, annotations = new ByteArrayOutputStream() );
        }
        zis.closeEntry();
      }
      if ( mondrian != null ) {
        dataInputStream = new ByteArrayInputStream( mondrian.toByteArray() );
      }
    }
    try {
      processMondrianImport(
        dataInputStream, catalogName, origCatalogName, overwrite, xmlaEnabledFlag, parameters, fileName, acl );
      if ( annotations != null ) {
        String catName = ( catalogName != null ) ? catalogName : fileName.substring( 0, fileName.indexOf( '.' ) );
        ByteArrayInputStream annots = new ByteArrayInputStream( annotations.toByteArray() );
        IPlatformImportBundle mondrianBundle = new RepositoryFileImportBundle.Builder()
          .input( annots ).path( ANNOTATION_FOLDER + catName )
          .name( ANNOTATIONS_FILE ).charSet( "UTF-8" ).overwriteFile( true )
          .mime( "text/xml" ) .withParam( "domain-id", catName )
          .build();
          // do import
        importer.importFile( mondrianBundle );
        logger.debug( "imported mondrian annotations" );
        annots.close();
      }
    } finally {
      if ( zis != null ) {
        zis.close();
      }
      if ( mondrian != null ) {
        mondrian.close();
      }
      if ( annotations != null ) {
        annotations.close();
      }
    }
  }

  public RepositoryFileAclDto getAnalysisDatasourceAcl( String analysisId )
      throws PentahoAccessControlException, FileNotFoundException {
    checkAnalysisExists( analysisId );

    if ( aclAwareMondrianCatalogService != null ) {
      final RepositoryFileAcl acl = aclAwareMondrianCatalogService.getAclFor( analysisId );
      return acl == null ? null : repositoryFileAclAdapter.marshal( acl );
    }
    return null;
  }

  public void setAnalysisDatasourceAcl( String analysisId, RepositoryFileAclDto aclDto )
      throws PentahoAccessControlException, FileNotFoundException {
    checkAnalysisExists( analysisId );

    final RepositoryFileAcl acl = aclDto == null ? null : repositoryFileAclAdapter.unmarshal( aclDto );
    if ( aclAwareMondrianCatalogService != null ) {
      aclAwareMondrianCatalogService.setAclFor( analysisId, acl );
    }
    flushDataSources();
  }

  private void checkAnalysisExists( String analysisId ) throws FileNotFoundException, PentahoAccessControlException {
    if ( !canManageACL() ) {
      throw new PentahoAccessControlException();
    }
    if ( mondrianCatalogService.getCatalog( analysisId, PentahoSessionHolder.getSession() ) == null ) {
      throw new FileNotFoundException( analysisId + " doesn't exist" );
    }
  }

  /**
   * This is the main method that handles the actual Import Handler to persist to PUR
   *
   * @param dataInputStream
   * @param catalogName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @param fileName
   * @param acl acl information for the data source. This parameter is optional.
   * @throws PlatformImportException
   */
  protected void processMondrianImport( InputStream dataInputStream, String catalogName, String origCatalogName,
                                        boolean overwrite, boolean xmlaEnabledFlag, String parameters, String fileName,
                                        RepositoryFileAclDto acl )
    throws PlatformImportException {
    boolean overWriteInRepository = determineOverwriteFlag( parameters, overwrite );
    IPlatformImportBundle bundle =
        createPlatformBundle(
          parameters, dataInputStream, catalogName, overWriteInRepository, fileName, xmlaEnabledFlag, acl );
    if ( isChangeCatalogName( origCatalogName, bundle ) ) {
      IMondrianCatalogService catalogService =
          PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
      catalogService.removeCatalog( origCatalogName, PentahoSessionHolder.getSession() );
    }
    if ( isOverwriteAnnotations( parameters, overWriteInRepository ) ) {
      IMondrianCatalogService catalogService =
          PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
      MondrianCatalog catalog = catalogService.getCatalog( bundle.getName(), PentahoSessionHolder.getSession() );
      if ( catalog != null ) {
        catalogService.removeCatalog( bundle.getName(), PentahoSessionHolder.getSession() );
      }
    }
    importer.importFile( bundle );
  }


  private boolean isChangeCatalogName( final String origCatalogName, final IPlatformImportBundle bundle ) {
    // MONDRIAN-1731
    // we are importing a mondrian catalog with a new schema (during edit), remove the old catalog first
    // processing the bundle without doing this will result in a new catalog, giving the effect of adding
    // a catalog rather than editing
    return !StringUtils.isEmpty( origCatalogName ) && !bundle.getName().equals( origCatalogName );
  }

  private boolean isOverwriteAnnotations( final String parameters, final boolean overWriteInRepository ) {
    return overWriteInRepository && !"true".equalsIgnoreCase( getValue( parameters, "retainInlineAnnotations" ) );
  }

  /**
   * helper method to calculate the overwrite in repos flag from parameters or passed value
   *
   * @param parameters
   * @param overWriteInRepository
   * @return boolean if overwrite is allowed
   */
  private boolean determineOverwriteFlag( String parameters, boolean overWriteInRepository ) {
    String overwriteStr = getValue( parameters, OVERWRITE_IN_REPOS );
    if ( overwriteStr != null ) {
      overWriteInRepository = "True".equalsIgnoreCase( overwriteStr ) ? true : false;
    } // if there is a conflict - parameters win?
    return overWriteInRepository;
  }

  /**
   * helper method to create the platform bundle used by the Jcr repository
   *
   * @param parameters
   * @param dataInputStream
   * @param catalogName
   * @param overWriteInRepository
   * @param fileName
   * @param xmlaEnabled
   * @param acl acl information for the data source. This parameter is optional.
   * @return IPlatformImportBundle
   */
  private IPlatformImportBundle createPlatformBundle( String parameters, InputStream dataInputStream,
                                                      String catalogName, boolean overWriteInRepository,
                                                      String fileName, boolean xmlaEnabled, RepositoryFileAclDto acl ) {

    byte[] bytes = null;
    try {
      bytes = IOUtils.toByteArray( dataInputStream );
      if ( bytes.length == 0 && catalogName != null ) {
        MondrianCatalogRepositoryHelper helper =
            new MondrianCatalogRepositoryHelper( PentahoSystem.get( IUnifiedRepository.class ) );
        Map<String, InputStream> fileData = helper.getModrianSchemaFiles( catalogName );
        dataInputStream = fileData.get( "schema.xml" );
        bytes = IOUtils.toByteArray( dataInputStream );
      }
    } catch ( IOException e ) {
      logger.error( e );
    }

    String datasource = getValue( parameters, "Datasource" );
    String domainId =
        this.determineDomainCatalogName( parameters, catalogName, fileName, new ByteArrayInputStream( bytes ) );
    String sep = ";";
    if ( StringUtils.isEmpty( parameters ) ) {
      parameters = "Provider=mondrian";
      parameters += sep + DATASOURCE_NAME + "=" + datasource;
      parameters += sep + ENABLE_XMLA + "=" + xmlaEnabled;
    }

    RepositoryFileImportBundle.Builder bundleBuilder =
        new RepositoryFileImportBundle.Builder().input( new ByteArrayInputStream( bytes ) ).charSet( UTF_8 ).hidden(
        false ).name( domainId ).overwriteFile( overWriteInRepository ).mime( MONDRIAN_MIME_TYPE ).withParam(
        PARAMETERS, parameters ).withParam( DOMAIN_ID, domainId );
    if ( acl != null ) {
      bundleBuilder.acl( repositoryFileAclAdapter.unmarshal( acl ) ).applyAclSettings( true );
    }
    bundleBuilder.withParam( ENABLE_XMLA, Boolean.toString( xmlaEnabled ) );

    IPlatformImportBundle bundle = bundleBuilder.build();
    return bundle;
  }

  /**
   * convert string to property to do a lookup "Provider=Mondrian;DataSource=Pentaho"
   *
   * @param parameters
   * @param key
   * @return
   */
  private String getValue( String parameters, String key ) {
    mondrian.olap.Util.PropertyList propertyList = mondrian.olap.Util.parseConnectString( parameters );
    return propertyList.get( key );
  }

  // for unit test
  XMLInputFactory getXMLInputFactory() {
    return XMLInputFactory.newInstance();
  }

  String getSchemaName( String encoding, InputStream inputStream ) throws XMLStreamException, IOException {
    String domainId = null;
    XMLStreamReader reader = null;
    try {
      XMLInputFactory factory = getXMLInputFactory();
      factory.setProperty( XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false );
      factory.setProperty( XMLInputFactory.IS_COALESCING, Boolean.TRUE );
      if ( StringUtils.isEmpty( encoding ) ) {
        reader = factory.createXMLStreamReader( inputStream );
      } else {
        reader = factory.createXMLStreamReader( inputStream, encoding );
      }

      while ( reader.next() != XMLStreamReader.END_DOCUMENT ) {
        if ( reader.getEventType() == XMLStreamReader.START_ELEMENT
            && reader.getLocalName().equalsIgnoreCase( "Schema" ) ) {
          domainId = reader.getAttributeValue( "", "name" );
          if ( domainId == null ) {
            domainId = reader.getAttributeValue( null, "name" );
          }
          return domainId;
        }
      }
    } finally {
      if ( reader != null ) {
        reader.close();
      }
      inputStream.reset();
    }

    return domainId;
  }
  /**
   * helper method to calculate the domain id from the parameters, file name, or pass catalog
   *
   * @param parameters
   * @param catalogName
   * @param fileName
   * @return Look up name from parameters or file name or passed in catalog name
   */
  private String determineDomainCatalogName( String parameters, String catalogName, String fileName,
                                             InputStream inputStream ) {
    /*
     * Try to resolve the domainId out of the mondrian schema name. If not present then use the catalog name parameter
     * or finally the file name.
     */
    String domainId = null;
    try {
      domainId = getSchemaName( null, inputStream );
    } catch ( Exception e ) {
      try {
        domainId = getSchemaName( UTF_8, inputStream );
      } catch ( Exception e1 ) {
        logger.error( e1 );
      }
    }
    if ( !StringUtils.isEmpty( domainId ) ) {
      return domainId;
    }

    domainId = ( getValue( parameters, CATALOG_NAME ) == null ) ? catalogName : getValue( parameters, CATALOG_NAME );
    if ( domainId == null || "".equals( domainId ) ) {
      if ( fileName.contains( "." ) ) {
        domainId = fileName.substring( 0, fileName.indexOf( "." ) );
      } else {
        domainId = fileName;
      }
    } else {
      if ( domainId.contains( "." ) ) {
        domainId = domainId.substring( 0, domainId.indexOf( "." ) );
      }
    }
    return domainId;
  }

  protected MondrianCatalogRepositoryHelper createNewMondrianCatalogRepositoryHelper() {
    return new MondrianCatalogRepositoryHelper( PentahoSystem.get( IUnifiedRepository.class ) );
  }

  protected boolean canAdministerCheck() {
    return super.canAdminister();
  }

  protected void ensureDataAccessPermissionCheck() throws ConnectionServiceException {
    super.ensureDataAccessPermission();
  }

  protected void accessValidation() throws PentahoAccessControlException {
    super.validateAccess();
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  private void fileNameValidation( final String fileName ) throws PlatformImportException {
    if ( fileName == null ) {
      throw new PlatformImportException( Messages.getString( "AnalysisService.ERROR_001_ANALYSIS_DATASOURCE_ERROR" ),
          PlatformImportException.PUBLISH_GENERAL_ERROR );
    }
    if ( fileName.endsWith( METADATA_EXT ) ) {
      throw new PlatformImportException( Messages.getString( "AnalysisService.ERROR_002_ANALYSIS_DATASOURCE_ERROR" ),
          PlatformImportException.PUBLISH_GENERAL_ERROR );
    }
  }
}
