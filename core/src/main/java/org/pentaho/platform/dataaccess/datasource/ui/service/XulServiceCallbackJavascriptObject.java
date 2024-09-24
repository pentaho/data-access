/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.ui.service;

import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.JavaScriptObject;

public class XulServiceCallbackJavascriptObject {

  XulServiceCallback<Boolean> xulCallback;
  public XulServiceCallbackJavascriptObject( XulServiceCallback<Boolean> callback ) { xulCallback = callback; }

  private void onSuccess( boolean flag  ) { xulCallback.success( new Boolean( flag ) ); }

  private void onError( String errorMsg ) { xulCallback.error( errorMsg, null ); }

  public native JavaScriptObject getJavascriptObject()/*-{
    var thisInstance = this;
    return {
      success : function(flag){
        thisInstance.@org.pentaho.platform.dataaccess.datasource.ui.service.XulServiceCallbackJavascriptObject::onSuccess(Z)(flag);
      },
      error : function(errorMsg) {
        thisInstance.@org.pentaho.platform.dataaccess.datasource.ui.service.XulServiceCallbackJavascriptObject::onError(Ljava/lang/String;)(errorMsg);
      }
    }
  }-*/;
}
