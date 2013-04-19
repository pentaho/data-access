package org.pentaho.platform.dataaccess.datasource.ui.importing;

public interface IOverwritableController {

  public void setOverwrite(boolean overwrite);
  public void removeHiddenPanels();
  public void buildAndSetParameters();

}
