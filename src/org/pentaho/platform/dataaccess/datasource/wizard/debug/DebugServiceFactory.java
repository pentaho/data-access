package org.pentaho.platform.dataaccess.datasource.wizard.debug;

import org.pentaho.platform.dataaccess.datasource.wizard.ServiceFactory;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;

/**
 * User: RFellows
 * Date: 12/14/12
 */
public class DebugServiceFactory extends ServiceFactory {

  @Override
  public IXulAsyncDatasourceServiceManager createDatasourceServiceManager() {
    return new DebugDatasourceServiceManagerGwtImpl();
  }
}
