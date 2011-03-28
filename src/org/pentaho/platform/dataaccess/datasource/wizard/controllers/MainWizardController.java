/*
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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.wizard.*;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.DummyDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.SelectDatasourceStep;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * The wizard-controler manages the navigation between the wizard-panes. All panes are organized as a list, where
 * each panel cannot be enabled if the previous panels are not valid or enabled.
 * <p/>
 * It is possible to jump back to previous steps and change values there. In some cases, this will just update
 * the model, but in some cases this will invalidate the subsequent steps (for instance, if the query has been
 * changed).
 *
 * @author William Seyler
 */
public class MainWizardController extends AbstractXulEventHandler implements IWizardController {

  private IXulAsyncDatasourceService datasourceService;
  private SelectDatasourceStep datasourceStep;
  private IWizardDatasource selectedDatasource;
  private XulDeck datasourceDeck;
  private XulTextbox datasourceName;

  // Binding converters
  protected class BackButtonBindingConverter extends BindingConvertor<Integer, Boolean> {

    /* (non-Javadoc)
     * @see org.pentaho.ui.xul.binding.BindingConvertor#sourceToTarget(java.lang.Object)
     */
    @Override
    public Boolean sourceToTarget(final Integer value) {
      return !(value > 0);
    }

    /* (non-Javadoc)
     * @see org.pentaho.ui.xul.binding.BindingConvertor#targetToSource(java.lang.Object)
     */
    @Override
    public Integer targetToSource(final Boolean value) {
      return null;
    }

  }

  private final static String DISABLED_PROPERTY_NAME = "disabled"; //$NON-NLS-1$

  private final static String VALID_PROPERTY_NAME = "valid"; //$NON-NLS-1$

  private final static String NEXT_BTN_ELEMENT_ID = "next_btn"; //$NON-NLS-1$

  private final static String BACK_BTN_ELEMENT_ID = "back_btn"; //$NON-NLS-1$

  private final static String FINISH_BTN_ELEMENT_ID = "finish_btn"; //$NON-NLS-1$

  private final static String CONTENT_DECK_ELEMENT_ID = "content_deck"; //$NON-NLS-1$

  private ArrayList<IWizardStep> steps;

  private int activeStep = -1; // bogus active step

  private BindingFactory bf;

  private XulDialog warningDialog;
  
  private Binding nextButtonBinding, finishedButtonBinding;

  private NotDisabledBindingConvertor notDisabledBindingConvertor;
  
  private DatasourceModel datasourceModel;

  private List<IWizardListener> wizardListeners = new ArrayList<IWizardListener>();

  private XulDialog wizardDialog;
  
  private XulDialog summaryDialog;

  private XulMenuList datatypeMenuList;

  public MainWizardController(final BindingFactory bf, final DatasourceModel datasourceModel, IXulAsyncDatasourceService datasourceService) {
    this.datasourceService = datasourceService;
    this.steps = new ArrayList<IWizardStep>();
    this.bf = bf;
    this.notDisabledBindingConvertor = new NotDisabledBindingConvertor();
    this.datasourceModel = datasourceModel;


  }

  public IWizardStep getStep(final int step) {
    return steps.get(step);
  }

  public int getStepCount() {
    return steps.size();
  }

