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

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.dom.Document;

/**
 * A single step in the wizard. The architecture assumes that the wizard-ui keeps synchronized with
 * the model at all the time, so that other steps can react to changes and update their own availability.
 * Steps *should* preserve the user input even when they temporarily enter a invalid UI state while they
 * are not yet active. 
 *
 * @author Thomas Morgner
 */
public interface IWizardStep {
  
  
  /**
   * setBindings()
   * 
   * Allows concrete implementations to set their bindings for enclosed properties
   * and Xul defined elements.
   */
  public void setBindings();

  /**
   * Checks, whether the step is currently valid. A step is valid, if it
   *
   * @return true, if the model matches the step's internal state, false otherwise.
   */
  public boolean isValid();

  /**
   * stepActivatingForward()
   * 
   * Called if the step that is activating is higher in the list than the current one (next).
   */
  public void stepActivatingForward();

  /**
   * stepActivatingReverse()
   * 
   * Called if the step that is activatig is lower in the list than the current active one (back)
   */
  public void stepActivatingReverse();
  
  /**
   * Called on a step just before it becomes deactivated (before the next active step is shown)
   * on response to moving forward in the step list (next).
   *
   * @return boolean indicating that this step should be allowed to become deactive
   */
  public boolean stepDeactivatingForward();

  /**
   * Called on a step just before it become deactivated (before the next active step is shown)
   * on response to moving backward in the step list (back)
   * 
   * @return boolean indicating that this step should be allowed to become deactive
   */
  public boolean stepDeactivatingReverse();
  
  /**
   * setFinishable()
   * @param finishable
   * 
   * sets the flag that determines if the "Finish" button should be enabled for this
   * wizard panel.
   */
  public void setFinishable(boolean finishable);
  
  /**
   * @return a boolean that determines if the "Finish" button should be enabled.
   */
  public boolean isFinishable();
  
  /**
   * @param mainWizardContainer
   * @throws XulException
   * 
   * Initializes the step. This is where bindings should be created and UI references obtained.
   */
  public void init(XulDomContainer mainWizardContainer) throws XulException;

  public XulComponent getUIComponent();

  public void activating() throws XulException;
  
  public void setBindingFactory(BindingFactory bindingFactory);
  
  public void setDocument(Document document);

  public void setDatasourceModel(DatasourceModel model);
  
  /**
   * @return a string (must be localized) that describes this step
   */
  public String getStepName();
  
  /**
   * isDisabled()
   * 
   * @return boolean that indicates if this step is active in the current linear controller
   * 
   * If this returns false the linear wizard controller will skip this step and move to the next
   * enabled step.
   */
  public boolean isDisabled();
  
  /**
   * setDisabled(boolean disable);
   * 
   * @param enable boolean that indicates if this step should be enabled in the current chain
   * 
   * Note: it is an error to call this method on a step that is currently being displayed.
   */
  public void setDisabled(boolean disabled);

  void deactivate();
}
