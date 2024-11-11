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


package org.pentaho.platform.dataaccess.datasource.utils;

public class WaitPopupUtil {
  public static native void showWaitPopup() /*-{
    window.parent.executeCommand("ShowWaitPopupCommand")
  }-*/;

  public static native void hideWaitPopup() /*-{
    window.parent.executeCommand("HideWaitPopupCommand")
  }-*/;
}
