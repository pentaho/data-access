package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: Aug 11, 2010
 * Time: 3:50:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CsvParseException extends Exception implements Serializable {
  private static final long serialVersionUID = 1L;
  private int lineNumber = 0;
  private String offendingLine = "";
  private static final String I18N_MESSAGE_KEY = "CsvParseException.0001_FAILED_TO_PARSE_CSV";

  public CsvParseException() {
    this(0,null);
  }

  public CsvParseException(int lineNumber, String offendingLine) {
    super(I18N_MESSAGE_KEY);
    this.lineNumber = lineNumber;
    this.offendingLine = offendingLine;
  }

  public CsvParseException(int lineNumber, String offendingLine, Throwable cause) {
    super(I18N_MESSAGE_KEY, cause);
    this.lineNumber = lineNumber;
    this.offendingLine = offendingLine;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getOffendingLine() {
    return offendingLine;
  }

}
