/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.api.resources;

import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

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

  static class ContentAlreadyExistsException extends WebApplicationException {
    public ContentAlreadyExistsException( String error ) {
      super( Response.status( CONFLICT ).entity( error ).type( "text/plain" ).build() );
    }
  }

  static class UnspecifiedErrorException extends WebApplicationException {
    public UnspecifiedErrorException( String error ) {
      super( Response.status( INTERNAL_SERVER_ERROR ).entity( error ).type( "text/plain" ).build() );
    }
  }

  static class PublishProhibitedException extends WebApplicationException {
    public PublishProhibitedException( String error ) {
      super( Response.status( UNAUTHORIZED ).entity( error ).type( "text/plain" ).build() );
    }
  }

  static class AccessControlException extends WebApplicationException {
    public AccessControlException( String error ) {
      super( Response.status( FORBIDDEN ).entity( error ).type( "text/plain" ).build() );
    }
  }

  static class ImportFailedException extends WebApplicationException {
    public ImportFailedException( String error ) {
      super( Response.status( PRECONDITION_FAILED ).entity( error ).type( "text/plain" ).build() );
    }
  }
}
