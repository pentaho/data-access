package org.pentaho.platform.dataaccess.datasource.modeler;

import org.pentaho.agilebi.modeler.IUriHandler;

public class GwtUriHandler implements IUriHandler {

  @Override
  public void openUri(String uri) {
    openUriInPuc("Infocenter Help", uri);
  }
  
  private native static void openUriInPuc(String title, String uri)/*-{
    if (typeof(window) === "object"
    &&  typeof(window.top) === "object"
    &&  typeof(window.top.urlCommand) === "function"
    ) {
      window.top.urlCommand(uri, title, false, 0, 0);
    }
    else {
      window.open(uri);
    }
  }-*/;
}
