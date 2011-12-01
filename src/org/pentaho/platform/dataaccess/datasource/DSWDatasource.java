package org.pentaho.platform.dataaccess.datasource;

import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;

public class DSWDatasource implements IDatasource{
  
  private static final long serialVersionUID = 1L;
  private LogicalModelSummary datasource;
  private IDatasourceInfo datasourceInfo;
  
  
  public DSWDatasource(LogicalModelSummary datasource, IDatasourceInfo datasourceInfo) {
    this.datasource = datasource;
    this.datasourceInfo = datasourceInfo;
  }

  @Override
  public LogicalModelSummary getDatasource() {
    return this.datasource;
  }

  @Override
  public IDatasourceInfo getDatasourceInfo() {
    return datasourceInfo;
  }
}
