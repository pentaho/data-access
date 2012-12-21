package org.pentaho.platform.dataaccess.datasource.wizard.debug;

import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceManagerGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;

/**
 * User: RFellows
 * Date: 12/17/12
 */
public class DebugDatasourceServiceManagerGwtImpl extends DatasourceServiceManagerGwtImpl {

  @Override
  public void isAdmin(final XulServiceCallback<Boolean> xulCallback) {
    xulCallback.success(true);
  }

}
