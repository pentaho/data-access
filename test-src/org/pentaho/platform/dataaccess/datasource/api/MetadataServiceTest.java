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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.repository2.unified.jcr.IAclNodeHelper;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.FileResource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import org.pentaho.platform.web.http.api.resources.services.FileService;

public class MetadataServiceTest {

  private static MetadataService metadataService;

  @Before
  public void setUp() {
    metadataService = spy( new MetadataService() );
    metadataService.metadataDomainRepository = mock( IMetadataDomainRepository.class );
    metadataService.aclHelper = mock( IAclNodeHelper.class );
    metadataService.fileService = mock( FileService.class );
  }

  @After
  public void cleanup() {
    metadataService = null;
  }

  @Test
  public void testRemoveMetadata() throws Exception {
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );

    doReturn( true ).when( metadataService ).canAdministerCheck();
    doReturn( "param" ).when( metadataService ).fixEncodedSlashParam( "metadataId" );
    doNothing().when( metadataService.metadataDomainRepository ).removeDomain( "param" );

    metadataService.removeMetadata( "metadataId" );

    verify( metadataService, times( 1 ) ).removeMetadata( "metadataId" );
  }

  @Test
  public void testRemoveMetadataError() throws Exception {
    doReturn( false ).when( metadataService ).canAdministerCheck();
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

    List<String> response = metadataService.getMetadataDatasourceIds();

    verify( metadataService, times( 1 ) ).getMetadataDatasourceIds();
  }

  @Test
  public void testImportMetadataDatasource() throws Exception {
    String domainId = "home\\admin/resource/";
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
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder = mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = mock( ByteArrayInputStream.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService ).createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    doReturn( "fileName" ).when( mockFormDataContentDisposition ).getFileName();
    doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( any( byte[].class ) );
    doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
        mockByteArrayInputStream, "fileName", domainId );
    doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    doNothing().when( metadataService ).publish( mockIPentahoSession );

    metadataService.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo, null );

    verify( metadataService, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
        localeFiles, localeFilesInfo, null );
  }

  @Test
  public void testImportMetadataDatasourceError() throws Exception {
    String domainId = "home\\admin\tresource/";
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
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( "" ).when( metadataService ).prohibitedSymbolMessage( domainId, mockFileResource );

    try {
      metadataService
        .importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo, null );
      fail();
    } catch ( PlatformImportException e ) {
      //expected
    }

    verify( metadataService, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
        localeFiles, localeFilesInfo, null );
  }

  @Test
  public void testImportMetadataDatasourceWithACL() throws Exception {
    String domainId = "home\\admin/resource/";
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
    RepositoryFileImportBundle.Builder mockRepositoryFileImportBundleBuilder = mock( RepositoryFileImportBundle.Builder.class );
    RepositoryFileImportBundle mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
    ByteArrayInputStream mockByteArrayInputStream = mock( ByteArrayInputStream.class );

    doNothing().when( metadataService ).accessValidation();
    doReturn( mockFileResource ).when( metadataService ).createNewFileResource();
    doReturn( mockResponse ).when( mockFileResource ).doGetReservedChars();
    doReturn( null ).when( mockResponse ).getEntity();
    doReturn( "\t\n/" ).when( metadataService ).objectToString( null );
    doReturn( mockRepositoryFileImportBundleBuilder ).when( metadataService ).createNewRepositoryFileImportBundleBuilder(
        metadataFile, false, domainId, null );
    doReturn( "fileName" ).when( mockFormDataContentDisposition ).getFileName();
    doReturn( mockByteArrayInputStream ).when( metadataService ).createNewByteArrayInputStream( any( byte[].class ) );
    doReturn( mockRepositoryFileImportBundle ).when( metadataService ).createNewRepositoryFileImportBundle(
        mockByteArrayInputStream, "fileName", domainId );
    doReturn( mockRepositoryFileImportBundle ).when( mockRepositoryFileImportBundleBuilder ).build();
    doReturn( mockIPlatformImporter ).when( metadataService ).getImporter();
    doNothing().when( mockIPlatformImporter ).importFile( mockIPlatformImportBundle );
    doReturn( mockIPentahoSession ).when( metadataService ).getSession();
    doNothing().when( metadataService ).publish( mockIPentahoSession );

    final RepositoryFileAclDto acl = new RepositoryFileAclDto();
    acl.setOwner( "owner" );
    acl.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    metadataService.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo, acl );

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
  public void testGetMetadataDatasourceAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    final RepositoryFileAcl acl = new RepositoryFileAcl.Builder( "owner" ).build();

    doReturn( true ).when( metadataService ).canAdministerCheck();
    when( metadataService.aclHelper.getAclFor( anyString(), any( IAclNodeHelper.DatasourceType.class ) ) )
        .thenReturn( acl );
    final IUnifiedRepository repository = mock( IUnifiedRepository.class );
    when( metadataService.fileService.getRepository() ).thenReturn( repository );
    when( metadataService.fileService.doGetFileAcl( anyString() ) ).thenReturn( new RepositoryFileAclAdapter().marshal( acl ) );
    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repository.getFileById( anyString() ) ).thenReturn( repositoryFile );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclHelper ).getAclFor( domainId, IAclNodeHelper.DatasourceType.METADATA );

    assertEquals( acl, new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test
  public void testGetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    doReturn( true ).when( metadataService ).canAdministerCheck();
    when( metadataService.aclHelper.getAclFor( anyString(), any( IAclNodeHelper.DatasourceType.class ) ) )
        .thenReturn( null );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclHelper ).getAclFor( domainId, IAclNodeHelper.DatasourceType.METADATA );

    assertNull( aclDto );
  }

  @Test
  public void testSetMetadataDatasourceAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doReturn( true ).when( metadataService ).canAdministerCheck();

    metadataService.setMetadataAcl( domainId, aclDto );

    verify( metadataService.aclHelper ).setAclFor( domainId, IAclNodeHelper.DatasourceType.METADATA,
        new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test
  public void testSetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    doReturn( true ).when( metadataService ).canAdministerCheck();

    metadataService.setMetadataAcl( domainId, null );

    verify( metadataService.aclHelper ).setAclFor( domainId, IAclNodeHelper.DatasourceType.METADATA, null );
  }
}

