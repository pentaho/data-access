package org.pentaho.platform.dataaccess.datasource.wizard.exception;

import java.io.Serializable;

/**
 * Attempt to Save a datasource when a datasource already exists by that name, and the method
 * is not set to overwrite.
 * 
 * @author tkafalas
 *
 */
public class DSWExistingFileException extends DSWException implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public DSWExistingFileException() {
    super();
  }
  
  public DSWExistingFileException(String message) {
    super(message);
  }
}
