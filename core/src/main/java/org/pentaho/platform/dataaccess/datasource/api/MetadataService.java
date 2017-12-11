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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataTempFilesListBundleDto;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataTempFilesListDto;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.metadata.IAclAwarePentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.web.http.api.resources.FileResource;
import org.pentaho.platform.web.servlet.UploadFileUtils;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;

public class MetadataService extends DatasourceService {

  private static final String XMI_EXTENSION = ".xmi";

  protected IAclAwarePentahoMetadataDomainRepositoryImporter aclAwarePentahoMetadataDomainRepositoryImporter;

  private static final Log logger = LogFactory.getLog( MetadataService.class );
  private static String upload_dir;

  public static String getUploadDir() {
    if ( upload_dir == null ) {
      IApplicationContext context = PentahoSystem.getApplicationContext();
      if ( context != null ) {
        upload_dir = PentahoSystem.getApplicationContext().getSolutionPath( "system/tmp" );
      } else {
        return "";
      }
    }
    return upload_dir;
  }

  protected String internalGetUploadDir() {
    return MetadataService.getUploadDir();
  }

  public MetadataService() {
    if ( metadataDomainRepository instanceof IAclAwarePentahoMetadataDomainRepositoryImporter ) {
      aclAwarePentahoMetadataDomainRepositoryImporter = (IAclAwarePentahoMetadataDomainRepositoryImporter) metadataDomainRepository;
    }
  }

  public void removeMetadata( String metadataId ) throws PentahoAccessControlException {
    try {
      ensureDataAccessPermissionCheck();
    } catch ( ConnectionServiceException e ) {
      throw new PentahoAccessControlException();
    }
    metadataDomainRepository.removeDomain( metadataId );
  }

  public List<String> getMetadataDatasourceIds() {
    List<String> metadataIds = new ArrayList<String>();
    try {
      sleep( 100 );
      for ( String id : metadataDomainRepository.getDomainIds() ) {
        if ( isMetadataDatasource( id ) ) {
          metadataIds.add( id );
        }
      }
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    }
    return metadataIds;
  }

  public MetadataTempFilesListDto uploadMetadataFilesToTempDir( InputStream metadataFile,
      List<InputStream> localeFileStreams, List<String> localeFileNames ) throws Exception {

    String fileName = uploadFile( metadataFile );
    MetadataTempFilesListDto dto = new MetadataTempFilesListDto();

    dto.setXmiFileName( fileName );
    logger.info( "metadata file uploaded: " + fileName );

    if ( localeFileStreams != null && localeFileStreams.size() != 0 ) {
      List<MetadataTempFilesListBundleDto> bundles = new ArrayList<MetadataTempFilesListBundleDto>();
      int cntr = 0;
      for ( InputStream inputStream : localeFileStreams ) {
        fileName = uploadFile( inputStream );

        MetadataTempFilesListBundleDto bundle = new MetadataTempFilesListBundleDto(
            localeFileNames.get( cntr ),
            fileName );
        bundles.add( bundle );

        logger.info( "locale file uploaded: " + fileName );
        cntr++;
      }
      dto.setBundles( bundles );
    }

    return dto;
  }

  protected String uploadFile( InputStream is ) throws Exception {
    StringWriter fileNameWriter = new StringWriter();
    UploadFileUtils utils = new UploadFileUtils( PentahoSessionHolder.getSession() );

    utils.setShouldUnzip( false );
    utils.setTemporary( true );
    utils.setFileName( UUIDUtil.getUUID().toString() );
    utils.setWriter( fileNameWriter );
    utils.process( is );
    return fileNameWriter.toString();

  }

  public MetadataTempFilesListDto uploadMetadataFilesToTempDir( InputStream metadataFile,
      List<FormDataBodyPart> localeFiles ) throws Exception {


    List<InputStream> bundles = null;
    List<String> fileNames = null;

    if ( localeFiles != null && localeFiles.size() != 0 ) {
      bundles = new ArrayList<InputStream>();
      fileNames = new ArrayList<String>();
      for ( FormDataBodyPart localeFile : localeFiles ) {
        InputStream inputStream = new ByteArrayInputStream( localeFile.getValueAs( byte[].class ) );
        bundles.add( inputStream );
        fileNames.add( localeFile.getFormDataContentDisposition().getFileName() );
      }
    }

    return uploadMetadataFilesToTempDir( metadataFile, bundles, fileNames );
  }

