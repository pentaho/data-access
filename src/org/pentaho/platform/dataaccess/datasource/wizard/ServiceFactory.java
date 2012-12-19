package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceManagerGwtImpl;

/**
 * User: RFellows
 * Date: 12/14/12
 */
public class ServiceFactory implements IServiceFactory {

  @Override
  public IXulAsyncDatasourceServiceManager createDatasourceServiceManager() {
    return new DatasourceServiceManagerGwtImpl();
  }

  @Override
  public IModelerServiceAsync createModelerService() {
    return new GwtModelerServiceImpl();
  }

  @Override
  public IXulAsyncDSWDatasourceService createDatasourceService() {
    return new DSWDatasourceServiceGwtImpl();
  }
}
