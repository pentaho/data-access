package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.Set;

/**
 * User: nbaker
 * Date: 3/30/11
 */
public interface IWizardModel extends XulEventSource {
  @Bindable
  String getDatasourceName();

  @Bindable
  void setDatasourceName(String datasourceName);

  Set<IWizardDatasource> getDatasources();

  void addDatasource(IWizardDatasource datasource);

  void removeDatasourceByType(Class<? extends IWizardDatasource> datasource);

  boolean isEditing();

  void setEditing(boolean editing);

  void setSelectedDatasource(IWizardDatasource datasource);
  IWizardDatasource getSelectedDatasource();
  void reset();

  boolean isReportingOnlyValid();
  void setReportingOnlyValid(boolean valid);
}
