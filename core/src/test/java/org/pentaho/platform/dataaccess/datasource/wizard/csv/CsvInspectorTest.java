/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;

public class CsvInspectorTest {

  @Test
  public void testDetermineFileFormat() {
    CsvInspector inspector = new CsvInspector();
    String line = "12;24;45\r\n59;99;100\r\n";
    assertEquals( TextFileInputMeta.FILE_FORMAT_DOS, inspector.determineFileFormat( line ) );
  }

  @Test
  public void testGetColumnData() {
    CsvInspector inspector = new CsvInspector();
    String[][] lines = new String[][] { { "1", "2", "3" }, { "5", "6", "7" } };
    List<String> column = inspector.getColumnData( 1, lines );
    assertEquals( "2", column.get( 0 ) );
    assertEquals( "6", column.get( 1 ) );
  }

  @Test
  public void testGuessDelimiter() {
    CsvInspector inspector = new CsvInspector();
    String line = "12;24;45";
    assertEquals( ";", inspector.guessDelimiter( line ) );
    line = "12,24,45";
    assertEquals( ",", inspector.guessDelimiter( line ) );
    line = "12~24~45";
    assertEquals( "~", inspector.guessDelimiter( line ) );
    line = "12:24:45";
    assertEquals( ":", inspector.guessDelimiter( line ) );
    line = "12\t24\t45";
    assertEquals( "\t", inspector.guessDelimiter( line ) );
    line = "12|24|45";
    assertEquals( "|", inspector.guessDelimiter( line ) );
    line = "12 2 24 5 45 8";
    assertEquals( true, null == inspector.guessDelimiter( line ) );
  }
}
