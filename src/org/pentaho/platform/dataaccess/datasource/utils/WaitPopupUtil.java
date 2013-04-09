package org.pentaho.platform.dataaccess.datasource.utils;

public class WaitPopupUtil {
  public static native void showWaitPopup() /*-{
                                            window.top.executeCommand("ShowWaitPopupCommand")
                                            }-*/;

  public static native void hideWaitPopup() /*-{
                                            window.top.executeCommand("HideWaitPopupCommand")
                                            }-*/;
}
