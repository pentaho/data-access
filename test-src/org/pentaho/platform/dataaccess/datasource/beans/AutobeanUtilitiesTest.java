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

package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
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

}
