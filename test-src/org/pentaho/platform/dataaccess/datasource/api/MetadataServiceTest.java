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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
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
      return mock( IUnifiedRepository.class );
    }
  }

  @Before
  public void setUp() {
    metadataService = spy( new MetadataServiceMock() );
    metadataService.metadataDomainRepository = mock( PentahoMetadataDomainRepository.class );
    metadataService.aclAwarePentahoMetadataDomainRepositoryImporter = mock( IAclAwarePentahoMetadataDomainRepositoryImporter.class );
    metadataService.mondrianCatalogService = mock( IMondrianCatalogService.class );
  }

  @After
  public void cleanup() {
    metadataService = null;
  }

  @Test
  public void testRemoveMetadata() throws Exception {
    doReturn( true ).when( metadataService ).canAdministerCheck();
    doReturn( "param" ).when( metadataService ).fixEncodedSlashParam( "metadataId" );
    doNothing().when( metadataService.metadataDomainRepository ).removeDomain( "param" );

    metadataService.removeMetadata( "metadataId" );

    verify( metadataService, times( 1 ) ).removeMetadata( "metadataId" );
	// checking fixEncodedSlashParam method is not called (BISERVER-12403 issue)
    verify( metadataService, never() ).fixEncodedSlashParam( "metadataId" );
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

    doReturn( true ).when( metadataService ).canManageACL();
    when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( acl );
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when( ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
        .thenReturn( domainFilesData );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertEquals( acl, new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test( expected = FileNotFoundException.class )
  public void testGetMetadataDatasourceAclNoDS() throws Exception {
    String domainId = "home\\admin/resource/";

    doReturn( true ).when( metadataService ).canManageACL();
    when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( null );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertNull( aclDto );
  }

  @Test
  public void testGetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    doReturn( true ).when( metadataService ).canManageACL();
    when( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn( null );
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when( ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
        .thenReturn( domainFilesData );
    final RepositoryFileAclDto aclDto = metadataService.getMetadataAcl( domainId );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertNull( aclDto );
  }

  @Test
  public void testSetMetadataDatasourceAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doReturn( true ).when( metadataService ).canManageACL();
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when( ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
        .thenReturn( domainFilesData );

    metadataService.setMetadataAcl( domainId, aclDto );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
        eq( new RepositoryFileAclAdapter().unmarshal( aclDto ) ) );
  }

  @Test( expected = FileNotFoundException.class )
  public void testSetMetadataDatasourceAclNoDS() throws Exception {
    String domainId = "home\\admin/resource/";

    doReturn( true ).when( metadataService ).canManageACL();

    metadataService.setMetadataAcl( domainId, null );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
        (RepositoryFileAcl) isNull() );
  }

  @Test
  public void testSetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "home\\admin/resource/";

    doReturn( true ).when( metadataService ).canManageACL();
    final Map<String, InputStream> domainFilesData = mock( Map.class );
    when( domainFilesData.isEmpty() ).thenReturn( false );
    when( ( (PentahoMetadataDomainRepository) metadataService.metadataDomainRepository ).getDomainFilesData( domainId ) )
        .thenReturn( domainFilesData );

    metadataService.setMetadataAcl( domainId, null );

    verify( metadataService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
        (RepositoryFileAcl) isNull() );
  }
}

