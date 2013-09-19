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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class CsvFileInfoTest {

  CsvFileInfo csv = null;
  
  @SuppressWarnings("nls")
  @Before
  public void setup() {
    csv = new CsvFileInfo();
    csv.setDelimiter(",");
    csv.setEnclosure("\"");
    csv.setHeaderRows(1);
    
    ArrayList<String> lines = new ArrayList<String>();
    lines.add("PRESIDENT,FROM,TO,ORDER,PARTY,TERMS,VICE PRESIDENT,SALARY,BIRTHDATE");
    lines.add("George Washington,1789,1797,1,none,2,John Adams,$400000.019,02/22/1732");
    lines.add("John Adams,1797,1801,2,federalist,1,Thomas Jefferson,$400000.029,10/30/1735");
    lines.add("Thomas Jefferson,1801,1809,3,democratic-republican,2,Aaron Burr/George Clinton,$400000.039,04/13/1743");
    lines.add("James Madison,1809,1817,4,democratic-republican,2,George Clinton/Elbridge Gerry,$400000.049,03/16/1751");
    lines.add("James Monroe,1817,1825,5,democratic-republican,2,Daniel Tompkins,$400000.059,04/28/1758");

    csv.setContents(lines);
    
  }
  
  @Test
  public void testParseSampleContents() throws Exception {

    List<List<String>> parsed = csv.parseSampleContents();
    assertEquals(6, parsed.size());
    for (List<String> list : parsed) {
      System.out.println(list);
      assertEquals(9, list.size());
    }
    
  }
    
  @Test(expected=IllegalStateException.class)
  public void testParseSampleContents_nullContent() throws Exception {
    csv.setContents(null);
    csv.parseSampleContents();
  }
  
  @SuppressWarnings("nls")
  @Test
  public void testParseSampleContents_badDelimiter() throws Exception {
    csv.setDelimiter(";");
    List<List<String>> parsed = csv.parseSampleContents();
    assertEquals(6, parsed.size());
    for (List<String> list : parsed) {
      // no semi-colons in the test string, should only have 1 column per row
      assertEquals(1, list.size());
    }
    
  }

  @Test
  public void testFormatSampleContentsText() throws Exception {
    String formatted = csv.formatSampleContents();
    String expected =
        "PRESIDENT          FROM  TO    ORDER  PARTY                  TERMS  VICE PRESIDENT                 SALARY       BIRTHDATE   \n" +
        "----------------------------------------------------------------------------------------------------------------------------\n" +
        "George Washington  1789  1797  1      none                   2      John Adams                     $400000.019  02/22/1732  \n" +
        "John Adams         1797  1801  2      federalist             1      Thomas Jefferson               $400000.029  10/30/1735  \n" +
        "Thomas Jefferson   1801  1809  3      democratic-republican  2      Aaron Burr/George Clinton      $400000.039  04/13/1743  \n" +
        "James Madison      1809  1817  4      democratic-republican  2      George Clinton/Elbridge Gerry  $400000.049  03/16/1751  \n" +
        "James Monroe       1817  1825  5      democratic-republican  2      Daniel Tompkins                $400000.059  04/28/1758  \n";

    assertEquals(expected, formatted);
    System.out.println(formatted);
  }

  @Test
  public void testFormatSampleContentsText_NoHeaderRow() throws Exception {
    csv.setHeaderRows(0);
    String formatted = csv.formatSampleContents();
    String expected =
        "Field_001          Field_002  Field_003  Field_004  Field_005              Field_006  Field_007                      Field_008    Field_009   \n" +
        "----------------------------------------------------------------------------------------------------------------------------------------------\n" +
        "PRESIDENT          FROM       TO         ORDER      PARTY                  TERMS      VICE PRESIDENT                 SALARY       BIRTHDATE   \n" +
        "George Washington  1789       1797       1          none                   2          John Adams                     $400000.019  02/22/1732  \n" +
        "John Adams         1797       1801       2          federalist             1          Thomas Jefferson               $400000.029  10/30/1735  \n" +
        "Thomas Jefferson   1801       1809       3          democratic-republican  2          Aaron Burr/George Clinton      $400000.039  04/13/1743  \n" +
        "James Madison      1809       1817       4          democratic-republican  2          George Clinton/Elbridge Gerry  $400000.049  03/16/1751  \n" +
        "James Monroe       1817       1825       5          democratic-republican  2          Daniel Tompkins                $400000.059  04/28/1758  \n";

    assertEquals(expected, formatted);
    System.out.println(formatted);
  }
}
