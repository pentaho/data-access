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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataTempFilesListDto;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.metadata.IAclAwarePentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.FileResource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;

public class MetadataServiceTest {

  private static final String TEST_XMI_FILE_PATH = "test.xmi";
  private static final String XMI_TEMP_FILE_NAME = "test_file.xmi";
  private static final String UPLOAD_DIR = "upload";
  private static final String DOMAIN_ID = "home\\admin/resource/";
  private static MetadataService metadataService;

  private class MetadataServiceMock extends MetadataService {

    @Override protected IUnifiedRepository getRepository() {
      return mock( IUnifiedRepository.class );
    }
  }

  @Before
  public void setUp() {
    metadataService = spy( new MetadataServiceMock() );
    metadataService.metadataDomainRepository = mock( PentahoMetadataDomainRepository.class );
    metadataService.aclAwarePentahoMetadataDomainRepositoryImporter =
      mock( IAclAwarePentahoMetadataDomainRepositoryImporter.class );
    metadataService.mondrianCatalogService = mock( IMondrianCatalogService.class );
    metadataService.lock = new DatasourceLock();
  }

  @After
  public void cleanup() {
    metadataService = null;
  }

  @Test
  public void testRemoveMetadata() throws Exception {
    doNothing().when( metadataService ).ensureDataAccessPermissionCheck();
    doReturn( "param" ).when( metadataService ).fixEncodedSlashParam( "metadataId" );
    doNothing().when( metadataService.metadataDomainRepository ).removeDomain( "param" );

    metadataService.removeMetadata( "metadataId" );

    verify( metadataService, times( 1 ) ).removeMetadata( "metadataId" );
    // checking fixEncodedSlashParam method is not called (BISERVER-12403 issue)
    verify( metadataService, never() ).fixEncodedSlashParam( "metadataId" );
  }

  @Test
  public void testRemoveMetadataError() throws Exception {
    ConnectionServiceException cse = new ConnectionServiceException();
    doThrow( cse ).when( metadataService ).ensureDataAccessPermissionCheck();
    try {
      metadataService.removeMetadata( "metadataId" );
      fail();
    } catch ( PentahoAccessControlException e ) {
      //expected
    }
    verify( metadataService, times( 1 ) ).removeMetadata( "metadataId" );
  }

  @Test
  public void testGetMetadataDatasourceIds() throws Exception {
    List<String> mockMetadataIdsList = new ArrayList<String>();
    Set<String> mockSet = new HashSet<String>();
    mockSet.add( "domainId1" );
    mockMetadataIdsList.add( "domainId1" );

    doReturn( true ).when( metadataService ).isMetadataDatasource( "domainId1" );
    doReturn( mockSet ).when( metadataService.metadataDomainRepository ).getDomainIds();

    List<String> response = metadataService.getMetadataDatasourceIds();

    verify( metadataService, times( 1 ) ).getMetadataDatasourceIds();
    assertEquals( mockMetadataIdsList, response );
  }

  @Test
  public void testGetMetadataDatasourceIdsError() throws Exception {
    InterruptedException mockInterruptedException = mock( InterruptedException.class );
    doThrow( mockInterruptedException ).when( metadataService ).sleep( 100 );

    metadataService.getMetadataDatasourceIds();

    verify( metadataService, times( 1 ) ).getMetadataDatasourceIds();
  }

