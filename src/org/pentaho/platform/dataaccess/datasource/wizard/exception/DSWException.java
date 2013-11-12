package org.pentaho.platform.dataaccess.datasource.wizard.exception;

import java.io.Serializable;

/**
 * General purpose exception for classes related to DataSourceWizard and templates
 * 
 * @author tkafalas
 *
 */
public class DSWException extends Exception  implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public DSWException() {
    super();
  }
  
  public DSWException(String message) {
    super(message);
  }
}
