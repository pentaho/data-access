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

import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.*;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.DummyDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.SelectDatasourceStep;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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

  private IWizardModel wizardModel;
  private IXulAsyncDSWDatasourceService datasourceService;
  private SelectDatasourceStep datasourceStep;
  private XulTextbox datasourceName;
  private List<IWizardDatasource> datasources = new ArrayList<IWizardDatasource>();
  private IWizardDatasource activeDatasource;
  private String invalidCharacters;
  public static final String DEFAULT_INVALID_CHARACTERS = "$<>?&#%^*()!~:;[]{}|"; //$NON-NLS-1$

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

  private final static String NEXT_BTN_ELEMENT_ID = "main_wizard_window_extra2"; //$NON-NLS-1$

  private final static String BACK_BTN_ELEMENT_ID = "main_wizard_window_extra1"; //$NON-NLS-1$

  private final static String FINISH_BTN_ELEMENT_ID = "main_wizard_window_accept"; //$NON-NLS-1$

  private final static String CONTENT_DECK_ELEMENT_ID = "content_deck"; //$NON-NLS-1$

  private ArrayList<IWizardStep> steps;

  private int activeStep = -1; // bogus active step

  private BindingFactory bf;

  private XulDialog warningDialog;
  
  private Binding nextButtonBinding, finishedButtonBinding;

  private NotDisabledBindingConvertor notDisabledBindingConvertor;

  private List<IWizardListener> wizardListeners = new ArrayList<IWizardListener>();

  private XulDialog wizardDialog;
  
  private XulDialog summaryDialog;

  private XulMenuList datatypeMenuList;

  private XulButton finishButton;
  
  private DummyDatasource dummyDatasource = new DummyDatasource();
  private SelectDatasourceStep selectDatasourceStep;

  public MainWizardController(final BindingFactory bf, IWizardModel wizardModel, IXulAsyncDSWDatasourceService datasourceService) {
    this.wizardModel = wizardModel;
    this.datasourceService = datasourceService;
    this.steps = new ArrayList<IWizardStep>();
    this.bf = bf;
    this.notDisabledBindingConvertor = new NotDisabledBindingConvertor();

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

      // update the controller panel
      final XulDeck deck = (XulDeck) document.getElementById(CONTENT_DECK_ELEMENT_ID);
      deck.setSelectedIndex(deck.getChildNodes().indexOf(activatingWizardStep.getUIComponent()));

      if (activeStep > oldActiveStep) {
        activatingWizardStep.stepActivatingForward();
      } else {
        activatingWizardStep.stepActivatingReverse();
      }

      this.firePropertyChange(ACTIVE_STEP_PROPERTY_NAME, oldActiveStep, this.activeStep);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public int getActiveStep() {
    return activeStep;
  }

  public void init() {

    // We need the SelectDatasourceStep at all times, add it now

    wizardDialog = (XulDialog) document.getElementById("main_wizard_window");

    summaryDialog = (XulDialog) document.getElementById("summaryDialog");

    finishButton  = (XulButton) document.getElementById(FINISH_BTN_ELEMENT_ID);

    datasourceName = (XulTextbox) document.getElementById("datasourceName"); //$NON-NLS-1$
    bf.createBinding(datasourceName, "value", wizardModel, "datasourceName");
    wizardModel.addPropertyChangeListener(new PropertyChangeListener(){
      @Override
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if(propertyChangeEvent.getPropertyName().equals("datasourceName")){
          steps.get(activeStep).setValid(steps.get(activeStep).isValid());
        }
      }
    });

    bf.setBindingType(Binding.Type.ONE_WAY);

    datatypeMenuList = (XulMenuList) document.getElementById("datatypeMenuList");


    Binding datasourceBinding = bf.createBinding(wizardModel, "datasources", datatypeMenuList, "elements");
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datatypeMenuList, "selectedItem", wizardModel, "selectedDatasource");


    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(wizardModel, "selectedDatasource", this, "selectedDatasource");
    bf.createBinding(this, ACTIVE_STEP_PROPERTY_NAME, BACK_BTN_ELEMENT_ID, DISABLED_PROPERTY_NAME, new BackButtonBindingConverter());

    dummyDatasource = ((DummyDatasource) wizardModel.getDatasources().iterator().next());
    activeDatasource = dummyDatasource;
    selectDatasourceStep = dummyDatasource.getSelectDatasourceStep();
    
    try {
      for(IWizardDatasource datasource : wizardModel.getDatasources()){
          datasource.init(getXulDomContainer(), wizardModel);
      } 
      steps.add(selectDatasourceStep);
      selectDatasourceStep.activating();
      setActiveStep(0);
      datasourceBinding.fireSourceChanged();
      setSelectedDatasource(dummyDatasource);
      datasourceService.getDatasourceIllegalCharacters(new XulServiceCallback<String>() {

        @Override
        public void success(String retVal) {
          invalidCharacters = retVal;
          
        }

        @Override
        public void error(String message, Throwable error) {
          invalidCharacters = DEFAULT_INVALID_CHARACTERS;
          
        }
      });
    } catch (XulException e) {
      MessageHandler.getInstance().showErrorDialog("Error", e.getMessage());
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      MessageHandler.getInstance().showErrorDialog("Error", e.getMessage());
      e.printStackTrace();
    }


  }

  @Bindable
  public void setSelectedDatasource(IWizardDatasource datasource){
    IWizardDatasource prevSelection = activeDatasource;
    activeDatasource = datasource;
    if(datasource == null || prevSelection == activeDatasource){
      return;
    }
    try {
      datasource.activating();
      if(prevSelection != null){
        steps.removeAll(prevSelection.getSteps());
        prevSelection.deactivating();
      }

      for(int i=1; i<datasource.getSteps().size(); i++){
        steps.add(datasource.getSteps().get(i));
      }
      steps.addAll(datasource.getSteps());
      wizardModel.setSelectedDatasource(datasource);
      activeStep = 0;

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

    finishedButtonBinding = bf.createBinding(activeDatasource, FINISHABLE_PROPERTY_NAME, FINISH_BTN_ELEMENT_ID, DISABLED_PROPERTY_NAME, notDisabledBindingConvertor);

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
    
    if (finishButton.isDisabled()) {
      return;
    }
    finishButton.setDisabled(true);    
    final String datasourceName = this.wizardModel.getDatasourceName();

    // Validating whether the datasource name contains any illegal characters
    if(isDatasourceNameValid(datasourceName)) {
      datasourceService.listDatasourceNames(new XulServiceCallback<List<String>>() {
  
        @Override
        public void success(List<String> datasourceNames) {
          finishButton.setDisabled(false);
          boolean isEditing = wizardModel.isEditing();
          if(datasourceNames.contains(datasourceName) && !isEditing) {
            showWarningDialog();
          } else {
            setFinished();
          }
        }
  
        @Override
        public void error(String s, Throwable throwable) {
          finishButton.setDisabled(false);
          throwable.printStackTrace();
          MessageHandler.getInstance().showErrorDialog(throwable.getMessage());
        }
      });
    } else {
      finishButton.setDisabled(false);
      MessageHandler.getInstance().showErrorDialog("Error", MessageHandler//$NON-NLS-1$
          .getString("DatasourceEditor.ERROR_0005_INVALID_DATASOURCE_NAME", invalidCharacters), true); //$NON-NLS-1$ 
    }
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
    activeDatasource.onFinish(new XulServiceCallback<IDatasourceSummary>() {
      @Override
      public void success(IDatasourceSummary iDatasourceSummary) {

        iDatasourceSummary.getDomain().getLogicalModels().get(0).setProperty("DatasourceType", activeDatasource.getId());
        for (IWizardListener wizardListener : wizardListeners) {
          wizardListener.onFinish(iDatasourceSummary);
        }
      }

      @Override
      public void error(String s, Throwable throwable) {
        throwable.printStackTrace();
        //TODO: improve error messaging
        MessageHandler.getInstance().closeWaitingDialog();
        MessageHandler.getInstance().showErrorDialog("Error", ExceptionParser //$NON-NLS-1$
            .getErrorMessage(throwable, MessageHandler.getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED")), true); //$NON-NLS-1$  
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
    summaryDialog.hide();
    wizardDialog.show();
  }

  private boolean isDatasourceNameValid(String datasourceName) {
    return containsNone(datasourceName, invalidCharacters);
  }
  
  /**
   * Checks that the String does not contain certain characters.
   *
   * @param str  the String to check, may be null
   * @param invalidChars  an String of invalid chars, may be null
   * @return true if it contains none of the invalid chars, or is null
   */
  private boolean containsNone(String str, String invalidChars) {
      if (str == null || invalidChars == null) {
          return true;
      }
      char[] invalidCharsArray = null;
      int strSize = str.length();
      if(invalidChars != null) {
        invalidCharsArray = invalidChars.toCharArray();
      }
      int validSize = invalidCharsArray.length;
      for (int i = 0; i < strSize; i++) {
          char ch = str.charAt(i);
          for (int j = 0; j < validSize; j++) {
              if (invalidCharsArray[j] == ch) {
                  return false;
              }
          }
      }
      return true;
  }
}
