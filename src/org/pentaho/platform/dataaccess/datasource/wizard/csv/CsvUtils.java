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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringEvaluationResult;
import org.pentaho.di.core.util.StringEvaluator;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvParseException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class CsvUtils extends PentahoBase {

  public static final List<String> NUMBER_FORMATS = Arrays.asList( "#",
    "#,##0.###"
  );

  private static final long serialVersionUID = 2498165533158485182L;

  private Log log = LogFactory.getLog( CsvUtils.class );
  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH =
    File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles"
      + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  public static final String TMP_FILE_PATH =
    File.separatorChar + "system" + File.separatorChar + File.separatorChar + "tmp" + File.separatorChar;
    //$NON-NLS-1$ //$NON-NLS-2$


  public ModelInfo getFileContents( String project, String name, String delimiter, String enclosure, int rows,
                                    boolean isFirstRowHeader, String encoding ) throws Exception {
    String path;
    if ( name.endsWith( ".tmp" ) ) { //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    } else {
      String relativePath = PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path",
        String.valueOf( DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) );  //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    }

    String fileLocation = path + name;

    ModelInfo result = new ModelInfo();
    CsvFileInfo fileInfo = new CsvFileInfo();
    fileInfo.setTmpFilename( name );
    result.setFileInfo( fileInfo );

    fileInfo.setContents( getLinesList( fileLocation, rows, encoding ) );
    fileInfo.setDelimiter( delimiter );
    fileInfo.setEnclosure( enclosure );
    fileInfo.setHeaderRows( 0 );

    // now try to generate some columns
    return result;
  }

  public ModelInfo generateFields( String project, String filename, int rowLimit, String delimiter, String enclosure,
                                   int headerRows, boolean doData, boolean doColumns, String encoding )
    throws Exception {

    String path;
    if ( filename.endsWith( ".tmp" ) ) { //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    } else {
      String relativePath = PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path",
        String.valueOf( DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) );  //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    }

    String fileLocation = path + filename;
    ModelInfo result = new ModelInfo();
    CsvFileInfo fileInfo = new CsvFileInfo();
    result.setFileInfo( fileInfo );

    CsvInspector inspector = new CsvInspector();
    String sampleLine = getLines( fileLocation, 1, encoding );
    int fileType = inspector.determineFileFormat( sampleLine );

    String contents = getLines( fileLocation, rowLimit, encoding );
    fileInfo.setContents( getLinesList( fileLocation, rowLimit, encoding ) );
    if ( delimiter.equals( "" ) ) { //$NON-NLS-1$
      delimiter = inspector.guessDelimiter( contents );
      enclosure = "\""; //$NON-NLS-1$
      headerRows = 0;
    }
    fileInfo.setDelimiter( delimiter );
    fileInfo.setEnclosure( enclosure );
    fileInfo.setHeaderRows( headerRows );
    fileInfo.setEncoding( encoding ); //Resolves the file encoding using icu4j.
    fileInfo.setProject( project );
    fileInfo.setTmpFilename( filename );

    DataProfile data = getDataProfile( fileInfo, rowLimit, fileLocation, fileType, encoding );
    if ( doData ) {
      result.setData( data.getRows() );
    }
    if ( doColumns ) {
      result.setColumns( data.getColumns() );
    }
    return result;
  }

  private List<String> getColumnData( int columnNumber, String[][] data ) {
    List<String> dataSample = new ArrayList<String>( data.length );
    for ( String[] row : data ) {
      dataSample.add( row[ columnNumber ] );
    }
    return dataSample;
  }

  protected List<String> getLinesList( String fileLocation, int rows, String encoding ) throws IOException {
    List<String> lines = new ArrayList<String>();
    try {
      File file = new File( fileLocation );
      FileInputStream fis = new FileInputStream( file );
      InputStreamReader isr = new InputStreamReader( fis, encoding );
      LineNumberReader reader = new LineNumberReader( isr );
      String line;
      int lineNumber = 0;
      while ( ( line = reader.readLine() ) != null && lineNumber < rows ) {
        lines.add( line );
        lineNumber++;
      }
      reader.close();
    } catch ( Exception e ) {
      log.equals( e );
    }
    return lines;
  }

  protected String getLines( String fileLocation, int rows, String encoding ) {
    File file = new File( fileLocation );

    // read one line, including all EOL characters
    InputStream in;
    InputStreamReader inr = null;
    StringBuilder line = new StringBuilder();
    int count = 0;
    try {
      in = new FileInputStream( file );
      inr = new InputStreamReader( in, encoding );


      int c = inr.read();
      boolean looking = true;
      while ( looking && c > 0 ) {
        line.append( (char) c );
        if ( c == '\r' || c == '\n' ) {
          // look at the next char
          c = inr.read();
          if ( c == '\r' || c == '\n' ) {
            line.append( (char) c );
            c = inr.read();
          }
          count++;
          if ( count == rows ) {
            looking = false;
          }
        } else {
          c = inr.read();
        }
      }
    } catch ( IOException e ) {
      //do nothing
    } finally {
      if ( inr != null ) {
        try {
          inr.close();
        } catch ( IOException e ) {
          // ignore this one
        }
      }
    }
    return line.toString();

  }

  private DataProfile getDataProfile( CsvFileInfo fileInfo, int rowLimit, String fileLocation, int fileType,
                                      String encoding ) throws Exception {
    DataProfile result = new DataProfile();
    String line = null;
    int row = 0;
    List<List<String>> headerSample = new ArrayList<List<String>>();
    List<List<String>> dataSample = new ArrayList<List<String>>( rowLimit );
    int maxColumns = 0;
    InputStreamReader reader = null;

    try {
      InputStream inputStream = new FileInputStream( fileLocation );
      UnicodeBOMInputStream bomIs = new UnicodeBOMInputStream( inputStream );
      reader = new InputStreamReader( bomIs, encoding );
      bomIs.skipBOM();

      //read each line of text file
      StringBuilder stringBuilder = new StringBuilder( 1000 );
      line = TextFileInput.getLine( null, reader, fileType, stringBuilder );

      while ( line != null && row < rowLimit ) {

        CSVTokenizer csvt = new CSVTokenizer( line, fileInfo.getDelimiter(), fileInfo.getEnclosure() );
        List<String> rowData = new ArrayList<String>();
        int count = 0;

        while ( csvt.hasMoreTokens() ) {
          String token = csvt.nextToken();
          if ( token != null ) {
            token = token.trim();
          }
          rowData.add( token );
          count++;
        }
        if ( maxColumns < count ) {
          maxColumns = count;
        }
        if ( row < fileInfo.getHeaderRows() ) {
          headerSample.add( rowData );
        } else {
          dataSample.add( rowData );
        }
        line = TextFileInput.getLine( null, reader, fileType, stringBuilder );
        row++;
      }

    } catch ( IllegalArgumentException iae ) {
      Logger.error( getClass().getSimpleName(), "There was an issue parsing the CSV file", iae );  //$NON-NLS-1$
      throw new CsvParseException( row + 1, line );
    } catch ( Exception e ) {
      Logger.error( getClass().getSimpleName(), "Could not read CSV", e );  //$NON-NLS-1$
      throw e;
    } finally {

      //close the file
      try {
        if ( reader != null ) {
          reader.close();
        }
      } catch ( Exception e ) {
        throw e;
        // ignore 
      }
    }
    String[][] headerValues = new String[ headerSample.size() ][ maxColumns ];
    int rowNo = 0;
    for ( List<String> values : headerSample ) {
      int colNo = 0;
      for ( String value : values ) {
        headerValues[ rowNo ][ colNo ] = value;
        colNo++;
      }
      rowNo++;
    }

    int[] fieldLengths = new int[ maxColumns ];

    String[][] dataValues = new String[ dataSample.size() ][ maxColumns ];
    DataRow[] data = new DataRow[ dataSample.size() ];
    rowNo = 0;
    for ( List<String> values : dataSample ) {
      int colNo = 0;
      for ( String value : values ) {
        dataValues[ rowNo ][ colNo ] = value;

        int currentMaxLength = fieldLengths[ colNo ];
        if ( value.length() > currentMaxLength ) {
          fieldLengths[ colNo ] = value.length();
        }
        colNo++;
      }
      data[ rowNo ] = new DataRow();
      data[ rowNo ].setCells( dataValues[ rowNo ] );
      rowNo++;
    }

    result.setRows( data );

    DecimalFormat df = new DecimalFormat( "000" ); //$NON-NLS-1$
    ColumnInfo[] profiles = new ColumnInfo[ maxColumns ];
    for ( int idx = 0; idx < maxColumns; idx++ ) {
      ColumnInfo profile = new ColumnInfo();
      profiles[ idx ] = profile;
      String title = CsvFileInfo.DEFAULT_COLUMN_NAME_PREFIX + df.format( idx + 1 );
      String colId = "PC_" + idx; //$NON-NLS-1$

      if ( headerValues.length > 0 ) {
        if ( headerValues[ headerValues.length - 1 ][ idx ] != null ) {
          title = headerValues[ headerValues.length - 1 ][ idx ];
          colId = title;
        }
      }
      profile.setTitle( title );
      profile.setId( colId );


      List<String> samples = getColumnData( idx, dataValues );

      assumeColumnDetails( profile, samples );

    }
    result.setColumns( profiles );
    return result;
  }


  protected void assumeColumnDetails( ColumnInfo profile, List<String> samples ) {
    StringEvaluator eval = new StringEvaluator( false, NUMBER_FORMATS, ColumnInfo.DATE_FORMATS );
    for ( String sample : samples ) {
      eval.evaluateString( sample );
    }
    StringEvaluationResult result = eval.getAdvicedResult();
    ValueMetaInterface meta = result.getConversionMeta();

    int type = meta.getType();
    String mask = meta.getConversionMask();
    int size;
    int precision = meta.getPrecision();

    profile.setFormat( mask );
    profile.setPrecision( precision > 0 ? precision : 0 );
    profile.setDataType( convertDataType( type ) );

    if ( meta.isString() ) {
      // pad the string lengths
      size = meta.getLength() + ( meta.getLength() / 2 );
    } else {
      size = precision > 0 ? meta.getLength() : 0;
    }

    profile.setLength( size );
  }

  @Override
  public Log getLogger() {
    return log;
  }


  public String getEncoding( String fileName ) throws Exception {

    String path;
    if ( fileName.endsWith( ".tmp" ) ) { //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    } else {
      String relativePath = PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path",
        String.valueOf( DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) );  //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    }
    String fileLocation = path + fileName;

    String encoding;
    try {
      byte[] bytes = new byte[ 1024 ];
      InputStream inputStream = new FileInputStream( new File( fileLocation ) );
      inputStream.read( bytes );
      CharsetDetector charsetDetector = new CharsetDetector();
      charsetDetector.setText( bytes );
      CharsetMatch charsetMatch = charsetDetector.detect();
      encoding = charsetMatch.getName();
      inputStream.close();
    } catch ( Exception e ) {
      log.error( e );
      throw e;
    }
    return encoding;
  }

  public ModelInfo getModelInfo( String project, String filename ) throws FileNotFoundException {
    XStream xstream = new XStream( new DomDriver( "UTF-8" ) ); //$NON-NLS-1$
    xstream.alias( "modelInfo", ModelInfo.class ); //$NON-NLS-1$
    xstream.alias( "columnInfo", ColumnInfo.class ); //$NON-NLS-1$
    String filepath = AgileHelper.getFolderPath( project ) + "/" + filename + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
    System.out.println( filepath );
    File f = new File( filepath );
    FileInputStream fis = new FileInputStream( f );
    return (ModelInfo) xstream.fromXML( fis );
  }

  private DataType convertDataType( int type ) {
    switch( type ) {
      case 1:
      case 5:
      case 6:
        return DataType.NUMERIC;
      case 3:
        return DataType.DATE;
      case 4:
        return DataType.BOOLEAN;
      default:
        return DataType.STRING;
    }
  }

  private static class DataProfile {
    DataRow[] rows = null;
    ColumnInfo[] columns = null;

    public DataRow[] getRows() {
      return rows;
    }

    public void setRows( DataRow[] rows ) {
      this.rows = rows;
    }

    public ColumnInfo[] getColumns() {
      return columns;
    }

    public void setColumns( ColumnInfo[] columns ) {
      this.columns = columns;
    }
  }

}
