/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.model.PartitionDatabaseMeta;

public class AutobeanUtilitiesTest {

  @Test
  public void testConnectionBeanToImpl() {
    DatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setId( "my id" );
    dbConnection.setAccessType( DatabaseAccessType.NATIVE );
    DatabaseType dbType = new DatabaseType();
    List<DatabaseAccessType> accessTypes = new LinkedList<DatabaseAccessType>();
    accessTypes.add( DatabaseAccessType.NATIVE );
    dbType.setSupportedAccessTypes( accessTypes );
    dbConnection.setDatabaseType( dbType );
    Map<String, String> extraOptions = new HashMap<String, String>();
    extraOptions.put( "opt", "value" );
    dbConnection.setExtraOptions( extraOptions );
    dbConnection.setName( "Best name" );
    dbConnection.setHostname( "localhost" );
    dbConnection.setDatabaseName( "foodmart" );
    dbConnection.setDatabasePort( "2233" );
    dbConnection.setUsername( "username" );
    dbConnection.setPassword( "password" );
    dbConnection.setStreamingResults( true );
    dbConnection.setDataTablespace( "tables" );
    dbConnection.setIndexTablespace( "indexes" );
    dbConnection.setSQLServerInstance( "INSTANCE_0" );
    dbConnection.setUsingDoubleDecimalAsSchemaTableSeparator( true );
    dbConnection.setInformixServername( "INFORM_1" );
    dbConnection.addExtraOption( "100", "option", "value" );
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put( "attr1", "value" );
    dbConnection.setAttributes( attributes );
    dbConnection.setChanged( true );
    dbConnection.setQuoteAllFields( true );
    dbConnection.setForcingIdentifiersToLowerCase( true );
    dbConnection.setForcingIdentifiersToUpperCase( true );
    dbConnection.setConnectSql( "select * from 1" );
    dbConnection.setUsingConnectionPool( true );
    dbConnection.setInitialPoolSize( 3 );
    dbConnection.setMaximumPoolSize( 9 );
    dbConnection.setPartitioned( true );
    Map<String, String> connectionPoolingProperties = new HashMap<String, String>();
    connectionPoolingProperties.put( "pool", "abc" );
    dbConnection.setConnectionPoolingProperties( connectionPoolingProperties );
    List<PartitionDatabaseMeta> partitioningInformation = new LinkedList<PartitionDatabaseMeta>();
    PartitionDatabaseMeta pdm = new PartitionDatabaseMeta();
    partitioningInformation.add( pdm );
    dbConnection.setPartitioningInformation( partitioningInformation );

    IDatabaseConnection conn = AutobeanUtilities.connectionBeanToImpl( dbConnection );
    assertEquals( conn.getId(), "my id" );
    assertEquals( conn.getAccessType( ), DatabaseAccessType.NATIVE );
    assertEquals( conn.getDatabaseType( ).getSupportedAccessTypes().size(), 1 );
    assertEquals( conn.getExtraOptions( ).size() , 3 );
    assertEquals( conn.getName( ), "Best name" );
    assertEquals( conn.getHostname( ), "localhost" );
    assertEquals( conn.getDatabaseName( ), "foodmart" );
    assertEquals( conn.getDatabasePort( ), "2233" );
    assertEquals( conn.getUsername( ), "username" );
    assertEquals( conn.getPassword( ), "password" );
    assertEquals( conn.isStreamingResults( ), true );
    assertEquals( conn.getDataTablespace( ), "tables" );
    assertEquals( conn.getIndexTablespace( ), "indexes" );
    assertEquals( conn.getSQLServerInstance( ), "INSTANCE_0" );
    assertEquals( conn.isUsingDoubleDecimalAsSchemaTableSeparator( ), true );
    assertEquals( conn.getInformixServername( ), "INFORM_1" );
    assertEquals( conn.getAttributes( ).size(), 1 );
    assertEquals( conn.getChanged( ), false );
    assertEquals( conn.isQuoteAllFields( ), true );
    assertEquals( conn.isForcingIdentifiersToLowerCase( ), true );
    assertEquals( conn.isForcingIdentifiersToUpperCase( ), true );
    assertEquals( conn.getConnectSql( ), "select * from 1" );
    assertEquals( conn.isUsingConnectionPool( ), true );
    assertEquals( conn.getInitialPoolSize( ), 3 );
    assertEquals( conn.getMaximumPoolSize( ), 9 );
    assertEquals( conn.isPartitioned( ), true );
    assertEquals( conn.getConnectionPoolingProperties( ).size(), 1 );
    assertEquals( conn.getPartitioningInformation( ).size(), 1 );
  }

  @Test
  public void testDbTypeBeanToImpl() {
    List<DatabaseAccessType> accessTypes = new LinkedList<DatabaseAccessType>();
    accessTypes.add( DatabaseAccessType.NATIVE );
    DatabaseType dbType1 = new DatabaseType( "name", "short name", accessTypes, 100500, "helpUri" );
    IDatabaseType dbType = AutobeanUtilities.dbTypeBeanToImpl( dbType1 );
    assertEquals( dbType.getName(), "name" );
    assertEquals( dbType.getShortName(), "short name" );
    assertEquals( dbType.getDefaultDatabasePort(), 100500 );
    assertEquals( dbType.getExtraOptionsHelpUrl(), "helpUri" );
    assertEquals( dbType.getSupportedAccessTypes().size(), 1 );
  }

