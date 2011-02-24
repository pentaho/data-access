/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jun 23, 2010 
 * @author wseyler
 */


package org.pentaho.platform.dataaccess.datasource.wizard.steps;

import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;

/**
 * @author wseyler
 *
 */
public class VisualizeStep extends AbstractWizardStep<DatasourceModel> {

  private DatasourceMessages datasourceMessages;

  public VisualizeStep(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#getStepName()
   */
  public String getStepName() {
    return datasourceMessages.getString("wizardStepName.VISUALIZE"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#setBindings()
   */
  public void setBindings() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#stepActivatingForward()
   */
  public void stepActivatingForward() {
    super.stepActivatingForward();
    setValid(false);
  }

}
