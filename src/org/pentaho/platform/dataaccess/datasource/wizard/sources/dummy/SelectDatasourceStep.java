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

package org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * User: nbaker
 * Date: 3/23/11
 */
public class SelectDatasourceStep extends AbstractWizardStep {
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
      if(datasource == null){
        return;
      }
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
    datasource.getSteps().get(0).refresh();
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
      return wrappedStep.isValid() && wizardModel.getDatasourceName() != null && !wizardModel.getDatasourceName().isEmpty();
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

  @Override
  public void refresh() {

  }

}
