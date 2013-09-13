package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;

public class DataRow implements Serializable {
  
  private static final long serialVersionUID = 2498165533349885182L;

  private Object cells[];

  public Object[] getCells() {
    return cells;
  }

  public void setCells(Object[] cells) {
    this.cells = cells;
  }
  
}
