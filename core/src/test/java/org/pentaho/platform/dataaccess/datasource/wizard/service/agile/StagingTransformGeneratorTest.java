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
* Copyright (c) 2002-2024 Hitachi Vantara..  All rights reserved.
*/
package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StagingTransformGeneratorTest {

  private StagingTransformGenerator stagingTransformGenerator;

  private Database database;

  private DatabaseMeta databaseMeta;

  @BeforeClass
  public static void setUpOnce() throws KettlePluginException {
    // Register Natives to create a default DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
  }

  @Before
  public void setUp() {
    database = mock( Database.class );
    databaseMeta = mock( DatabaseMeta.class );
    stagingTransformGenerator = new CsvTransformGenerator( mock( ModelInfo.class ), databaseMeta ) {
      @Override Database getDatabase( DatabaseMeta databaseMeta ) {
        return database;
      }
    };
  }

  /**
   * Given a name of an existing table to drop.
   * <br/>
   * When StagingTransformGenerator is called to drop this table,
   * then it should execute drop statement.
   */
  @Test
  public void shouldDropTableIfExists() throws Exception {
    String existingTable = "existingTable";
    when( database.checkTableExists( existingTable ) ).thenReturn( true );
    when( databaseMeta.getQuotedSchemaTableCombination( (String) isNull(), eq( existingTable ) ) )
      .thenReturn( existingTable );

    stagingTransformGenerator.dropTable( existingTable );

    verify( database ).execStatement( "DROP TABLE existingTable" );
  }

  /**
   * Given a name of a non-existing table to drop.
   * <br/>
   * When StagingTransformGenerator is called to drop this table,
   * then it shouldn't execute drop statement.
   */
  @Test
  public void shouldNotDropTableIfNotExists() throws Exception {
    String nonExistingTable = "nonExistingTable";
    when( database.checkTableExists( nonExistingTable ) ).thenReturn( false );
    when( databaseMeta.getQuotedSchemaTableCombination( (String) isNull(), eq( nonExistingTable ) ) )
      .thenReturn( nonExistingTable );

    stagingTransformGenerator.dropTable( nonExistingTable );

    verify( database, never() ).execStatement( anyString() );
  }
}