  /**
   * A GWT AutoBean proxy returns its collection properties wrapped in emulated types such as
   * {@code emul.java.util.ListAutoBean$1}, which no GWT-RPC serialization policy knows about. Sharing that list by
   * reference is what caused "could not get type signature for class emul.java.util.ListAutoBean$1" in the Data
   * Source Wizard, so every collection must come back as a distinct, plain instance.
   */
  @Test
  public void testConnectionBeanToImplCopiesCollectionsRatherThanSharingThem() {
    DatabaseConnection dbConnection = new DatabaseConnection();
    DatabaseType dbType = new DatabaseType();
    dbType.setSupportedAccessTypes( new LinkedList<DatabaseAccessType>( List.of( DatabaseAccessType.NATIVE ) ) );
    dbConnection.setDatabaseType( dbType );
    dbConnection.setPartitioningInformation(
      new LinkedList<PartitionDatabaseMeta>( List.of( new PartitionDatabaseMeta() ) ) );
    dbConnection.setAttributes( new HashMap<String, String>( Map.of( "attr1", "value" ) ) );
    dbConnection.setExtraOptions( new HashMap<String, String>( Map.of( "opt", "value" ) ) );
    dbConnection.setExtraOptionsOrder( new HashMap<String, String>( Map.of( "opt", "1" ) ) );
    dbConnection.setConnectionPoolingProperties( new HashMap<String, String>( Map.of( "pool", "abc" ) ) );

    IDatabaseConnection conn = AutobeanUtilities.connectionBeanToImpl( dbConnection );

    assertNotSame( dbConnection.getPartitioningInformation(), conn.getPartitioningInformation() );
    assertNotSame( dbConnection.getDatabaseType().getSupportedAccessTypes(),
      conn.getDatabaseType().getSupportedAccessTypes() );
    assertNotSame( dbConnection.getAttributes(), conn.getAttributes() );
    assertNotSame( dbConnection.getExtraOptions(), conn.getExtraOptions() );
    assertNotSame( dbConnection.getExtraOptionsOrder(), conn.getExtraOptionsOrder() );
    assertNotSame( dbConnection.getConnectionPoolingProperties(), conn.getConnectionPoolingProperties() );

    // The copies must still carry the same content.
    assertEquals( dbConnection.getPartitioningInformation(), conn.getPartitioningInformation() );
    assertEquals( dbConnection.getAttributes(), conn.getAttributes() );
  }

  /**
   * Edge case: a null collection must stay null rather than throwing or being replaced with an empty collection.
   * Callers such as ConnectionService branch on null to decide whether to emit the property at all.
   * <p/>
   * A mock stands in for the AutoBean proxy here: a decoded AutoBean returns null for any property absent from the
   * JSON payload, which a real DatabaseConnection cannot represent because its getSQLServerInstance() dereferences
   * the extraOptions map.
   */
  @Test
  public void testConnectionBeanToImplPreservesNullCollections() {
    IDatabaseConnection dbConnection = mock( IDatabaseConnection.class );
    // Mockito hands back empty collections by default, so the absent-property state must be stubbed explicitly.
    when( dbConnection.getPartitioningInformation() ).thenReturn( null );
    when( dbConnection.getAttributes() ).thenReturn( null );
    when( dbConnection.getExtraOptions() ).thenReturn( null );
    when( dbConnection.getExtraOptionsOrder() ).thenReturn( null );
    when( dbConnection.getConnectionPoolingProperties() ).thenReturn( null );

    IDatabaseConnection conn = AutobeanUtilities.connectionBeanToImpl( dbConnection );

    assertNull( conn.getPartitioningInformation() );
    assertNull( conn.getAttributes() );
    assertNull( conn.getExtraOptions() );
    assertNull( conn.getExtraOptionsOrder() );
    assertNull( conn.getConnectionPoolingProperties() );
    assertNull( conn.getDatabaseType() );
  }

  /**
   * Error path: a connection with no database type must convert instead of throwing. ConnectionController swallows
   * exceptions from this call, so an NPE here silently drops the connection from the Data Source Wizard list.
   */
  @Test
  public void testConnectionBeanToImplHandlesNullDatabaseType() {
    DatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setDatabaseType( null );

    IDatabaseConnection conn = AutobeanUtilities.connectionBeanToImpl( dbConnection );

    assertNull( conn.getDatabaseType() );
  }

  @Test
  public void testDbTypeBeanToImplHandlesNullType() {
    assertNull( AutobeanUtilities.dbTypeBeanToImpl( null ) );
  }

