package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.reporting.libraries.base.util.CSVTokenizer;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class CsvFileInfo extends XulEventSourceAdapter implements Serializable {

  public static final String HEADER_ROWS_ATTRIBUTE = "headerRows"; //$NON-NLS-1$
  public static final String ENCLOSURE_ATTRIBUTE = "enclosure"; //$NON-NLS-1$
  public static final String DELIMITER_ATTRIBUTE = "delimiter"; //$NON-NLS-1$
  public static final String TMP_FILENAME_ATTRIBUTE = "tmpFilename"; //$NON-NLS-1$
  public static final String ENCODING = "encoding"; //$NON-NLS-1$
  public static final String DEFAULT_COLUMN_NAME_PREFIX = "Field_"; //$NON-NLS-1$
  
  private static final long serialVersionUID = 2498165533158482382L;
  
  private List<String> contents;
  
  private String delimiter = ""; //$NON-NLS-1$
  
  private String enclosure = ""; //$NON-NLS-1$
  
  private String encoding; //$NON-NLS-1$

  private int headerRows;
  
  private String project;
  
  private String tmpFilename;
  
  private String fileName;
  
  private String currencySymbol = ""; //$NON-NLS-1$
  
  private String decimalSymbol = "."; //$NON-NLS-1$
  
  private String groupSymbol = ","; //$NON-NLS-1$
  
  private String ifNull = "---"; //$NON-NLS-1$
  
  private String nullStr = ""; //$NON-NLS-1$
  public static final String FRIENDLY_FILENAME_ATTRIBUTE = "friendlyFilename"; //$NON-NLS-1$
  private String friendlyFilename;
  private String savedEncoding;
  
  @Bindable
  public String getIfNull() {
    return ifNull;
  }

  @Bindable
  public void setIfNull(String ifNull) {
    this.ifNull = ifNull;
  }

  @Bindable
  public String getNullStr() {
    return nullStr;
  }

  @Bindable
  public void setNullStr(String nullStr) {
    this.nullStr = nullStr;
  }

  @Bindable
  public List<String> getContents() {
    return contents;
  }

  @Bindable
  public void setContents(List<String> contents) {
    this.contents = contents;
  }

  @Bindable
  public String getDelimiter() {
    return delimiter;
  }

  @Bindable
  public void setDelimiter(String delimiter) {
    String previousVal = this.delimiter;
    this.delimiter = delimiter;
    firePropertyChange(DELIMITER_ATTRIBUTE, previousVal, delimiter);
  }

  @Bindable
  public String getEnclosure() {
    return enclosure;
  }

  @Bindable
  public void setEnclosure(String enclosure) {
    String previousVal = this.enclosure;
    this.enclosure = enclosure;
    firePropertyChange(ENCLOSURE_ATTRIBUTE, previousVal, enclosure);
  }

  @Bindable
  public int getHeaderRows() {
    return headerRows;
  }

  @Bindable
  public void setHeaderRows(int headerRows) {
    int previousVal = this.headerRows;
    this.headerRows = headerRows;
    firePropertyChange(HEADER_ROWS_ATTRIBUTE, previousVal, this.headerRows);
  }

  @Bindable
  public String getProject() {
    return project;
  }

  @Bindable
  public void setProject(String project) {
    this.project = project;
  }

  @Bindable
  public String getTmpFilename() {
    return tmpFilename;
  }

  @Bindable
  public void setTmpFilename(String filename) {
    String previousVal = this.tmpFilename;
    this.tmpFilename = filename;
    firePropertyChange(TMP_FILENAME_ATTRIBUTE, previousVal, filename);
  }
  
  public String getFileName() {
	  return this.fileName;
  }
  
  public void setFileName(String file) {
	  this.fileName = file;
  }

  @Bindable
  public String getCurrencySymbol() {
    return currencySymbol;
  }

  @Bindable
  public void setCurrencySymbol(String currencySymbol) {
    this.currencySymbol = currencySymbol;
  }

  @Bindable
  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  @Bindable
  public void setDecimalSymbol(String decimalSymbol) {
    this.decimalSymbol = decimalSymbol;
  }

  @Bindable
  public String getGroupSymbol() {
    return groupSymbol;
  }

  @Bindable
  public void setGroupSymbol(String groupSymbol) {
    this.groupSymbol = groupSymbol;
  }

  @Bindable
  public String getEncoding() {
    return encoding;
  }

  @Bindable
  public void setEncoding(String encoding) {
    String previousVal = this.encoding;
    this.encoding = encoding;
    firePropertyChange(ENCODING, previousVal, encoding);
  }
  
  public void setEncodingFromServer(String encoding){
	  if(this.savedEncoding != null && !this.savedEncoding.trim().equals("")){
		  setEncoding(this.savedEncoding);
		  this.savedEncoding = null;
	  } else {
		  setEncoding(encoding);
	  }
  }
  
  public void setSavedEncoding(String encoding) {
	  this.savedEncoding = encoding;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((currencySymbol == null) ? 0 : currencySymbol.hashCode());
    result = prime * result + ((decimalSymbol == null) ? 0 : decimalSymbol.hashCode());
    result = prime * result + ((delimiter == null) ? 0 : delimiter.hashCode());
    result = prime * result + ((enclosure == null) ? 0 : enclosure.hashCode());
    result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
    result = prime * result + ((tmpFilename == null) ? 0 : tmpFilename.hashCode());
    result = prime * result + ((groupSymbol == null) ? 0 : groupSymbol.hashCode());
    result = prime * result + headerRows;
    result = prime * result + ((ifNull == null) ? 0 : ifNull.hashCode());
    result = prime * result + ((nullStr == null) ? 0 : nullStr.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CsvFileInfo other = (CsvFileInfo) obj;
    if (currencySymbol == null) {
      if (other.currencySymbol != null)
        return false;
    } else if (!currencySymbol.equals(other.currencySymbol))
      return false;
    if (decimalSymbol == null) {
      if (other.decimalSymbol != null)
        return false;
    } else if (!decimalSymbol.equals(other.decimalSymbol))
      return false;
    if (delimiter == null) {
      if (other.delimiter != null)
        return false;
    } else if (!delimiter.equals(other.delimiter))
      return false;
    if (enclosure == null) {
      if (other.enclosure != null)
        return false;
    } else if (!enclosure.equals(other.enclosure))
      return false;
    if (encoding == null) {
      if (other.encoding != null)
        return false;
    } else if (!encoding.equals(other.encoding))
      return false;
    if (tmpFilename == null) {
      if (other.tmpFilename != null)
        return false;
    } else if (!tmpFilename.equals(other.tmpFilename))
      return false;
    if (groupSymbol == null) {
      if (other.groupSymbol != null)
        return false;
    } else if (!groupSymbol.equals(other.groupSymbol))
      return false;
    if (headerRows != other.headerRows)
      return false;
    if (ifNull == null) {
      if (other.ifNull != null)
        return false;
    } else if (!ifNull.equals(other.ifNull))
      return false;
    if (nullStr == null) {
      if (other.nullStr != null)
        return false;
    } else if (!nullStr.equals(other.nullStr))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
      return false;
    return true;
  }

  public List<List<String>> parseSampleContents() {
    String delim = getDelimiter();
    if (contents == null) {
      throw new IllegalStateException("Sample Contents is null, nothing to parse"); //$NON-NLS-1$
    } else if (delim == null || "".equals(delim)) { //$NON-NLS-1$
      // use a random delimiter that will result in an un-parsed list 
      delim = "~!@#$%";
    }
    List<List<String>> sample = new ArrayList<List<String>>();
    CSVTokenizer csvTokenizer;
    String enclosure = null;
    if (!"".equals(getEnclosure())) {
      enclosure = getEnclosure();
    }
    for ( String line : contents ) {
      csvTokenizer = new CSVTokenizer(line, delim, enclosure);

      List<String> rowData = new ArrayList<String>();
      int count = 0;
      
      while (csvTokenizer.hasMoreTokens()) {
        // get next token and store it in the list
        rowData.add(csvTokenizer.nextToken());
        count++;
      }
      
      sample.add(rowData);  
    
    }
    return sample;
  }

  public String formatSampleContents() {
    StringBuilder sb = new StringBuilder();
    String padding = "  ";

    List<List<String>> parsed;
    try {
      parsed = parseSampleContents();
    } catch (IllegalStateException e) {
      // nothing to parse, formatted value is "nothing"
      return "";
    }

    int maxColumns = getMaxColumnCount(parsed);

    // create a dummy row for the header
    if (getHeaderRows() == 0) {
      List<String> dummy = getDummyHeader(maxColumns);
      parsed.add(0, dummy);
    }

    int[] widths = getMaxWidths(parsed, maxColumns);

    int lineNumber = 0;
    StringBuilder headerMarker = new StringBuilder();
    int lineWidth = 0;

    // find out how big the entire line will be, including the padding between fields
    for (int w = 0; w < widths.length; w++) {
      lineWidth += widths[w] + padding.length();
    }

    // create a string that will separate the header from the body of text
    for (int x = 0; x < lineWidth; x++) {
      headerMarker.append("-");
    }

    // format each field, padding as required 
    for (List<String> line : parsed) {
      for (int i = 0; i < line.size(); i++) {
        String field = line.get(i);
        sb.append(padField(field, widths[i]));
        sb.append(padding);
      }
      sb.append("\n");
      if (lineNumber == 0) {
        sb.append(headerMarker.toString());
        sb.append("\n");
      }
      lineNumber++;
    }
    return sb.toString();
  }

  private List<String> getDummyHeader(int maxColumns) {
    List<String> dummy = new ArrayList<String>(maxColumns);
    DecimalFormat df = new DecimalFormat("000");
    for(int i = 0; i < maxColumns; i++) {
      dummy.add(DEFAULT_COLUMN_NAME_PREFIX + df.format(i + 1));
    }
    return dummy;
  }

  /**
   * It would be nice just to use String.format here, but it is not available in GWT.
   * @param field
   * @param totalWidth
   * @return
   */
  private String padField(String field, int totalWidth) {
    StringBuilder sb = new StringBuilder(field);

    if (field.length() < totalWidth) {
      for (int i = field.length(); i < totalWidth; i++) {
        sb.append(" ");
      }
    }

    return sb.toString();
  }

  private int[] getMaxWidths(List<List<String>> parsedContents, int columns) {
    if (parsedContents != null && parsedContents.size() > 0) {
      int[] widths = new int[columns];
      for (List<String> lines: parsedContents) {
        for (int i = 0; i < lines.size(); i++) {
          String field = lines.get(i);
          widths[i] = field.length() > widths[i] ? field.length() : widths[i];
        }
      }
      return widths;
    }
    return null;
  }

  private int getMaxColumnCount(List<List<String>> parsedContents) {
    int max = 0;
    for (List<String> line : parsedContents) {
      max = line.size() > max ? line.size() : max;
    }
    return max;
  }

  @Bindable
  public String getFriendlyFilename() {
    return friendlyFilename;
  }

  @Bindable
  public void setFriendlyFilename(String friendlyFilename) {
    String previousVal = this.friendlyFilename;
    this.friendlyFilename = friendlyFilename;
    firePropertyChange(FRIENDLY_FILENAME_ATTRIBUTE, previousVal, friendlyFilename);
  }
  
  public void clear() {
    setDelimiter(",");
    setContents(null);
    setCurrencySymbol("");
    setEnclosure("\"");
    setEncoding("");
    setTmpFilename(null);
    setFileName(null);
    setFriendlyFilename(null);
    setGroupSymbol(",");
    setCurrencySymbol("");
    setDecimalSymbol(".");
    setHeaderRows(1);
    setIfNull("---");
    setNullStr("");
    setProject(null);
    formatSampleContents();
  }  
}