  @Test( expected = PlatformImportException.class )
  public void testImportMetadataDatasourceMaxFileSize() throws Exception {
    // test should work in case max-file-limit is set
    int fileDefaultSize = 10000000;
    String maxFileLimit = PentahoSystem
      .getSystemSetting( "file-upload-defaults/max-file-limit", String.valueOf( fileDefaultSize ) );  //$NON-NLS-1$

    assertEquals( fileDefaultSize, Integer.parseInt( maxFileLimit ) );

    // fileDefaultSize will be exceeded
    byte[] bytes = new byte[ fileDefaultSize + 1 ];
    InputStream metadataFile = new ByteArrayInputStream( bytes );

    FileResource mockFileResource = mock( FileResource.class );
    Response mockResponse = mock( Response.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();

    metadataService.importMetadataDatasource( "test_file", metadataFile, null, false, null, null, null );
  }

  @Test
  public void testImportMetadataDatasource() throws Exception {
    String domainId = DOMAIN_ID;
    InputStream metadataFile = mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = mock( FormDataContentDisposition.class );
    boolean overwrite = true;
    FormDataBodyPart mockFormDataBodyPart = mock( FormDataBodyPart.class );
    List<FormDataBodyPart> localeFiles = new ArrayList<FormDataBodyPart>();
    localeFiles.add( mockFormDataBodyPart );
    List<FormDataContentDisposition> localeFilesInfo = new ArrayList<FormDataContentDisposition>();
    FormDataContentDisposition mockFormDataContentDisposition = mock( FormDataContentDisposition.class );
    localeFilesInfo.add( mockFormDataContentDisposition );
    FileResource mockFileResource = mock( FileResource.class );
    Response mockResponse = mock( Response.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );
    IPlatformImportBundle mockIPlatformImportBundle = mock( IPlatformImportBundle.class );
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder =
      mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = mock( ByteArrayInputStream.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( metadataFile ).when( metadataService ).validateFileSize( any( InputStream.class ), anyString() );
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService )
      .createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    doReturn( "fileName" ).when( mockFormDataContentDisposition ).getFileName();
    doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( any( byte[].class ) );
    //    doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
    //        mockByteArrayInputStream, "fileName", domainId );
    doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    doNothing().when( metadataService ).publish( mockIPentahoSession );

    metadataService
      .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo,
        null );

    verify( metadataService, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
      localeFiles, localeFilesInfo, null );
  }

  @Test( expected = PlatformImportException.class )
  public void testImportMetadataDatasourceDomainEmpty() throws Exception {
    String domainId = "";
    metadataService.importMetadataDatasource( domainId, null, null, false, null, null, null );
  }

  @Test
  public void testImportMetadataDatasourceError() throws Exception {
    String domainId = DOMAIN_ID;
    InputStream metadataFile = mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = mock( FormDataContentDisposition.class );
    boolean overwrite = true;
    FormDataBodyPart mockFormDataBodyPart = mock( FormDataBodyPart.class );
    List<FormDataBodyPart> localeFiles = new ArrayList<FormDataBodyPart>();
    localeFiles.add( mockFormDataBodyPart );
    List<FormDataContentDisposition> localeFilesInfo = new ArrayList<FormDataContentDisposition>();
    FormDataContentDisposition mockFormDataContentDisposition = mock( FormDataContentDisposition.class );
    localeFilesInfo.add( mockFormDataContentDisposition );
    FileResource mockFileResource = mock( FileResource.class );
    Response mockResponse = mock( Response.class );

    doNothing().when( metadataService ).accessValidation();
    when( metadataFile.read( any() ) ).thenReturn( -1 );
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( "" ).when( metadataService ).prohibitedSymbolMessage( domainId, mockFileResource );

    try {
      metadataService
        .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo,
          null );
      fail();
    } catch ( PlatformImportException e ) {
      //expected
    } catch ( IllegalStateException e ) {
      //expected
    }

