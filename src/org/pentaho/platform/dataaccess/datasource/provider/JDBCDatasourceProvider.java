package org.pentaho.platform.dataaccess.datasource.provider;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.catalog.impl.Datasource;

public class JDBCDatasourceProvider implements IDatasourceProvider{

  private IDatasourceMgmtService datasourceMgmtService;
  private IDatasourceType datasourceType = new JDBCDatasourceType(JDBCDatasourceType.ID, JDBCDatasourceType.ID); 
  
  public JDBCDatasourceProvider(final IDatasourceMgmtService datasourceMgmtService) {
    this.datasourceMgmtService = datasourceMgmtService;
  }
  @Override
  public List<IDatasource> getDatasources() {
    List<IDatasource> datasources = new ArrayList<IDatasource>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        datasources.add( new Datasource(databaseConnection.getName(), getType(), null) );
      }
      
    } catch ( DatasourceMgmtServiceException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return datasources;
  }

  @Override
  public IDatasourceType getType() {
    return datasourceType;
  }

}
