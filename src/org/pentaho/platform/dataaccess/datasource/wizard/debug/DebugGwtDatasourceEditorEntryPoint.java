package org.pentaho.platform.dataaccess.datasource.wizard.debug;

import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint;
import org.pentaho.platform.dataaccess.datasource.wizard.IServiceFactory;

/**
 * User: RFellows
 * Date: 12/17/12
 */
public class DebugGwtDatasourceEditorEntryPoint extends GwtDatasourceEditorEntryPoint {

  private IServiceFactory serviceFactory = new DebugServiceFactory();

  protected IServiceFactory getServiceFactory() {
    return serviceFactory;
  }
}
