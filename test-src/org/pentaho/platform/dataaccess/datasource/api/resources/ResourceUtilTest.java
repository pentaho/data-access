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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceUtilTest {

  private static ResourceUtil resourceUtil;

  @Before
  public void setUp() {
    resourceUtil = spy( new ResourceUtil() );
  }

  @After
  public void cleanup() {
    resourceUtil = null;
  }

  @Test
  public void testCreateAttachment() throws Exception {
    Map<String, InputStream> fileData = new TreeMap<String, InputStream>();
    InputStream mockInputStream = mock( InputStream.class );
    InputStream mockInputStream2 = mock( InputStream.class );
    fileData.put( "file1", mockInputStream );
    String domainId = "domainId";
    StreamingOutput mockStreamingOutput = mock( StreamingOutput.class );
    Response mockResponse = mock( Response.class );
    File mockFile = mock( File.class );
    ZipOutputStream mockZipOutputStream = mock( ZipOutputStream.class );
    ZipEntry mockZipEntry = mock( ZipEntry.class );
    ZipEntry mockZipEntry2 = mock( ZipEntry.class );
    FileInputStream mockFileInputStream = mock( FileInputStream.class );

    //Test 1
    doReturn( MediaType.TEXT_PLAIN ).when( resourceUtil ).getMimeType( fileData.get( "file1" ) );
    doReturn( mockStreamingOutput ).when( resourceUtil ).getStreamingOutput( fileData.get( "file1" ) );
    doReturn( mockResponse ).when( resourceUtil ).buildOkResponse( mockStreamingOutput, "text/plain", "\"file1\"" );

    Response response = resourceUtil.createAttachment( fileData, domainId );
    assertEquals( mockResponse, response );

    //Test 2
    fileData.put( "file2", mockInputStream2 );
    doReturn( mockFile ).when( resourceUtil ).createTempFile( "datasourceExport", ".zip" );
    doReturn( mockZipOutputStream ).when( resourceUtil ).createZipOutputStream( mockFile );
    doReturn( mockZipEntry ).when( resourceUtil ).createZipEntry( "file1" );
    doReturn( mockZipEntry2 ).when( resourceUtil ).createZipEntry( "file2" );
    doNothing().when( resourceUtil ).copy( mockInputStream, mockZipOutputStream );
    doNothing().when( resourceUtil ).copy( mockInputStream2, mockZipOutputStream );
    doReturn( mockFileInputStream ).when( resourceUtil ).createFileInputStream( mockFile );
    doReturn( mockStreamingOutput ).when( resourceUtil ).createStreamingOutput( mockFileInputStream );
    doReturn( mockResponse ).when( resourceUtil ).buildOkResponse( mockStreamingOutput, resourceUtil.APPLICATION_ZIP, "\"domainId.zip\"" );

    response = resourceUtil.createAttachment( fileData, domainId );
    assertEquals( mockResponse, response );

    verify( resourceUtil, times( 2 ) ).createAttachment( fileData, domainId );
  }

  @Test
  public void testCreateAttachmentError() throws Exception {
    Map<String, InputStream> fileData = new TreeMap<String, InputStream>();
    InputStream mockInputStream = mock( InputStream.class );
    InputStream mockInputStream2 = mock( InputStream.class );
    String domainId = "domainId";
    Response mockResponse = mock( Response.class );

    //Test 1
    doReturn( mockResponse ).when( resourceUtil ).buildServerErrorResponse();

    Response response = resourceUtil.createAttachment( fileData, domainId );
    assertEquals( mockResponse, response );

    //Test 2
    fileData.put( "file1", mockInputStream );
    fileData.put( "file2", mockInputStream2 );
    IOException mockIOException = mock( IOException.class );
    doThrow( mockIOException ).when( resourceUtil ).createTempFile( "datasourceExport", ".zip" );
    doReturn( mockResponse ).when( resourceUtil ).buildServerErrorResponse( mockIOException );

    response = resourceUtil.createAttachment( fileData, domainId );
    assertEquals( mockResponse, response );

    //Test 3
    File mockFile = mock( File.class );
    ZipOutputStream mockZipOutputStream = mock( ZipOutputStream.class );
    ZipEntry mockZipEntry = mock( ZipEntry.class );
    ZipEntry mockZipEntry2 = mock( ZipEntry.class );
    FileInputStream mockFileInputStream = mock( FileInputStream.class );
    StreamingOutput mockStreamingOutput = mock( StreamingOutput.class );
    doReturn( mockFile ).when( resourceUtil ).createTempFile( "datasourceExport", ".zip" );
    doReturn( mockZipOutputStream ).when( resourceUtil ).createZipOutputStream( mockFile );
    doReturn( mockZipEntry ).when( resourceUtil ).createZipEntry( "file1" );
    doReturn( mockZipEntry2 ).when( resourceUtil ).createZipEntry( "file2" );
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( resourceUtil ).copy( mockInputStream, mockZipOutputStream );
    doThrow( mockException ).when( resourceUtil ).copy( mockInputStream2, mockZipOutputStream );
    doReturn( mockFileInputStream ).when( resourceUtil ).createFileInputStream( mockFile );
    doReturn( mockStreamingOutput ).when( resourceUtil ).createStreamingOutput( mockFileInputStream );
    doReturn( mockResponse ).when( resourceUtil ).buildOkResponse( mockStreamingOutput, resourceUtil.APPLICATION_ZIP,
        "\"domainId.zip\"" );

    response = resourceUtil.createAttachment( fileData, domainId );
    assertEquals( mockResponse, response );

    verify( resourceUtil, times( 3 ) ).createAttachment( fileData, domainId );
  }
}

