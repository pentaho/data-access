package org.pentaho.platform.dataaccess.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.ui.xul.XulServiceCallback;

public class UIDatasourceServiceManager {

  Map<String, IUIDatasourceAdminService> serviceMap = new HashMap<String, IUIDatasourceAdminService>();
  
 // private native void setupPrivilegedNativeHooks(UIDatasourceServiceManager manager)/*-{
 //   $wnd.pho.datasourceManager.registerService = function(jsDatasourceService) {
 //     manager.@org.pentaho.platform.dataaccess.datasource.UIDatasourceServiceManager::registerService(Lorg/pentaho/platform/dataaccess/datasource/JSUIDatasourceService;)(jsDatasourceService);
 //   }
 // }-*/;
  public UIDatasourceServiceManager() {
 //   setupPrivilegedNativeHooks(this);
  }
  public UIDatasourceServiceManager(List<IUIDatasourceAdminService> services) {
    for(IUIDatasourceAdminService  service:services) {
      registerService(service);
    }
  }

 // private void registerService(JSUIDatasourceService jsDatasourceService) {
 //  registerService(jsDatasourceService);
 // }
  
  public void registerService(IUIDatasourceAdminService service) {
    serviceMap.put(service.getType(), service);
  }

  public IUIDatasourceAdminService getService(String serviceType) {
    return serviceMap.get(serviceType);
  }

  public void getIds(final XulServiceCallback<List<IDatasourceInfo>> mainCallback) {
    final List<IDatasourceInfo> datasourceList = new ArrayList<IDatasourceInfo>();
  
    final int asyncCallCount = serviceMap.size();
    
    final ICallback<Void> counterCallback = new ICallback<Void>() {
      int counter = 0;
      @Override
      public void onHandle(Void o) {
        counter++;
        if (counter >= asyncCallCount) {
          mainCallback.success(datasourceList);
        }
      }
    };
    for(IUIDatasourceAdminService service:serviceMap.values()) {
        service.getIds(new XulServiceCallback<List<IDatasourceInfo>>() {
 
          @Override
          public void success(List<IDatasourceInfo> list) {
            datasourceList.addAll(list);
            counterCallback.onHandle(null);
          }
 
          @Override
          public void error(String message, Throwable error) {
            mainCallback.error(message, error);
          }
        });
    }
  }


  public List<String> getTypes() {
    return new ArrayList<String>(serviceMap.keySet());
  }
}