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

import org.pentaho.platform.dataaccess.datasource.wizard.controllers.IWizardController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * This panel contains all the buttons.
 *
 * @author Thomas Morgner
 */
public class WizardControllerPanel {
  private IWizardController controller;
  public WizardControllerPanel(final IWizardController controller) {
    if (controller == null) {
      throw new NullPointerException();
    }
    this.controller = controller;
  }

  public IWizardController getController() {
    return controller;
  }
  
  /**
   * @param mainWizardContainer
   */
  public void addContent(final XulDomContainer mainWizardContainer) throws XulException {
    mainWizardContainer.addEventHandler((XulEventHandler) controller);
  }
}
