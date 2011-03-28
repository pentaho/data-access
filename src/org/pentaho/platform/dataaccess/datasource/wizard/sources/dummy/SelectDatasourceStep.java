package org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy;

import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * User: nbaker
 * Date: 3/23/11
 */
public class SelectDatasourceStep extends AbstractWizardStep {
  private XulMenuList datatypeMenuList;

  public SelectDatasourceStep( DummyDatasource parentDatasource){
    super(parentDatasource);
  }

  @Override
  public void setBindings() {

  }

  @Override
  public XulComponent getUIComponent() {
    return document.getElementById("NoopDatasource");
  }
  
  @Override
  @Bindable
  public String getStepName() {
    return MessageHandler.getString("datasourceDialog.SelectDatabaseType");
  }

}
