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

import org.pentaho.ui.xul.binding.BindingFactory;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public interface IWizardController {
  public static final String ACTIVE_STEP_PROPERTY_NAME = "activeStep"; //$NON-NLS-1$

  public static final String STEP_COUNT_PROPERTY_NAME = "stepCount"; //$NON-NLS-1$

  public static final String FINISHABLE_PROPERTY_NAME = "finishable"; //$NON-NLS-1$

  public static final String PREVIEWABLE_PROPERTY_NAME = "previewable"; //$NON-NLS-1$

  public IWizardStep getStep( int step );

  public int getStepCount();

  public void setActiveStep( int step );

  public int getActiveStep();

  public void init();

  public void cancel();

  public void finish();

  public void setBindingFactory( BindingFactory factory );

  public BindingFactory getBindingFactory();
}
