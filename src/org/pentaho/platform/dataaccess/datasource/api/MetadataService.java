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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.metadata.IAclAwarePentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.FileResource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;

public class MetadataService extends DatasourceService {
  protected IAclAwarePentahoMetadataDomainRepositoryImporter aclAwarePentahoMetadataDomainRepositoryImporter;

  private static final Log logger = LogFactory.getLog( MetadataService.class );

  public MetadataService() {
    if ( metadataDomainRepository instanceof IAclAwarePentahoMetadataDomainRepositoryImporter ) {
      aclAwarePentahoMetadataDomainRepositoryImporter = (IAclAwarePentahoMetadataDomainRepositoryImporter) metadataDomainRepository;
    }
  }

  public void removeMetadata( String metadataId ) throws PentahoAccessControlException {
    if ( !canAdministerCheck() ) {
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

  public void importMetadataDatasource( String domainId, InputStream metadataFile,
                                        FormDataContentDisposition metadataFileInfo, boolean overwrite,
                                        List<FormDataBodyPart> localeFiles,
                                        List<FormDataContentDisposition> localeFilesInfo, RepositoryFileAclDto acl )
    throws PentahoAccessControlException, PlatformImportException,
    Exception {

    accessValidation();

    FileResource fr = createNewFileResource();
    Object reservedCharsObject = fr.doGetReservedChars().getEntity();
    String reservedChars = objectToString( reservedCharsObject );
    if ( reservedChars != null
        && domainId.matches( ".*[" + reservedChars.replaceAll( "/", "" ) + "]+.*" ) ) {
      String msg = prohibitedSymbolMessage( domainId, fr );
      throw new PlatformImportException( msg, PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
    }

    RepositoryFileImportBundle.Builder bundleBuilder = createNewRepositoryFileImportBundleBuilder( metadataFile, overwrite, domainId, acl );


    if ( localeFiles != null ) {
      for ( int i = 0; i < localeFiles.size(); i++ ) {
        logger.info( "create language file" );
        ByteArrayInputStream bais = createNewByteArrayInputStream( localeFiles.get( i ).getValueAs( byte[].class ) );
        IPlatformImportBundle localizationBundle =  createNewRepositoryFileImportBundle( bais, localeFilesInfo.get( i ).getFileName(), domainId );
        bundleBuilder.addChildBundle( localizationBundle );
      }
    }

    IPlatformImportBundle bundle = bundleBuilder.build();
    IPlatformImporter importer = getImporter();
    importer.importFile( bundle );
    IPentahoSession pentahoSession = getSession();
    publish( pentahoSession );
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

  protected RepositoryFileImportBundle createNewRepositoryFileImportBundle( ByteArrayInputStream bais, String fileName, String domainId ) {
    return new RepositoryFileImportBundle.Builder().input( bais ).charSet( "UTF-8" ).hidden( false )
      .name( fileName ).withParam( "domain-id", domainId )
      .build();
  }

  protected ByteArrayInputStream createNewByteArrayInputStream( byte[] buf ) {
    return new ByteArrayInputStream( buf );
  }
}