  /**
   * The shared copy helper used by the connection controllers must convert collections too, otherwise an AutoBean
   * proxy list can re-enter the connection graph after conversion.
   */
  @Test
  public void testCopyDatabaseConnectionPropertiesCopiesCollectionsRatherThanSharingThem() {
    DatabaseConnection source = new DatabaseConnection();
    source.setName( "Best name" );
    source.setPartitioningInformation(
      new LinkedList<PartitionDatabaseMeta>( List.of( new PartitionDatabaseMeta() ) ) );
    source.setAttributes( new HashMap<String, String>( Map.of( "attr1", "value" ) ) );
    source.setExtraOptions( new HashMap<String, String>( Map.of( "opt", "value" ) ) );
    source.setExtraOptionsOrder( new HashMap<String, String>( Map.of( "opt", "1" ) ) );
    source.setConnectionPoolingProperties( new HashMap<String, String>( Map.of( "pool", "abc" ) ) );

    DatabaseConnection target = new DatabaseConnection();
    AutobeanUtilities.copyDatabaseConnectionProperties( source, target );

    assertEquals( "Best name", target.getName() );
    assertNotSame( source.getPartitioningInformation(), target.getPartitioningInformation() );
    assertNotSame( source.getAttributes(), target.getAttributes() );
    assertNotSame( source.getExtraOptions(), target.getExtraOptions() );
    assertNotSame( source.getExtraOptionsOrder(), target.getExtraOptionsOrder() );
    assertNotSame( source.getConnectionPoolingProperties(), target.getConnectionPoolingProperties() );

    assertEquals( source.getAttributes(), target.getAttributes() );
    assertEquals( 1, target.getPartitioningInformation().size() );
  }

  /**
   * The database type carries its own collection (supportedAccessTypes), so copying the connection must also convert
   * the type. An AutoBean-backed database type would otherwise smuggle an emul.java.util.ListAutoBean into the copied
   * connection and reintroduce the GWT-RPC serialization failure through the nested object.
   */
  @Test
  public void testCopyDatabaseConnectionPropertiesCopiesDatabaseTypeRatherThanSharingIt() {
    DatabaseType sourceType = new DatabaseType();
    sourceType.setName( "Hypersonic" );
    sourceType.setShortName( "HYPERSONIC" );
    sourceType.setDefaultDatabasePort( 9001 );
    sourceType.setExtraOptionsHelpUrl( "helpUri" );
    sourceType.setSupportedAccessTypes(
      new LinkedList<>( List.of( DatabaseAccessType.NATIVE, DatabaseAccessType.JNDI ) ) );

    DatabaseConnection source = new DatabaseConnection();
    source.setDatabaseType( sourceType );

    DatabaseConnection target = new DatabaseConnection();
    AutobeanUtilities.copyDatabaseConnectionProperties( source, target );

    IDatabaseType targetType = target.getDatabaseType();
    assertNotNull( "Database type should be copied, not dropped", targetType );
    assertNotSame( "Database type must not be shared by reference", sourceType, targetType );
    assertNotSame( "supportedAccessTypes must not be shared by reference",
      sourceType.getSupportedAccessTypes(), targetType.getSupportedAccessTypes() );

    // The copy must still describe the same database type.
    assertEquals( "Hypersonic", targetType.getName() );
    assertEquals( "HYPERSONIC", targetType.getShortName() );
    assertEquals( 9001, targetType.getDefaultDatabasePort() );
    assertEquals( "helpUri", targetType.getExtraOptionsHelpUrl() );
    assertEquals( sourceType.getSupportedAccessTypes(), targetType.getSupportedAccessTypes() );

    // Mutating the copy must not write back through to the original.
    targetType.getSupportedAccessTypes().clear();
    assertEquals( 2, sourceType.getSupportedAccessTypes().size() );
  }

  /**
   * Error path: a connection with no database type must copy cleanly. The controllers call this while building the
   * AutoBean sent to the server, so an NPE here would abort saving the connection outright.
   */
  @Test
  public void testCopyDatabaseConnectionPropertiesHandlesNullDatabaseType() {
    DatabaseConnection source = new DatabaseConnection();
    source.setDatabaseType( null );

    DatabaseConnection target = new DatabaseConnection();
    target.setDatabaseType( new DatabaseType() );
    AutobeanUtilities.copyDatabaseConnectionProperties( source, target );

    assertNull( "A null database type must overwrite any stale value on the target", target.getDatabaseType() );
  }

  @Test
  public void testCopyDatabaseConnectionPropertiesPreservesNullCollections() {
    DatabaseConnection source = new DatabaseConnection();
    source.setAttributes( null );
    source.setExtraOptions( null );
    source.setExtraOptionsOrder( null );
    source.setConnectionPoolingProperties( null );
    source.setPartitioningInformation( null );

    DatabaseConnection target = new DatabaseConnection();
    AutobeanUtilities.copyDatabaseConnectionProperties( source, target );

    assertNull( target.getPartitioningInformation() );
    assertNull( target.getAttributes() );
    assertNull( target.getExtraOptions() );
    assertNull( target.getExtraOptionsOrder() );
    assertNull( target.getConnectionPoolingProperties() );
  }

}
