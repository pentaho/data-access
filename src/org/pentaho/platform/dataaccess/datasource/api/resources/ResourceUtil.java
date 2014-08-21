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
 * Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;

public class ResourceUtil {

  public static final String APPLICATION_ZIP = "application/zip"; //$NON-NLS-1$

  public static Response createAttachment( Map<String, InputStream> fileData, String domainId ) {
    String quotedFileName = null;
    final InputStream is;
    if ( fileData.size() > 1 ) { // we've got more than one file so we want to zip them up and send them
      File zipFile = null;
      try {
        zipFile = File.createTempFile( "datasourceExport", ".zip" ); //$NON-NLS-1$ //$NON-NLS-2$
        zipFile.deleteOnExit();
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipFile ) );
        for ( String fileName : fileData.keySet() ) {
          InputStream zipEntryIs = null;
          try { 
            ZipEntry entry = new ZipEntry( fileName );
            zos.putNextEntry( entry );
            zipEntryIs = fileData.get( fileName );
            IOUtils.copy( zipEntryIs, zos );
          } catch ( Exception e ) {
            continue;
          } finally {
            zos.closeEntry();
            if ( zipEntryIs != null ) {
              zipEntryIs.close();
            }
          }
        }
        zos.close();
        is = new FileInputStream( zipFile );
      } catch ( IOException ioe ) {
        return Response.serverError().entity( ioe.toString() ).build();
      }
      StreamingOutput streamingOutput = new StreamingOutput() {
        public void write( OutputStream output ) throws IOException {
          IOUtils.copy( is, output );
        }
      };
      final int xmiIndex = domainId.lastIndexOf( ".xmi" );//$NON-NLS-1$
      quotedFileName = "\"" + ( xmiIndex > 0 ? domainId.substring( 0, xmiIndex ) : domainId ) + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      return Response.ok( streamingOutput, APPLICATION_ZIP ).header( "Content-Disposition", "attachment; filename=" + quotedFileName ).build(); //$NON-NLS-1$ //$NON-NLS-2$
    } else if ( fileData.size() == 1 ) {  // we've got a single metadata file so we just return that.
      String fileName = (String) fileData.keySet().toArray()[0];
      quotedFileName = "\"" + fileName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
      is = fileData.get( fileName );
      String mimeType = MediaType.TEXT_PLAIN;
      if (is instanceof RepositoryFileInputStream) {
        mimeType = ( (RepositoryFileInputStream)is ).getMimeType();
      }
      StreamingOutput streamingOutput = new StreamingOutput() {
        public void write( OutputStream output ) throws IOException {
          IOUtils.copy( is, output );
        }
      };
      return Response.ok( streamingOutput, mimeType ).header( "Content-Disposition", "attachment; filename=" + quotedFileName ).build(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return Response.serverError().build();
  }

  public static void parseMondrianSchemaName( String dswId, Map<String, InputStream> fileData ) {
    final String keySchema = "schema.xml";//$NON-NLS-1$
    if ( fileData.containsKey( keySchema ) ) {
      final int xmiIndex = dswId.lastIndexOf( ".xmi" );//$NON-NLS-1$
      fileData.put( ( xmiIndex > 0 ? dswId.substring( 0, xmiIndex ) : dswId ) + ".mondrian.xml", fileData.get( keySchema ) );//$NON-NLS-1$
      fileData.remove( keySchema );
    }
  }  
}
