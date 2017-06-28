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
* Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.api;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
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

  private static MetadataService metadataService;

  private class MetadataServiceMock extends MetadataService {
    @Override protected IUnifiedRepository getRepository() {
      return Mockito.mock( IUnifiedRepository.class );
    }
  }

  @Before
  public void setUp() {
    metadataService = Mockito.spy( new MetadataServiceMock() );
    metadataService.metadataDomainRepository = Mockito.mock( PentahoMetadataDomainRepository.class );
    metadataService.aclAwarePentahoMetadataDomainRepositoryImporter =
      Mockito.mock( IAclAwarePentahoMetadataDomainRepositoryImporter.class );
    metadataService.mondrianCatalogService = Mockito.mock( IMondrianCatalogService.class );
  }

  @After
  public void cleanup() {
    metadataService = null;
  }

  @Test
  public void testRemoveMetadata() throws Exception {
    Mockito.doNothing().when( metadataService ).ensureDataAccessPermissionCheck();
    Mockito.doReturn( "param" ).when( metadataService ).fixEncodedSlashParam( "metadataId" );
    Mockito.doNothing().when( metadataService.metadataDomainRepository ).removeDomain( "param" );

    metadataService.removeMetadata( "metadataId" );

    Mockito.verify( metadataService, Mockito.times( 1 ) ).removeMetadata( "metadataId" );
    // checking fixEncodedSlashParam method is not called (BISERVER-12403 issue)
    Mockito.verify( metadataService, Mockito.never() ).fixEncodedSlashParam( "metadataId" );
  }

  @Test
  public void testRemoveMetadataError() throws Exception {
    ConnectionServiceException cse = new ConnectionServiceException();
    Mockito.doThrow( cse ).when( metadataService ).ensureDataAccessPermissionCheck();
    try {
      metadataService.removeMetadata( "metadataId" );
      Assert.fail();
    } catch ( PentahoAccessControlException e ) {
      //expected
    }
    Mockito.verify( metadataService, Mockito.times( 1 ) ).removeMetadata( "metadataId" );
  }

  @Test
  public void testGetMetadataDatasourceIds() throws Exception {
    List<String> mockMetadataIdsList = new ArrayList<String>();
    Set<String> mockSet = new HashSet<String>();
    mockSet.add( "domainId1" );
    mockMetadataIdsList.add( "domainId1" );

    Mockito.doReturn( true ).when( metadataService ).isMetadataDatasource( "domainId1" );
    Mockito.doReturn( mockSet ).when( metadataService.metadataDomainRepository ).getDomainIds();

    List<String> response = metadataService.getMetadataDatasourceIds();

    Mockito.verify( metadataService, Mockito.times( 1 ) ).getMetadataDatasourceIds();
    Assert.assertEquals( mockMetadataIdsList, response );
  }

  @Test
  public void testGetMetadataDatasourceIdsError() throws Exception {
    InterruptedException mockInterruptedException = Mockito.mock( InterruptedException.class );
    Mockito.doThrow( mockInterruptedException ).when( metadataService ).sleep( 100 );

    List<String> response = metadataService.getMetadataDatasourceIds();

    Mockito.verify( metadataService, Mockito.times( 1 ) ).getMetadataDatasourceIds();
  }

  @Test( expected = PlatformImportException.class )
  public void testImportMetadataDatasourceMaxFileSize() throws Exception {
    // test should work in case max-file-limit is set
    int fileDefaultSize = 10000000;
    String maxFileLimit = PentahoSystem
      .getSystemSetting( "file-upload-defaults/max-file-limit", String.valueOf( fileDefaultSize ) );  //$NON-NLS-1$

    Assert.assertEquals( fileDefaultSize, Integer.parseInt( maxFileLimit ) );

    // fileDefaultSize will be exceeded
    byte[] bytes = new byte[ fileDefaultSize + 1 ];
    InputStream metadataFile = new ByteArrayInputStream( bytes );

    FileResource mockFileResource = Mockito.mock( FileResource.class );
    Response mockResponse = Mockito.mock( Response.class );
    IPlatformImporter mockIPlatformImporter = Mockito.mock( IPlatformImporter.class );

    Mockito.doNothing().when( metadataService ).accessValidation();
    Mockito.doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    Mockito.doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    Mockito.doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();

    metadataService.importMetadataDatasource( "test_file", metadataFile, null, false, null, null, null );
  }

  @Test
  public void testImportMetadataDatasource() throws Exception {
    String domainId = "home\\admin/resource/";
    InputStream metadataFile = Mockito.mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = Mockito.mock( FormDataContentDisposition.class );
    boolean overwrite = true;
    FormDataBodyPart mockFormDataBodyPart = Mockito.mock( FormDataBodyPart.class );
    List<FormDataBodyPart> localeFiles = new ArrayList<FormDataBodyPart>();
    localeFiles.add( mockFormDataBodyPart );
    List<FormDataContentDisposition> localeFilesInfo = new ArrayList<FormDataContentDisposition>();
    FormDataContentDisposition mockFormDataContentDisposition = Mockito.mock( FormDataContentDisposition.class );
    localeFilesInfo.add( mockFormDataContentDisposition );
    FileResource mockFileResource = Mockito.mock( FileResource.class );
    Response mockResponse = Mockito.mock( Response.class );
    IPentahoSession mockIPentahoSession = Mockito.mock( IPentahoSession.class );
    IPlatformImporter mockIPlatformImporter = Mockito.mock( IPlatformImporter.class );
    IPlatformImportBundle mockIPlatformImportBundle = Mockito.mock( IPlatformImportBundle.class );
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder =
      Mockito.mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = Mockito.mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = Mockito.mock( ByteArrayInputStream.class );

    Mockito.doNothing().when( metadataService ).accessValidation();
    Mockito.doReturn( metadataFile ).when( metadataService ).validateFileSize( Mockito.any( InputStream.class ), Mockito.anyString() );
    Mockito.doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    Mockito.doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    Mockito.doReturn( null ).when( mockResponse ).getEntity();
    Mockito.doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    Mockito.doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService )
      .createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    Mockito.doReturn( "fileName" ).when( mockFormDataContentDisposition ).getFileName();
    Mockito.doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( Mockito.any( byte[].class ) );
    Mockito.doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
      mockByteArrayInputStream, "fileName", domainId );
    Mockito.doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    Mockito.doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    Mockito.doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    Mockito.doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    Mockito.doNothing().when( metadataService ).publish( mockIPentahoSession );

    metadataService
      .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo,
        null );

    Mockito.verify( metadataService, Mockito.times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
      localeFiles, localeFilesInfo, null );
  }

  @Test( expected = PlatformImportException.class )
  public void testImportMetadataDatasourceDomainEmpty() throws Exception {
    String domainId = "";
    metadataService.importMetadataDatasource( domainId, null, null, false, null, null, null );
  }

  @Test
  public void testImportMetadataDatasourceError() throws Exception {
    String domainId = "home\\admin\tresource/";
    InputStream metadataFile = Mockito.mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = Mockito.mock( FormDataContentDisposition.class );
    boolean overwrite = true;
    FormDataBodyPart mockFormDataBodyPart = Mockito.mock( FormDataBodyPart.class );
    List<FormDataBodyPart> localeFiles = new ArrayList<FormDataBodyPart>();
    localeFiles.add( mockFormDataBodyPart );
    List<FormDataContentDisposition> localeFilesInfo = new ArrayList<FormDataContentDisposition>();
    FormDataContentDisposition mockFormDataContentDisposition = Mockito.mock( FormDataContentDisposition.class );
    localeFilesInfo.add( mockFormDataContentDisposition );
    FileResource mockFileResource = Mockito.mock( FileResource.class );
    Response mockResponse = Mockito.mock( Response.class );

    Mockito.doNothing().when( metadataService ).accessValidation();
    Mockito.doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    Mockito.doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    Mockito.doReturn( null ).when( mockResponse ).getEntity();
    Mockito.doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    Mockito.doReturn( "" ).when( metadataService ).prohibitedSymbolMessage( domainId, mockFileResource );

    try {
      metadataService
        .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo,
          null );
      Assert.fail();
    } catch ( PlatformImportException e ) {
      //expected
    }

    Mockito.verify( metadataService, Mockito.times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
      localeFiles, localeFilesInfo, null );
  }

  @Test
  public void testImportMetadataDatasourceWithACL() throws Exception {
    String domainId = "home\\admin/resource/";
    InputStream metadataFile = Mockito.mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = Mockito.mock( FormDataContentDisposition.class );
    boolean overwrite = true;
    FormDataBodyPart mockFormDataBodyPart = Mockito.mock( FormDataBodyPart.class );
    List<FormDataBodyPart> localeFiles = new ArrayList<FormDataBodyPart>();
    localeFiles.add( mockFormDataBodyPart );
    List<FormDataContentDisposition> localeFilesInfo = new ArrayList<FormDataContentDisposition>();
    FormDataContentDisposition mockFormDataContentDisposition = Mockito.mock( FormDataContentDisposition.class );
    localeFilesInfo.add( mockFormDataContentDisposition );
    FileResource mockFileResource = Mockito.mock( FileResource.class );
    Response mockResponse = Mockito.mock( Response.class );
    IPentahoSession mockIPentahoSession = Mockito.mock( IPentahoSession.class );
    IPlatformImporter mockIPlatformImporter = Mockito.mock( IPlatformImporter.class );
    IPlatformImportBundle mockIPlatformImportBundle = Mockito.mock( IPlatformImportBundle.class );
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder =
      Mockito.mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = Mockito.mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = Mockito.mock( ByteArrayInputStream.class );

    Mockito.doNothing().when( metadataService ).accessValidation();
    Mockito.doReturn( metadataFile ).when( metadataService ).validateFileSize( Mockito.any( InputStream.class ), Mockito.anyString() );
    Mockito.doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    Mockito.doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    Mockito.doReturn( null ).when( mockResponse ).getEntity();
    Mockito.doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    Mockito.doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService )
      .createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    Mockito.doReturn( "fileName" ).when( mockFormDataContentDisposition ).getFileName();
    Mockito.doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( Mockito.any( byte[].class ) );
    Mockito.doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
      mockByteArrayInputStream, "fileName", domainId );
    Mockito.doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    Mockito.doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    Mockito.doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    Mockito.doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    Mockito.doNothing().when( metadataService ).publish( mockIPentahoSession );

    final RepositoryFileAclDto acl = new RepositoryFileAclDto();
    acl.setOwner( "owner" );
    acl.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    metadataService
      .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo,
        acl );

    Mockito.verify( metadataService, Mockito.times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
      localeFiles, localeFilesInfo, acl );

    Mockito.verify( metadataService.getImporter() ).importFile( Mockito.argThat( new ArgumentMatcher<IPlatformImportBundle>() {
      @Override public boolean matches( Object argument ) {
        IPlatformImportBundle bundle = (IPlatformImportBundle) argument;
        return new RepositoryFileAclAdapter().unmarshal( acl ).equals( bundle.getAcl() );
      }
    } ) );
  }

  @Test
  public void testGetMetadataDatasourceAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    final RepositoryFileAcl acl = new RepositoryFileAcl.Builder( "owner" ).build();

    Mockito.doReturn( true ).when( metadataService ).canManageACL();
    Mockito.when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( acl );
    final Map<String, InputStream> domainFilesData = Mockito.mock( Map.class );
    Mockito.when( domainFilesData.isEmpty() ).thenReturn( false );
    Mockito.when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    Mockito.verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( Mockito.eq( domainId ) );

    Assert.assertEquals( acl, new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test( expected = FileNotFoundException.class )
  public void testGetMetadataDatasourceAclNoDS() throws Exception {
    String domainId = "home\\admin/resource/";

    Mockito.doReturn( true ).when( metadataService ).canManageACL();
    Mockito.when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( null );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    Mockito.verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( Mockito.eq( domainId ) );

    Assert.assertNull( aclDto );
  }

  @Test
  public void testGetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    Mockito.doReturn( true ).when( metadataService ).canManageACL();
    Mockito.when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( null );
    final Map<String, InputStream> domainFilesData = Mockito.mock( Map.class );
    Mockito.when( domainFilesData.isEmpty() ).thenReturn( false );
    Mockito.when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    Mockito.verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( Mockito.eq( domainId ) );

    Assert.assertNull( aclDto );
  }

  @Test
  public void testSetMetadataDatasourceAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    Mockito.doReturn( true ).when( metadataService ).canManageACL();
    final Map<String, InputStream> domainFilesData = Mockito.mock( Map.class );
    Mockito.when( domainFilesData.isEmpty() ).thenReturn( false );
    Mockito.when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );

    metadataService.setMetadataAcl( domainId, aclDto );

    Mockito.verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( Mockito.eq( domainId ),
      Mockito.eq( new RepositoryFileAclAdapter().unmarshal( aclDto ) ) );
  }

  @Test( expected = FileNotFoundException.class )
  public void testSetMetadataDatasourceAclNoDS() throws Exception {
    String domainId = "home\\admin/resource/";

    Mockito.doReturn( true ).when( metadataService ).canManageACL();

    metadataService.setMetadataAcl( domainId, null );

    Mockito.verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( Mockito.eq( domainId ),
      (RepositoryFileAcl) Mockito.isNull() );
  }

  @Test
  public void testSetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    Mockito.doReturn( true ).when( metadataService ).canManageACL();
    final Map<String, InputStream> domainFilesData = Mockito.mock( Map.class );
    Mockito.when( domainFilesData.isEmpty() ).thenReturn( false );
    Mockito.when(
      ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
      .thenReturn( domainFilesData );

    metadataService.setMetadataAcl( domainId, null );

    Mockito.verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( Mockito.eq( domainId ),
      (RepositoryFileAcl) Mockito.isNull() );
  }
}