    verify( metadataService, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
      localeFiles, localeFilesInfo, null );
  }

  @Test
  public void testImportMetadataDatasourceWithACL() throws Exception {
    String domainId = DOMAIN_ID;
    InputStream metadataFile = mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = mock( FormDataContentDisposition.class );
    boolean overwrite = true;
    FormDataBodyPart mockFormDataBodyPart = mock( FormDataBodyPart.class );
    List<FormDataBodyPart> localeFiles = new ArrayList<FormDataBodyPart>();
    localeFiles.add( mockFormDataBodyPart );
    List<FormDataContentDisposition> localeFilesInfo = new ArrayList<FormDataContentDisposition>();
    FormDataContentDisposition mockFormDataContentDisposition = mock( FormDataContentDisposition.class );
    localeFilesInfo.add( mockFormDataContentDisposition );
    FileResource mockFileResource = mock( FileResource.class );
    Response mockResponse = mock( Response.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );
    IPlatformImportBundle mockIPlatformImportBundle = mock( IPlatformImportBundle.class );
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder =
      mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = mock( ByteArrayInputStream.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( metadataFile ).when( metadataService ).validateFileSize( any( InputStream.class ), anyString() );
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService )
      .createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    doReturn( "fileName" ).when( mockFormDataContentDisposition ).getFileName();
    doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( any( byte[].class ) );
    //    doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
    //        mockByteArrayInputStream, "fileName", domainId );
    doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    doNothing().when( metadataService ).publish( mockIPentahoSession );

    final RepositoryFileAclDto acl = new RepositoryFileAclDto();
    acl.setOwner( "owner" );
    acl.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    metadataService
      .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo,
        acl );

    verify( metadataService, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
      localeFiles, localeFilesInfo, acl );

    verify( metadataService.getImporter() ).importFile( argThat( new ArgumentMatcher<IPlatformImportBundle>() {
      @Override public boolean matches( Object argument ) {
        IPlatformImportBundle bundle = (IPlatformImportBundle) argument;
        return new RepositoryFileAclAdapter().unmarshal( acl ).equals( bundle.getAcl() );
      }
    } ) );
  }

  @Test
  public void testImportMetadataDatasourceLocaleFileStreams() throws Exception {
    String domainId = DOMAIN_ID;
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = true;
    InputStream mockLocalFileStream = mock( InputStream.class );
    List<InputStream> localeFiles = new ArrayList<>();
    localeFiles.add( mockLocalFileStream );
    List<String> localeFilesInfo = new ArrayList<>();
    localeFilesInfo.add( "fileName" );
    FileResource mockFileResource = mock( FileResource.class );
    Response mockResponse = mock( Response.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );
    IPlatformImportBundle mockIPlatformImportBundle = mock( IPlatformImportBundle.class );
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder =
      mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = mock( ByteArrayInputStream.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( metadataFile ).when( metadataService ).validateFileSize( any( InputStream.class ), anyString() );
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService )
      .createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( any( byte[].class ) );
    //    doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
    //        mockByteArrayInputStream, "fileName", domainId );
    doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    doNothing().when( metadataService ).publish( mockIPentahoSession );

    final RepositoryFileAclDto acl = new RepositoryFileAclDto();
    acl.setOwner( "owner" );
    acl.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    metadataService
      .importMetadataDatasource( domainId, metadataFile,  overwrite, localeFiles, localeFilesInfo,
        acl );

    verify( metadataService, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, overwrite,
      localeFiles, localeFilesInfo, acl );

    verify( metadataService.getImporter() ).importFile( argThat( new ArgumentMatcher<IPlatformImportBundle>() {
      @Override public boolean matches( Object argument ) {
        IPlatformImportBundle bundle = (IPlatformImportBundle) argument;
        return new RepositoryFileAclAdapter().unmarshal( acl ).equals( bundle.getAcl() );
      }
    } ) );
  }

  @Test
  public void testImportMetadataDatasourceNoPublishPermission() throws Exception {
    String domainId = DOMAIN_ID;
    InputStream metadataFile = mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = mock( FormDataContentDisposition.class );

    doThrow( new PentahoAccessControlException() ).when( metadataService ).accessValidation();

    try {
      metadataService.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, true,
        Collections.emptyList(), Collections.emptyList(), null );
      fail();
    } catch ( PentahoAccessControlException e ) {
      //expected
    }
    verify( metadataService, never() ).getImporter();
  }

  @Test
  public void testGetMetadataDatasourceAcl() throws Exception {
    String domainId = DOMAIN_ID;

    final RepositoryFileAcl acl = new RepositoryFileAcl.Builder( "owner" ).build();

    doReturn( true ).when( metadataService ).canManageACL();
    when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( acl );
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertEquals( acl, new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test( expected = FileNotFoundException.class )
  public void testGetMetadataDatasourceAclNoDS() throws Exception {
    String domainId = DOMAIN_ID;

    doReturn( true ).when( metadataService ).canManageACL();
    when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( null );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertNull( aclDto );
  }

  @Test
  public void testGetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = DOMAIN_ID;

    doReturn( true ).when( metadataService ).canManageACL();
    when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( null );
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertNull( aclDto );
  }

  @Test
  public void testSetMetadataDatasourceAcl() throws Exception {
    String domainId = DOMAIN_ID;

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doReturn( true ).when( metadataService ).canManageACL();
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );

    metadataService.setMetadataAcl( domainId, aclDto );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
      eq( new RepositoryFileAclAdapter().unmarshal( aclDto ) ) );
  }

  @Test( expected = FileNotFoundException.class )
  public void testSetMetadataDatasourceAclNoDS() throws Exception {
    String domainId = DOMAIN_ID;

    doReturn( true ).when( metadataService ).canManageACL();

    metadataService.setMetadataAcl( domainId, null );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
      (RepositoryFileAcl) isNull() );
  }

  @Test
  public void testSetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = DOMAIN_ID;

    doReturn( true ).when( metadataService ).canManageACL();
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );

    metadataService.setMetadataAcl( domainId, null );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
      (RepositoryFileAcl) isNull() );
  }

  @Test
  public void testIsContainsModel() throws Exception {
    testIsContainsModel( true );
    testIsContainsModel( false );
  }

  private void testIsContainsModel( final boolean isContainsValue ) throws Exception {

    InputStream metadataFile = getClass().getClassLoader().getResourceAsStream( TEST_XMI_FILE_PATH );

    fillServiceMock( DOMAIN_ID, metadataFile );
    doReturn( isContainsValue ).when( metadataService ).isContainsModel( any( Domain.class ) );
    doReturn( metadataFile ).when( metadataService ).createInputStreamFromFile( any( String.class ) );

    MetadataTempFilesListDto fileList = new MetadataTempFilesListDto();
    fileList.setXmiFileName( XMI_TEMP_FILE_NAME );

    assertEquals( metadataService.isContainsModel( XMI_TEMP_FILE_NAME ), isContainsValue );

  }

  @Test
  public void testImportMetadataFromTemp() throws Exception {

    final boolean IS_CONTAINS_MODEL = true;
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = true;
    when( metadataFile.read( any() ) ).thenReturn( -1 );

    fillServiceMock( DOMAIN_ID, metadataFile );
    doReturn( IS_CONTAINS_MODEL ).when( metadataService ).isContainsModel( any( Domain.class ) );
    doReturn( metadataFile ).when( metadataService ).createInputStreamFromFile( anyString() );

    MetadataTempFilesListDto fileList = new MetadataTempFilesListDto();
    fileList.setXmiFileName( XMI_TEMP_FILE_NAME );

    metadataService.importMetadataFromTemp( DOMAIN_ID, fileList, overwrite, null );

    verify( metadataService, times( 1 ) )
      .importMetadataDatasource( DOMAIN_ID, metadataFile, overwrite, new ArrayList<InputStream>(),
        new ArrayList<String>(), null );

  }

  @Test
  public void testUploadMetadataFilesToTempDir() throws Exception {

    InputStream metadataFile = mock( InputStream.class );

    fillServiceMock( DOMAIN_ID, metadataFile );
    doReturn( new StringInputStream( "" ) ).when( metadataService ).createInputStreamFromFile( any( String.class ) );
    doReturn( XMI_TEMP_FILE_NAME ).when( metadataService ).uploadFile( any( InputStream.class ) );

    MetadataTempFilesListDto res = metadataService.uploadMetadataFilesToTempDir( metadataFile, null, null );

    assertEquals( res.getXmiFileName(), XMI_TEMP_FILE_NAME );

  }

  private void fillServiceMock( String domainId, InputStream metadataFile ) throws Exception {
    FileResource mockFileResource = mock( FileResource.class );
    Response mockResponse = mock( Response.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );
    IPlatformImportBundle mockIPlatformImportBundle = mock( IPlatformImportBundle.class );
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder =
      mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = mock( ByteArrayInputStream.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService )
      .createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    doReturn( UPLOAD_DIR ).when( metadataService ).internalGetUploadDir();
    doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( any( byte[].class ) );
    //    doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
    //        mockByteArrayInputStream, "fileName", domainId );
    doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    doNothing().when( metadataService ).publish( mockIPentahoSession );
  }

  @Test
  public void createNewByteArrayInputStream() throws Exception {
    byte[] buffer = null;
    ByteArrayInputStream stream = metadataService.createNewByteArrayInputStream( buffer );
    assertNull( stream );
    buffer = new byte[ 10 ];
    stream = metadataService.createNewByteArrayInputStream( buffer );
    assertNotNull( stream );
  }


}

