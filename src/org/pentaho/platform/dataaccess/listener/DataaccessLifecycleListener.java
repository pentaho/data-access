package org.pentaho.platform.dataaccess.listener;
import org.pentaho.platform.api.datasource.IDatasourceServiceManager;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class DataaccessLifecycleListener implements IPluginLifecycleListener{
 

  @Override
  public void init() throws PluginLifecycleException {
    IDatasourceServiceManager manager = PentahoSystem.get(IDatasourceServiceManager.class, PentahoSessionHolder.getSession());
    manager.registerService(new DSWDatasourceService());
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
    // TODO Auto-generated method stub
    
  }

}
