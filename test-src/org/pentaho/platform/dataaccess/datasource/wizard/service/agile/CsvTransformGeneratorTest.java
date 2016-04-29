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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.CsvTransformGeneratorIT.CutLongNamesStepContext;

public class CsvTransformGeneratorTest {

  @Test
  public void testGetMaxColumnNameLength() throws Exception {

    CsvTransformGenerator ctg10 = spy( new CsvTransformGenerator( new ModelInfo(), null ) );
    Database db10 = mock( Database.class );
    doReturn( db10 ).when( ctg10 ).getDatabase( any( DatabaseMeta.class ) );
    DatabaseMetaData dbmd10 = mock( DatabaseMetaData.class );
    doReturn( dbmd10 ).when( db10 ).getDatabaseMetaData();
    doReturn( 10 ).when( dbmd10 ).getMaxColumnNameLength();
    assertEquals( 10, ctg10.getMaxColumnNameLength() );

    CsvTransformGenerator ctg0 = spy( new CsvTransformGenerator( new ModelInfo(), null ) );
    Database db0 = mock( Database.class );
    doReturn( db0 ).when( ctg0 ).getDatabase( any( DatabaseMeta.class ) );
    DatabaseMetaData dbmd0 = mock( DatabaseMetaData.class );
    doReturn( dbmd0 ).when( db0 ).getDatabaseMetaData();
    doReturn( 0 ).when( dbmd0 ).getMaxColumnNameLength();
    assertEquals( 0, ctg0.getMaxColumnNameLength() );

    // As for the case of doReturn( <less then 0> ).when( dbmd0 ).getMaxColumnNameLength();
    // It's impossible according to java.sql.DatabaseMetaData.
    // @see java.sql.DatabaseMetaData.getMaxColumnNameLength()
    //

    CsvTransformGenerator ctgErr1 = spy( new CsvTransformGenerator( new ModelInfo(), null ) );
    Database dbErr1 = mock( Database.class );
    doReturn( dbErr1 ).when( ctgErr1 ).getDatabase( any( DatabaseMeta.class ) );
    DatabaseMetaData dbmdErr1 = mock( DatabaseMetaData.class );
    doReturn( dbmdErr1 ).when( dbErr1 ).getDatabaseMetaData();
    doThrow( new SQLException() ).when( dbmdErr1 ).getMaxColumnNameLength();
    assertEquals( 0, ctgErr1.getMaxColumnNameLength() );

    CsvTransformGenerator ctgErr2 = spy( new CsvTransformGenerator( new ModelInfo(), null ) );
    Database dbErr2 = mock( Database.class );
    doReturn( dbErr2 ).when( ctgErr2 ).getDatabase( any( DatabaseMeta.class ) );
    doThrow( new KettleDatabaseException() ).when( dbErr2 ).getDatabaseMetaData();
    assertEquals( 0, ctgErr2.getMaxColumnNameLength() );

    CsvTransformGenerator ctgNoDatabase = spy( new CsvTransformGenerator( new ModelInfo(), null ) );
    doReturn( null ).when( ctgNoDatabase ).getDatabase( any( DatabaseMeta.class ) );
    assertEquals( 0, ctgNoDatabase.getMaxColumnNameLength() );
  }

  @Test
  public void testCreateCutLongNamesStep_shortColumnNames() throws Exception {
    CsvTransformGenerator ctg = new CsvTransformGenerator( new ModelInfo(), null );
    int maxColumnNameLength = 18;
    String stepName = "TEST_STEP_CutLongNames";
    CutLongNamesStepContext testData = new CutLongNamesStepContext();
    StepMeta step = ctg.createCutLongNamesStep( testData.fields, maxColumnNameLength, stepName );
    Assert.assertNull( "step", step );
  }

