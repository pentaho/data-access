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
