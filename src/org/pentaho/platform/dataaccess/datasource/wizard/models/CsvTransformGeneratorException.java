package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;

public class CsvTransformGeneratorException extends Exception implements Serializable {

  private static final long serialVersionUID = 1L;
  private String causeMessage;
  private String causeStackTrace;
  
  public CsvTransformGeneratorException() {
    super();
  }
  
  public CsvTransformGeneratorException(String message) {
    super(message);
  }
  
  public CsvTransformGeneratorException(Throwable cause) {
    super(cause);
  }
  
  public CsvTransformGeneratorException(String message, Throwable cause) {
    super(message, cause);
  }

  public CsvTransformGeneratorException(String message, Throwable cause, String causeStackTrace) {
    super(message, cause);
    this.causeMessage = cause.getMessage();
    this.causeStackTrace = causeStackTrace;
  }

  public CsvTransformGeneratorException(String message, Throwable cause, String causeMessage, String causeStackTrace) {
    super(message, cause);
    this.causeMessage = causeMessage;
    this.causeStackTrace = causeStackTrace;
  }

  public String getCauseMessage() {
	return causeMessage;
  }

  public void setCauseMessage(String causeMessage) {
	this.causeMessage = causeMessage;
  }

  public String getCauseStackTrace() {
	return causeStackTrace;
  }

  public void setCauseStackTrace(String causeStackTrace) {
	this.causeStackTrace = causeStackTrace;
  }
}
