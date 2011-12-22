package org.pentaho.platform.dataaccess.datasource;

public interface AdminDatasourceListener<T> {

    void onSuccess(T returnValue);

    void onCancel();
        
    void onError(String Message, Throwable error);
}
