package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;

/**
 * User: RFellows
 * Date: 12/14/12
 */
public interface IServiceFactory {

  public IXulAsyncDatasourceServiceManager createDatasourceServiceManager();
  public IModelerServiceAsync createModelerService();
  public IXulAsyncDSWDatasourceService createDatasourceService();

}
