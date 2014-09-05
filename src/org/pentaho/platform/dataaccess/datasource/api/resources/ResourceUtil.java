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
import java.io.FileNotFoundException;
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

  public Response createAttachment( Map<String, InputStream> fileData, String domainId ) {
    String quotedFileName = null;
    final InputStream is;
    if ( fileData.size() > 1 ) { // we've got more than one file so we want to zip them up and send them
      File zipFile = null;
      try {
        zipFile = createTempFile( "datasourceExport", ".zip" );
        zipFile.deleteOnExit();
        ZipOutputStream zos = createZipOutputStream( zipFile );
        for ( String fileName : fileData.keySet() ) {
          InputStream zipEntryIs = null;
          try {
            ZipEntry entry = createZipEntry( fileName );
            zos.putNextEntry( entry );
            zipEntryIs = fileData.get( fileName );
            copy( zipEntryIs, zos );
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
        is = createFileInputStream( zipFile );
      } catch ( IOException ioe ) {
        return buildServerErrorResponse( ioe );
      }
      StreamingOutput streamingOutput = createStreamingOutput( is );
      final int xmiIndex = domainId.lastIndexOf( ".xmi" ); //$NON-NLS-1$
      quotedFileName =
        "\"" + ( xmiIndex > 0 ? domainId.substring( 0, xmiIndex ) : domainId ) + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      return buildOkResponse( streamingOutput, APPLICATION_ZIP, quotedFileName );
    } else if ( fileData.size() == 1 ) {  // we've got a single metadata file so we just return that.
      String fileName = (String) fileData.keySet().toArray()[ 0 ];
      quotedFileName = "\"" + fileName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
      is = fileData.get( fileName );
      String mimeType = getMimeType( is );
      StreamingOutput streamingOutput = getStreamingOutput( is );
      return buildOkResponse( streamingOutput, mimeType, quotedFileName );
    }
    return buildServerErrorResponse();
  }

  protected StreamingOutput createStreamingOutput( final InputStream is ) {
    return new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };
  }

  protected FileInputStream createFileInputStream( File zipFile ) throws FileNotFoundException {
    return new FileInputStream( zipFile );
  }

  protected void copy( InputStream zipEntryIs, ZipOutputStream zos ) throws IOException {
    IOUtils.copy( zipEntryIs, zos );
  }

  protected File createTempFile( String fileName, String extension ) throws IOException {
    return File.createTempFile( fileName, extension ); //$NON-NLS-1$ //$NON-NLS-2$;
  }

  protected ZipEntry createZipEntry( String fileName ) {
    return new ZipEntry( fileName );
  }

  protected ZipOutputStream createZipOutputStream( File zipFile ) throws IOException {
    return new ZipOutputStream( new FileOutputStream( zipFile ) );
  }

  protected Response buildOkResponse( StreamingOutput streamingOutput, String mimeType, String quotedFileName ) {
    return Response.ok( streamingOutput, mimeType )
      .header( "Content-Disposition", "attachment; filename=" + quotedFileName ).build(); //$NON-NLS-1$ //$NON-NLS-2$
  }

  protected Response buildServerErrorResponse() {
    return Response.serverError().build();
  }

  protected Response buildServerErrorResponse( Exception ioe ) {
    return Response.serverError().entity( ioe.toString() ).build();
  }

  protected String getMimeType( InputStream is ) {
    return ( is instanceof RepositoryFileInputStream ) ? ( (RepositoryFileInputStream) is ).getMimeType() : MediaType.TEXT_PLAIN;
  }

  protected StreamingOutput getStreamingOutput( final InputStream is ) {
    return new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };
  }
}