  public void importMetadataDatasource( String domainId, InputStream metadataFile,
                                        FormDataContentDisposition metadataFileInfo, boolean overwrite,
                                        List<FormDataBodyPart> localeFiles,
                                        List<FormDataContentDisposition> localeFilesInfo, RepositoryFileAclDto acl )
    throws PentahoAccessControlException, PlatformImportException,
    Exception {
    if ( StringUtils.isEmpty( domainId ) ) {
      throw new PlatformImportException( Messages.getString( "MetadataDatasourceService.ERROR_005_DOMAIN_NAME_EMPTY" ) );
    }
    List<InputStream> localeFileStreams = null;
    List<String> localeFileNames = null;

    if ( localeFiles != null ) {
      localeFileStreams = new ArrayList<InputStream>();
      localeFileNames = new ArrayList<String>();

      for ( int i = 0; i < localeFiles.size(); i++ ) {
        logger.info( "create language file" );
        InputStream inputStream = createNewByteArrayInputStream( localeFiles.get( i ).getValueAs( byte[].class ) );
        localeFileStreams.add( inputStream );
        localeFileNames.add( localeFilesInfo.get( i ).getFileName() );
      }
    }

    importMetadataDatasource( domainId, metadataFile, overwrite, localeFileStreams, localeFileNames, acl );

  }

  public void importMetadataDatasource( String domainId, InputStream metadataFile, boolean overwrite,
      List<InputStream> localeFileStreams, List<String> localeFileNames, RepositoryFileAclDto acl )
    throws PentahoAccessControlException, PlatformImportException, Exception {
    if ( StringUtils.isEmpty( domainId ) ) {
      throw new PlatformImportException( Messages.getString( "MetadataDatasourceService.ERROR_005_DOMAIN_NAME_EMPTY" ) );
    }
    accessValidation();

    FileResource fr = createNewFileResource();
    Object reservedCharsObject = fr.doGetReservedChars().getEntity();
    String reservedChars = objectToString( reservedCharsObject );
    if ( reservedChars != null
        && domainId.matches( ".*[" + reservedChars.replaceAll( "/", "" ) + "]+.*" ) ) {
      String msg = prohibitedSymbolMessage( domainId, fr );
      throw new PlatformImportException( msg, PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
    }

    metadataFile = validateFileSize( metadataFile, domainId );

    // domain ID comes with ".xmi" suffix when creating or editing domain
    // (see ModelerService.serializeModels( Domain, String, boolean ) ),
    // but when the user enters domain ID manually when importing metadata file,
    // it will unlikely contain that suffix, so let's add it forcibly.
    domainId = forceXmiSuffix( domainId );

    RepositoryFileImportBundle.Builder bundleBuilder = createNewRepositoryFileImportBundleBuilder( metadataFile, overwrite, domainId, acl );

    if ( localeFileStreams != null ) {
      for ( int i = 0; i < localeFileStreams.size(); i++ ) {
        IPlatformImportBundle localizationBundle =  createNewRepositoryFileImportBundle( localeFileStreams.get( i ), localeFileNames.get( i ), domainId );
        bundleBuilder.addChildBundle( localizationBundle );
      }
    }

    IPlatformImportBundle bundle = bundleBuilder.build();
    IPlatformImporter importer = getImporter();
    importer.importFile( bundle );
    IPentahoSession pentahoSession = getSession();
    publish( pentahoSession );
  }

  public boolean isContainsModel( String tempFileName ) throws Exception {
    XmiParser xmiParser = new XmiParser();
    byte[] is = IOUtils.toByteArray( createInputStreamFromFile( internalGetUploadDir() + File.separatorChar + tempFileName ) );
    Domain domain = xmiParser.parseXmi( new java.io.ByteArrayInputStream( is ) );
    return isContainsModel( domain );
  }

  protected  boolean isContainsModel( Domain domain ) throws Exception {
    return !DatasourceService.isMetadataDatasource( domain ) && domain.getLogicalModels().size() > 1;
  }

  public void importMetadataFromTemp( String domainId, MetadataTempFilesListDto fileList,
      boolean overwrite, RepositoryFileAclDto acl ) throws PentahoAccessControlException, PlatformImportException, Exception {

    String metadataTempFileName = fileList.getXmiFileName();
    InputStream metaDataFileInputStream = createInputStreamFromFile( internalGetUploadDir() + File.separatorChar + metadataTempFileName );
    List<MetadataTempFilesListBundleDto> locBundles = fileList.getBundles();
    List<String> localeFileNames = new ArrayList<String>();
    List<InputStream> localeFileStreams = new ArrayList<InputStream>();

    if ( locBundles != null ) {
      for ( MetadataTempFilesListBundleDto bundle : locBundles ) {
        localeFileNames.add( bundle.getOriginalFileName() );
        localeFileStreams.add( createInputStreamFromFile( internalGetUploadDir() + File.separatorChar + bundle.getTempFileName() ) );
      }
    }

    importMetadataDatasource( domainId, metaDataFileInputStream, overwrite, localeFileStreams, localeFileNames, acl );

  }

  @VisibleForTesting
  InputStream validateFileSize( InputStream metadataFile, String domainId )
    throws IOException, PlatformImportException {
    // maxFileLimit is 10 Mb by default
    String maxFileLimit = PentahoSystem
      .getSystemSetting( "file-upload-defaults/max-file-limit", String.valueOf( 10000000 ) );  //$NON-NLS-1$
    byte[] bytes = IOUtils.toByteArray( metadataFile );

    if ( Long.parseLong( maxFileLimit ) < bytes.length ) {
      String msg = Messages.getString( "MetadataDatasourceService.ERROR_004_MAX_FILE_SIZE_EXCEEDED_ERROR", domainId );
      throw new PlatformImportException( msg, PlatformImportException.PUBLISH_DATASOURCE_ERROR );
    }
    return new ByteArrayInputStream( bytes );
  }

  public RepositoryFileAclDto getMetadataAcl( String domainId )
    throws PentahoAccessControlException, FileNotFoundException {
    checkMetadataExists( domainId );
    if ( aclAwarePentahoMetadataDomainRepositoryImporter != null ) {
      final RepositoryFileAcl acl = aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId );
      return acl == null ? null : repositoryFileAclAdapter.marshal( acl );
    }
    return null;
  }

