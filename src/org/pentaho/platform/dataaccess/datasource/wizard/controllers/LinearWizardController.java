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

import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

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
public class LinearWizardController extends AbstractXulEventHandler implements IWizardController {

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

  private boolean canceled;

  private boolean finished;

  private XulDomContainer mainXULContainer;
  
  private ICsvDatasourceServiceAsync csvDatasourceService;

  private BindingFactory bf;

  private XulDialog warningDialog;
  
  private Binding nextButtonBinding, finishedButtonBinding;

  private NotDisabledBindingConvertor notDisabledBindingConvertor;
  
  private DatasourceModel datasourceModel;

  public LinearWizardController(final BindingFactory bf, final DatasourceModel datasourceModel) {
    this.steps = new ArrayList<IWizardStep>();
    this.bf = bf;
    this.notDisabledBindingConvertor = new NotDisabledBindingConvertor();
    this.datasourceModel = datasourceModel;
    
    this.csvDatasourceService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) this.csvDatasourceService;
    endpoint.setServiceEntryPoint(PhysicalDatasourceController.getDatasourceURL());
  }

  public void addStep(final AbstractWizardStep step) {
    if (step == null) {
      throw new NullPointerException();
    }
    steps.add(step);
  }

  public void removeStep(final IWizardStep step) {
    steps.remove(step);
  }

  public IWizardStep getStep(final int step) {
    return steps.get(step);
  }

  public int getStepCount() {
    return steps.size();
  }

  public void setActiveStep(final int step) {
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
    final XulDeck deck = (XulDeck) mainXULContainer.getDocumentRoot().getElementById(CONTENT_DECK_ELEMENT_ID);
    deck.setSelectedIndex(activeStep);

    this.firePropertyChange(ACTIVE_STEP_PROPERTY_NAME, oldActiveStep, this.activeStep);
  }

  public int getActiveStep() {
    return activeStep;
  }

  public void initialize() {
    if (!steps.isEmpty()) {
      for (final IWizardStep wizardStep : steps) {
        wizardStep.setBindings();
      }
      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(this, ACTIVE_STEP_PROPERTY_NAME, BACK_BTN_ELEMENT_ID, DISABLED_PROPERTY_NAME, new BackButtonBindingConverter());

      setActiveStep(0); // Fires the events to update the buttons
    }

    setCancelled(false);
    setFinished(false);
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
    setCancelled(true);
    setFinished(false);
  }

  public void setCancelled(final boolean canceled) {
    final boolean oldCanceled = this.canceled;
    this.canceled = canceled;
    this.firePropertyChange(CANCELLED_PROPERTY_NAME, oldCanceled, this.canceled);
  }

  public boolean isCancelled() {
    return canceled;
  }

  @Bindable
  public void finish() {
	final String datasourceName = this.datasourceModel.getDatasourceName();  
	csvDatasourceService.listDatasourceNames(new AsyncCallback<List<String>>() {
		public void onSuccess(List<String> datasourceNames) {
			boolean isEditing = datasourceModel.getGuiStateModel().isEditing();
			if(datasourceNames.contains(datasourceName) && !isEditing) {
				showWarningDialog();
			} else {
  			    setFinished(true);
				setCancelled(false);
			}
		}
		
		public void onFailure(Throwable e ) {
			 warningDialog.hide();
		}
	});
  }

  @Bindable
  public void overwriteDialogAccept() {
	  warningDialog.hide();
	  setFinished(true);
	  setCancelled(false);
  }
  
  @Bindable
  public void overwriteDialogCancel() {
	  warningDialog.hide();
  }
  
  public boolean isFinished() {
    return finished;
  }

  public void setFinished(final boolean finished) {
    final boolean oldFinished = this.finished;
    this.finished = finished;
    this.firePropertyChange(FINISHED_PROPERTY_NAME, oldFinished, this.finished);
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
    return "wizard_controller"; //$NON-NLS-1$
  }

  public void onLoad() {
    initialize();
  }

  /**
   * @param mainWizardContainer
   */
  public void registerMainXULContainer(final XulDomContainer mainWizardContainer) {
    mainXULContainer = mainWizardContainer;
    bf.setDocument(mainWizardContainer.getDocumentRoot());
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

}
