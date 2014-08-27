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

package org.pentaho.platform.dataaccess.datasource.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.web.http.api.resources.FileResource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;

public class MetadataService extends DatasourceService {

  private static final Log logger = LogFactory.getLog( MetadataService.class );

  public void removeMetadata( String metadataId ) throws PentahoAccessControlException {
    if ( !canAdminister() ) {
      throw new PentahoAccessControlException();
    }
    metadataDomainRepository.removeDomain( fixEncodedSlashParam( metadataId ) );
  }

  public List<String> getMetadataDatasourceIds() {
    List<String> metadataIds = new ArrayList<String>();
    try {
      Thread.sleep( 100 );
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
      FormDataContentDisposition metadataFileInfo, String overwrite, List<FormDataBodyPart> localeFiles,
      List<FormDataContentDisposition> localeFilesInfo ) throws PentahoAccessControlException, PlatformImportException,
    Exception {

    validateAccess();

    FileResource fr = new FileResource();
    String reservedChars = (String) fr.doGetReservedChars().getEntity();
    if ( reservedChars != null
    // \ need to be replaced with \\ for Regex
        && domainId.matches( ".*[" + reservedChars.replaceAll( "\\\\", "\\\\\\\\" ) + "]+.*" ) ) {
      String msg =
          Messages.getString( "MetadataDatasourceService.ERROR_003_PROHIBITED_SYMBOLS_ERROR", domainId, (String) fr
              .doGetReservedCharactersDisplay().getEntity() );
      throw new PlatformImportException( msg, PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
    }

    boolean overWriteInRepository = "True".equalsIgnoreCase( overwrite ) ? true : false;
    RepositoryFileImportBundle.Builder bundleBuilder =
        new RepositoryFileImportBundle.Builder().input( metadataFile ).charSet( "UTF-8" ).hidden( false )
            .overwriteFile( overWriteInRepository ).mime( "text/xmi+xml" ).withParam( "domain-id", domainId );

    if ( localeFiles != null ) {
      for ( int i = 0; i < localeFiles.size(); i++ ) {
        logger.info( "create language file" );
        IPlatformImportBundle localizationBundle =
            new RepositoryFileImportBundle.Builder().input(
                new ByteArrayInputStream( localeFiles.get( i ).getValueAs( byte[].class ) ) ).charSet( "UTF-8" )
                .hidden( false ).name( localeFilesInfo.get( i ).getFileName() ).withParam( "domain-id", domainId )
                .build();

        bundleBuilder.addChildBundle( localizationBundle );
      }
    }

    IPlatformImportBundle bundle = bundleBuilder.build();
    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
    importer.importFile( bundle );
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    PentahoSystem.publish( pentahoSession, org.pentaho.platform.engine.services.metadata.MetadataPublisher.class
        .getName() );
  }
}
