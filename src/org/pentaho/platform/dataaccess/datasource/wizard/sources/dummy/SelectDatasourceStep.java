package org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy;

import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.AbstractWizardStep;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.lang.reflect.InvocationTargetException;

/**
 * User: nbaker
 * Date: 3/23/11
 */
public class SelectDatasourceStep extends AbstractWizardStep {
  private XulMenuList datatypeMenuList;

  @Override
  public void setBindings() {

    bf.setBindingType(Binding.Type.ONE_WAY);
    datatypeMenuList = (XulMenuList) document.getElementById("datatypeMenuList");

    Binding datasourceBinding = bf.createBinding(datasourceModel, "datasources", datatypeMenuList, "elements");
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datatypeMenuList, "selectedItem", datasourceModel, "selectedDatasource");


    try {
      datasourceBinding.fireSourceChanged();
    } catch (Exception e) {
      MessageHandler.getInstance().showErrorDialog(e.getMessage());
      e.printStackTrace();
    }

  }

  @Override
  public XulComponent getUIComponent() {
    return document.getElementById("NoopDatasource");
  }

  @Override
  @Bindable
  public String getStepName() {
    return MessageHandler.getInstance().messages.getString("datasourceDialog.SelectDatabaseType");
  }
}
