package org.pentaho.platform.dataaccess.datasource.ui.service;

public interface AdminDatasourceListener<T> {

    void onSuccess(T returnValue);

    void onCancel();
        
    void onError(String Message, Throwable error);
}