  public void setMetadataAcl( String domainId, RepositoryFileAclDto aclDto )
    throws PentahoAccessControlException, FileNotFoundException {
    checkMetadataExists( domainId );
    if ( aclAwarePentahoMetadataDomainRepositoryImporter != null ) {
      final RepositoryFileAcl acl = aclDto == null ? null : repositoryFileAclAdapter.unmarshal( aclDto );
      aclAwarePentahoMetadataDomainRepositoryImporter.setAclFor( domainId, acl );
      flushDataSources();
    }
  }

  private void checkMetadataExists( String domainId ) throws PentahoAccessControlException, FileNotFoundException {
    if ( !canManageACL() ) {
      throw new PentahoAccessControlException();
    }
    if ( metadataDomainRepository instanceof IPentahoMetadataDomainRepositoryExporter ) {
      Map<String, InputStream> domainFilesData =
          ( (IPentahoMetadataDomainRepositoryExporter) metadataDomainRepository ).getDomainFilesData( domainId );
      if ( domainFilesData == null || domainFilesData.isEmpty() ) {
        throw new FileNotFoundException();
      }
    }
  }

  protected void sleep( int i ) throws InterruptedException {
    Thread.sleep( i );
  }

  protected String prohibitedSymbolMessage( String domainId, FileResource fr ) throws InterruptedException {
    String illegalCharacterList = (String) fr.doGetReservedCharactersDisplay().getEntity();
    //For metadata \ is a legal character and must be removed from the message list before returning the message list to the user
    illegalCharacterList = illegalCharacterList.replaceAll( "\\,", "" );
    return Messages.getString( "MetadataDatasourceService.ERROR_003_PROHIBITED_SYMBOLS_ERROR", domainId, illegalCharacterList );
  }

  protected String objectToString( Object o ) throws InterruptedException {
    return (String) o;
  }

  protected void publish( IPentahoSession pentahoSession ) throws InterruptedException {
    PentahoSystem.publish( pentahoSession, org.pentaho.platform.engine.services.metadata.MetadataPublisher.class.getName() );
  }

  protected IPentahoSession getSession() throws InterruptedException {
    return PentahoSessionHolder.getSession();
  }

  protected IPlatformImporter getImporter() throws InterruptedException {
    return PentahoSystem.get( IPlatformImporter.class );
  }

  protected void accessValidation() throws PentahoAccessControlException {
    super.validateAccess();
  }

  protected boolean canAdministerCheck() {
    return super.canAdminister();
  }

  protected void ensureDataAccessPermissionCheck() throws ConnectionServiceException {
    super.ensureDataAccessPermission();
  }

  protected FileResource createNewFileResource() {
    return new FileResource();
  }

  protected RepositoryFileImportBundle.Builder createNewRepositoryFileImportBundleBuilder( InputStream metadataFile,
      boolean overWriteInRepository, String domainId, RepositoryFileAclDto acl ) {
    final RepositoryFileImportBundle.Builder
        builder =
        new RepositoryFileImportBundle.Builder().input( metadataFile ).charSet( "UTF-8" ).hidden( false )
            .overwriteFile( overWriteInRepository ).mime( "text/xmi+xml" ).withParam( "domain-id", domainId );
    if ( acl != null ) {
      builder.acl( repositoryFileAclAdapter.unmarshal( acl ) ).applyAclSettings( true );
    }
    return builder;
  }


  protected InputStream createInputStreamFromFile( String fileName ) throws FileNotFoundException {
    return new FileInputStream( fileName );
  }

  public static RepositoryFileImportBundle createNewRepositoryFileImportBundle( InputStream bais, String fileName, String domainId ) {
    return new RepositoryFileImportBundle.Builder().input( bais ).charSet( "UTF-8" ).hidden( false )
      .name( fileName ).withParam( "domain-id", domainId )
      .build();
  }

  protected ByteArrayInputStream createNewByteArrayInputStream( byte[] buf ) {
    if ( buf != null ) {
      return new ByteArrayInputStream( buf );
    } else {
      return null;
    }
  }

  private static String forceXmiSuffix( String domainId ) {
    if ( domainId.endsWith( XMI_EXTENSION ) ) {
      return domainId;
    } else {
      return domainId + XMI_EXTENSION;
    }
  }
}
