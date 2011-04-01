package org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy;

import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * User: nbaker
 * Date: 3/23/11
 */
public class SelectDatasourceStep extends AbstractWizardStep {
  private XulMenuList datatypeMenuList;
  private IWizardDatasource datasource;

  private XulDeck datasourceDeck;
  private IWizardStep wrappedStep;

  private PropertyChangeListener validListener = new PropertyChangeListener(){
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      SelectDatasourceStep.this.firePropertyChange("valid", !isValid(), isValid());
    }
  };

  private PropertyChangeListener finishableListener = new PropertyChangeListener(){
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      parentDatasource.setFinishable(datasource.isFinishable());
    }
  };
  private IWizardModel wizardModel;

  public SelectDatasourceStep( DummyDatasource parentDatasource){
    super(parentDatasource);
  }

  @Override
  public void init(IWizardModel wizardModel) throws XulException {
    this.wizardModel = wizardModel;

    datasourceDeck = (XulDeck) document.getElementById("datasourceDialogDeck");
    super.init(wizardModel);
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(wizardModel, "selectedDatasource", this, "selectedDatasource");
  }

  @Override
  public void setBindings() {

  }

  @Override
  public XulComponent getUIComponent() {
    return document.getElementById("sourcestep");
  }
  
  @Override
  @Bindable
  public String getStepName() {
    return MessageHandler.getString("datasourceDialog.SelectDatabaseType");
  }

  @Bindable
  public void setSelectedDatasource(IWizardDatasource datasource){
    if(datasource instanceof DummyDatasource){
      this.datasourceDeck.setSelectedIndex(0);
      this.datasource = null;
      this.wrappedStep = null;
      return;
    }
    if(datasource == null){
      return;
    }
    if(this.datasource != null){
      this.wrappedStep.removePropertyChangeListener(validListener);
      this.datasource.removePropertyChangeListener(finishableListener);
    }
    this.wrappedStep = datasource.getSteps().get(0);
    this.datasource = datasource;
    this.wrappedStep.addPropertyChangeListener(validListener);
    this.datasource.addPropertyChangeListener(finishableListener);

    this.datasourceDeck.setSelectedIndex(datasourceDeck.getChildNodes().indexOf(datasource.getSteps().get(0).getUIComponent()));
    firePropertyChange("valid", !isValid(), isValid());
  }

  @Override
  public void activating() throws XulException {
    super.activating();
    if(wrappedStep != null){
      wrappedStep.activating();
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    if(wrappedStep != null){
      wrappedStep.deactivate();
    }
  }

  @Override
  public boolean isValid() {
    if(wrappedStep != null){
      return wrappedStep.isValid();
    } else {
      return false;
    }
  }

  @Override
  public void stepActivatingForward() {
    super.stepActivatingForward();
    if(wrappedStep != null){
      wrappedStep.stepActivatingForward();
    }
  }

  @Override
  public void stepActivatingReverse() {
    super.stepActivatingReverse();
    if(wrappedStep != null){
      wrappedStep.stepActivatingReverse();
    }
  }

  @Override
  public boolean stepDeactivatingForward() {
    boolean superReturn = super.stepDeactivatingForward();
    if(wrappedStep != null){
        return wrappedStep.stepDeactivatingReverse();
    } else {
      return superReturn;
    }
  }

  @Override
  public boolean stepDeactivatingReverse() {
    boolean superReturn = super.stepDeactivatingReverse();
    if(wrappedStep != null){
      return wrappedStep.stepDeactivatingReverse();
    } else {
      return superReturn;
    }
  }

}
