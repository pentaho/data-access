/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * A single step in the wizard. The architecture assumes that the wizard-ui keeps synchronized with the model at all the
 * time, so that other steps can react to changes and update their own availability. Steps *should* preserve the user
 * input even when they temporarily enter a invalid UI state while they are not yet active.
 *
 * @author Thomas Morgner
 */
public interface IWizardStep extends XulEventHandler, XulEventSource {


  /**
   * setBindings()
   * <p/>
   * Allows concrete implementations to set their bindings for enclosed properties and Xul defined elements.
   */
  public void setBindings();

  /**
   * Checks, whether the step is currently valid. A step is valid, if it
   *
   * @return true, if the model matches the step's internal state, false otherwise.
   */
  public boolean isValid();


  /**
   * Set the validity of the step. Really only used to manually fire the valid binding/
   */
  public void setValid( boolean valid );

  /**
   * stepActivatingForward()
   * <p/>
   * Called if the step that is activating is higher in the list than the current one (next).
   */
  public void stepActivatingForward();

  /**
   * stepActivatingReverse()
   * <p/>
   * Called if the step that is activatig is lower in the list than the current active one (back)
   */
  public void stepActivatingReverse();

  /**
   * Called on a step just before it becomes deactivated (before the next active step is shown) on response to moving
   * forward in the step list (next).
   *
   * @return boolean indicating that this step should be allowed to become deactive
   */
  public boolean stepDeactivatingForward();

  /**
   * Called on a step just before it become deactivated (before the next active step is shown) on response to moving
   * backward in the step list (back)
   *
   * @return boolean indicating that this step should be allowed to become deactive
   */
  public boolean stepDeactivatingReverse();

  /**
   * @param IWizardModel
   * @throws XulException Initializes the step. This is where bindings should be created and UI references obtained.
   */
  public void init( IWizardModel wizardModel ) throws XulException;

  public XulComponent getUIComponent();

  public void activating() throws XulException;

  /**
   * @return a string (must be localized) that describes this step
   */
  public String getStepName();

  /**
   * isDisabled()
   *
   * @return boolean that indicates if this step is active in the current linear controller
   * <p/>
   * If this returns false the linear wizard controller will skip this step and move to the next enabled step.
   */
  public boolean isDisabled();

  void deactivate();

  void refresh();
}