  @Test
  /**
   *  Very artificial case.
   *  <code>org.pentaho.di.core.row.RowMeta</code> provides unique names,
   *  though it's not required in <code>org.pentaho.di.core.row.RowMetaInterface</code>
  */
  public void testCreateCutLongNamesStep_dupColumnNames() throws Exception {
    CsvTransformGenerator ctg = new CsvTransformGenerator( new ModelInfo(), null );
    int maxColumnNameLength = 18;
    String stepName = "TEST_STEP_DupLongNames";
    CutLongNamesStepContext testData = new CutLongNamesStepContext();
    RowMetaInterface fields = spy( testData.fields );
    List<ValueMetaInterface> vmList = new ArrayList<ValueMetaInterface>( testData.fields.getValueMetaList() );
    vmList.set( 1, vmList.get( 0 ) );
    doReturn( vmList ).when(  fields ).getValueMetaList();

    String[] fieldNamesDups = new String[] {"a", "a", "A_1", "b_1", "LONGlonglong", "longlonglong_again", "a_2", "lonGlo_1"};
    String[] fieldRenames = new String[] {"a", "a_1", "A_1_1", "b_1", "LONGlonglong", "longlonglong_again", "a_2", "lonGlo_1"};

    StepMeta step = ctg.createCutLongNamesStep( fields, maxColumnNameLength, stepName );
    assertNotNull( "step", step );
    assertEquals( "step name", stepName, step.getName() );
    StepMetaInterface stepMetaIntegrface = step.getStepMetaInterface();
    assertNotNull( "stepMetaIntegrface", stepMetaIntegrface );
    assertTrue( "stepMetaIntegrface instanceof SelectValuesMeta", stepMetaIntegrface instanceof SelectValuesMeta );
    SelectValuesMeta svm = (SelectValuesMeta) stepMetaIntegrface;
    String[] selectName = svm.getSelectName();
    assertArrayEquals( "selectName", fieldNamesDups, selectName );
    String[] selectRename = svm.getSelectRename();
    assertArrayEquals( "selectName", fieldRenames, selectRename );
  }

  @Test
  public void testCreateCutLongNamesStep_longColumnNames() throws Exception {
    CsvTransformGenerator ctg = new CsvTransformGenerator( new ModelInfo(), null );
    int maxColumnNameLength = 8;
    String stepName = "TEST_STEP_CutLongNames";
    CutLongNamesStepContext testData = new CutLongNamesStepContext();
    String[] fieldRenames = testData.fieldRenamesCut8;

    StepMeta step = ctg.createCutLongNamesStep( testData.fields, maxColumnNameLength, stepName );
    Assert.assertNotNull( "step", step );
    Assert.assertEquals( "step name", stepName, step.getName() );
    StepMetaInterface stepMetaIntegrface = step.getStepMetaInterface();
    Assert.assertNotNull( "stepMetaIntegrface", stepMetaIntegrface );
    Assert.assertTrue( "stepMetaIntegrface instanceof SelectValuesMeta", stepMetaIntegrface instanceof SelectValuesMeta );
    SelectValuesMeta svm = (SelectValuesMeta) stepMetaIntegrface;
    String[] selectName = svm.getSelectName();
    Assert.assertArrayEquals( "selectName", testData.fieldNames, selectName );
    String[] selectRename = svm.getSelectRename();
    Assert.assertArrayEquals( "selectName", fieldRenames, selectRename );
  }

  @Test
  public void testCreateCutLongNamesStep_littleMaxColumnNameLength() throws Exception {
    CsvTransformGenerator ctg = new CsvTransformGenerator( new ModelInfo(), null );
    int maxColumnNameLength = 1;
    String stepName = "TEST_STEP_CutLongNames";
    CutLongNamesStepContext testData = new CutLongNamesStepContext();

    try {
      StepMeta step = ctg.createCutLongNamesStep( testData.fields, maxColumnNameLength, stepName );
      fail( "Ex[pected exception: Cannot cut field name. Maximum suffix length is exceeded" );
    } catch ( Exception e ) {
      // expected
    }
  }

}
