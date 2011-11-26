package org.pentaho.platform.dataaccess.datasource;

import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.datasource.GenericDatasourceInfo;

public class DSWDatasource extends GenericDatasourceInfo implements IGenericDatasource{
  
  private static final long serialVersionUID = 1L;
  private LogicalModelSummary datasource;
  
  
  public DSWDatasource(LogicalModelSummary datasource, String name, String id, String type) {
    super(name, id, type);
    this.datasource = datasource;
  }

  @Override
  public LogicalModelSummary getDatasource() {
    return this.datasource;
  }

  @Override
  public void setDatasource(Object datasource) {
    if(datasource instanceof LogicalModelSummary) {
      this.datasource = (LogicalModelSummary)datasource;      
    }
  }

}