  public void setActiveStep(final int step) {
    try{
      if(this.steps == null || steps.isEmpty()){
        return;
      }
      final int oldActiveStep = this.activeStep;
      if (oldActiveStep >= 0) {
        final IWizardStep deactivatingWizardStep = steps.get(oldActiveStep);
        if (step > oldActiveStep) {
          if (!deactivatingWizardStep.stepDeactivatingForward()) {
            return;
          }
        } else {
          if (!deactivatingWizardStep.stepDeactivatingReverse()) {
            return;
          }
        }
      }

      this.activeStep = step;
      final IWizardStep activatingWizardStep = steps.get(activeStep);
      updateBindings();

      if (activeStep > oldActiveStep) {
        activatingWizardStep.stepActivatingForward();
      } else {
        activatingWizardStep.stepActivatingReverse();
      }

      // update the controller panel
      final XulDeck deck = (XulDeck) document.getElementById(CONTENT_DECK_ELEMENT_ID);
      deck.setSelectedIndex(activeStep);

      this.firePropertyChange(ACTIVE_STEP_PROPERTY_NAME, oldActiveStep, this.activeStep);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public int getActiveStep() {
    return activeStep;
  }

  public void init() {

    wizardDialog = (XulDialog) document.getElementById("main_wizard_window");

    summaryDialog = (XulDialog) document.getElementById("summaryDialog");

    datasourceDeck = (XulDeck) document.getElementById("datasourceDialogDeck");

    datasourceName = (XulTextbox) document.getElementById("datasourceName"); //$NON-NLS-1$
    bf.createBinding(datasourceName, "value", datasourceModel, "datasourceName");

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(this, ACTIVE_STEP_PROPERTY_NAME, BACK_BTN_ELEMENT_ID, DISABLED_PROPERTY_NAME, new BackButtonBindingConverter());

    bf.createBinding(datasourceModel, "selectedDatasource", this,"selectedDatasource");

    for(IWizardDatasource datasource : this.datasourceModel.getDatasources()){
      try {
        datasource.init(datasourceModel, getXulDomContainer());
      } catch (XulException e) {
        MessageHandler.getInstance().showErrorDialog("Error", e.getMessage());
        e.printStackTrace();
      } 
    }

    setSelectedDatasource(datasourceModel.getSelectedDatasource());

  }

  @Bindable
  public void setSelectedDatasource(IWizardDatasource datasource){
    IWizardDatasource prevSelection = selectedDatasource;
    selectedDatasource = datasource;
    try {
      datasource.activating();
      if(prevSelection != null){
        steps.removeAll(prevSelection.getSteps());
        prevSelection.deactivating();
      }
      steps.addAll(datasource.getSteps());
      datasource.getSteps().get(0).activating();
      activeStep = 0;

      this.datasourceDeck.setSelectedIndex(datasourceDeck.getChildNodes().indexOf(datasource.getSteps().get(0).getUIComponent()));
      updateBindings();

    } catch (XulException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public void reset(){
    setActiveStep(0);

  }
  protected void updateBindings() {
    // Destroy any old bindings
    if (nextButtonBinding != null) {
      nextButtonBinding.destroyBindings();
    }
    if (finishedButtonBinding != null) {
      finishedButtonBinding.destroyBindings();
    }

    // Create new binding to the current wizard panel
    bf.setBindingType(Binding.Type.ONE_WAY);
    nextButtonBinding = bf.createBinding(getStep(getActiveStep()), VALID_PROPERTY_NAME, NEXT_BTN_ELEMENT_ID, DISABLED_PROPERTY_NAME, notDisabledBindingConvertor);
    finishedButtonBinding = bf.createBinding(getStep(getActiveStep()), FINISHABLE_PROPERTY_NAME, FINISH_BTN_ELEMENT_ID, DISABLED_PROPERTY_NAME, notDisabledBindingConvertor);

    try {
      nextButtonBinding.fireSourceChanged();
      finishedButtonBinding.fireSourceChanged();
    } catch (Exception e) {
      //TODO add some exception handling here.
    }
  }
  
  @Bindable
  public void cancel() {
    setCancelled();
  }

  private void setCancelled() {
    for (IWizardListener wizardListener : wizardListeners) {
      wizardListener.onCancel();
    }
  }

  @Bindable
  // TODO: migrate to CSV datasource
  public void finish() {
    final String datasourceName = this.datasourceModel.getDatasourceName();
    datasourceService.listDatasourceNames(new XulServiceCallback<List<String>>() {

      @Override
      public void success(List<String> datasourceNames) {
        boolean isEditing = datasourceModel.getGuiStateModel().isEditing();
        if(datasourceNames.contains(datasourceName) && !isEditing) {
          showWarningDialog();
        } else {
          setFinished();
        }
      }

      @Override
      public void error(String s, Throwable throwable) {
        throwable.printStackTrace();
        MessageHandler.getInstance().showErrorDialog(throwable.getMessage());
      }
    });
  }

  @Bindable
  public void overwriteDialogAccept() {
	  warningDialog.hide();
	  setFinished();
  }
  
  @Bindable
  public void overwriteDialogCancel() {
	  warningDialog.hide();
  }

  private void setFinished() {

    wizardDialog.hide();
    MessageHandler.getInstance().showWaitingDialog();
    datasourceModel.getSelectedDatasource().onFinish(new XulServiceCallback<IDatasourceSummary>() {
      @Override
      public void success(IDatasourceSummary iDatasourceSummary) {

        iDatasourceSummary.getDomain().getLogicalModels().get(0).setProperty("DatasourceType", datasourceModel.getSelectedDatasource().getId());
        for (IWizardListener wizardListener : wizardListeners) {
          wizardListener.onFinish(iDatasourceSummary);
        }
      }

      @Override
      public void error(String s, Throwable throwable) {
        //TODO: improve error messaging
        MessageHandler.getInstance().closeWaitingDialog();
        MessageHandler.getInstance().showErrorDialog(s, s);
      }
    });

  }
  
  public void showWarningDialog() {
    warningDialog = (XulDialog) xulDomContainer.getDocumentRoot().getElementById("overwriteDialog");
    warningDialog.show();
  }

  // Button click methods
  @Bindable
  public void next() {
    for (int i=getActiveStep(); i<steps.size()-1; i++) {
      IWizardStep nextStep = getStep(i + 1);
      if (!nextStep.isDisabled()) {
        setActiveStep(i + 1);
        break;
      }
    }
  }

  @Bindable
  public void back() {
    for (int i=getActiveStep()-1; i>-1; i--) {
      IWizardStep lastStep = getStep(i);
      if (!lastStep.isDisabled()) {
        setActiveStep(i);
        break;
      }
    }
  }

  // Stuff for XUL
  @Override
  public String getName() {
    return "wizardController"; //$NON-NLS-1$
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardController#getBindingFactory()
   */
  public BindingFactory getBindingFactory() {
    return bf;
  }

  class NotDisabledBindingConvertor extends BindingConvertor<Boolean, Boolean> {
    public Boolean sourceToTarget(Boolean value) {
      return Boolean.valueOf(!value.booleanValue());
    }
    public Boolean targetToSource(Boolean value) {
      return Boolean.valueOf(!value.booleanValue());
    }
  }


  public void addWizardListener(IWizardListener listener){
    wizardListeners.add(listener);
  }

  public void removeWizardListener(IWizardListener listener){
    wizardListeners.remove(listener);
  }

  @Bindable
  public void editFieldSettings() {
    setFinished();
    summaryDialog.hide();
    wizardDialog.show();
  }

}
