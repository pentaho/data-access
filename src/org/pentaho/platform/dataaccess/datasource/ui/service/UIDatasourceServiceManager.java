package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.RequestCallback;

public class UIDatasourceServiceManager {

  Map<String, IUIDatasourceAdminService> serviceMap = new HashMap<String, IUIDatasourceAdminService>();
  private static UIDatasourceServiceManager instance;
  
  private UIDatasourceServiceManager() {
  }

  public static UIDatasourceServiceManager getInstance() {
    if (instance == null) {
      instance = new UIDatasourceServiceManager();
    }
    return instance;
  }
  
  public UIDatasourceServiceManager(List<IUIDatasourceAdminService> services) {
    for(IUIDatasourceAdminService  service:services) {
      registerService(service);
    }
  }

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
        	if(mainCallback != null) { 
        		mainCallback.success(datasourceList);
        	}
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
        	  if(mainCallback != null) {
        		  mainCallback.error(message, error);
        	  }
          }
        });
    }
  }

  public void exportDatasource(IDatasourceInfo dsInfo) {
    for(IUIDatasourceAdminService service:serviceMap.values()) {
      if (service.getType().equals(dsInfo.getType()) && dsInfo.isExportable()) {
        service.export(dsInfo);
        break;
      }
    }
  }
  
  /**
   * @param dsInfo
   */
  public void remove(IDatasourceInfo dsInfo, XulServiceCallback<Boolean> callback) {
    for(IUIDatasourceAdminService service:serviceMap.values()) {
      if (service.getType().equals(dsInfo.getType()) && dsInfo.isRemovable()) {
        service.remove(dsInfo, callback);
        break;
      }
    }
  }

  public List<String> getTypes() {
    return new ArrayList<String>(serviceMap.keySet());
  }

}