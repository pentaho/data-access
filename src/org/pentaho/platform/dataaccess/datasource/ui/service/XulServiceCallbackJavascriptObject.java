/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.ui.service;

import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.JavaScriptObject;

public class XulServiceCallbackJavascriptObject {

  XulServiceCallback<Boolean> xulCallback;
  public XulServiceCallbackJavascriptObject (XulServiceCallback<Boolean> callback){
    xulCallback = callback;
  }
  
  private void onSuccess(boolean flag){
    xulCallback.success(new Boolean(flag));
  }
  
  private void onError(String errorMsg) {
    xulCallback.error(errorMsg, null);
  }
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
