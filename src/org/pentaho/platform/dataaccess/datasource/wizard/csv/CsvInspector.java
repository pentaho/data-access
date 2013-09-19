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

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import java.util.ArrayList;
import java.util.List;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;

public class CsvInspector {
  
  public int determineFileFormat( String line ) {
    
    int type = -1;
    int n = line.length();
    char c1 = 0;
    char c2 = 0;
    if( n > 0 ) {
      c1 = line.charAt(n-1);
      if( n > 1 ) {
        c2 = line.charAt(n-2);
      }
      if( c1 == '\n' || c1 == '\r' ) {
        if( c2 == '\n' || c2 == '\r') {
          type = TextFileInputMeta.FILE_FORMAT_DOS;
        } else {
          type = TextFileInputMeta.FILE_FORMAT_UNIX;
        }
      }
    }
    return type;
  }
  
  public List<String> getColumnData(int columnNumber, String data[][]) {
    List<String> dataSample = new ArrayList<String>(data.length);
    for (String[] row : data) {
      dataSample.add(row[columnNumber]);
    }
    return dataSample;
  }

  public String guessDelimiter( String line ) {
    int numTabs = 0;
    int numCommas = 0;
    int numPipes = 0;
    int numTildas = 0;
    int numColons = 0;
    int numSemiColons = 0;
    for( int idx=0; idx<line.length(); idx++ ) {
      char c = line.charAt( idx );
      switch (c) {
        case '\t' : numTabs++; break;
        case ',' : numCommas++; break;
        case '|' : numPipes++; break;
        case '~' : numTildas++; break;
        case ':' : numColons++; break;
        case ';' : numSemiColons++; break;
      }
    }
    int max = Math.max(numTabs, numCommas);
    max = Math.max( max, numPipes);
    max = Math.max( max, numTildas);
    max = Math.max( max, numColons);
    max = Math.max( max, numSemiColons);
    
    if( max == 0 ) {
      return null;
    }
    if( max == numCommas ) {
      return ","; //$NON-NLS-1$
    }
    if( max == numTabs ) {
      return "\t"; //$NON-NLS-1$
    }
    if( max == numPipes ) {
      return "|"; //$NON-NLS-1$
    }
    if( max == numTildas ) {
      return "~"; //$NON-NLS-1$
    }
    if( max == numColons ) {
      return ":"; //$NON-NLS-1$
    }
    if( max == numSemiColons ) {
      return ";"; //$NON-NLS-1$
    }
    return null;
  }

}
