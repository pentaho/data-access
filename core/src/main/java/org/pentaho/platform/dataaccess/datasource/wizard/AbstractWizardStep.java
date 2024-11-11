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
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulImage;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulGrid;
import org.pentaho.ui.xul.containers.XulRow;
import org.pentaho.ui.xul.containers.XulRows;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.tags.GwtImage;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Todo: Document Me
 *
 * @author William Seyler
 */
public abstract class AbstractWizardStep extends AbstractXulEventHandler implements IWizardStep {

  public static final String VALID_PROPERTY_NAME = "valid"; //$NON-NLS-1$

  public static final String PREVIEWABLE_PROPERTY_NAME = "previewable"; //$NON-NLS-1$

  public static final String FINISHABLE_PROPERTY_NAME = "finishable"; //$NON-NLS-1$

  public static final String DISABLED_PROPERTY_NAME = "disabled"; //$NON-NLS-1$

  public static final String STEP_GRID_ID = "step_grid"; //$NON-NLS-1$

  public static final String STEP_ROWS_ID = "step_rows"; //$NON-NLS-1$

  public static final String XUL_ROW_TYPE = "row"; //$NON-NLS-1$

  public static final String XUL_IMAGE_TYPE = "image"; //$NON-NLS-1$

  public static final String XUL_LABEL_TYPE = "label"; //$NON-NLS-1$

  public static final String STEP_IMAGE_SRC = "images/spacer.gif"; //$NON-NLS-1$

  public static final String SPACER_IMAGE_SRC = "images/empty_spacer.png"; //$NON-NLS-1$

  protected GwtXulDomContainer mainContainer;

  private boolean disabled = false;

  private boolean valid;

  protected BindingFactory bf;

  protected XulImage stepImage;

  protected XulLabel stepLabel;

  protected XulRow stepRow;

  protected boolean activated;

  protected IWizardDatasource parentDatasource;

  protected IWizardModel wizardModel;

  protected AbstractWizardStep( IWizardDatasource parentDatasource ) {
    super();
    this.parentDatasource = parentDatasource;
  }

  /**
   * Checks, whether the step is currently valid. This returns false as soon as any of the properties changed.
   *
   * @return true, if the model matches the step's internal state, false otherwise.
   */
  @Bindable
  public boolean isValid() {
    return valid;
  }

  @Bindable
  public void setValid( final boolean valid ) {
    this.valid = valid;

    this.firePropertyChange( VALID_PROPERTY_NAME, !valid, this.valid );
  }

  /**
   * @throws XulException
   */
  public void init( IWizardModel wizardModel ) throws XulException {
    this.wizardModel = wizardModel;
    bf = new GwtBindingFactory( document );
    this.setBindings();
  }

  @Override
  public void activating() throws XulException {

    // get the grid itself so we can update it later
    final XulGrid stepGrid = (XulGrid) document.getElementById( STEP_GRID_ID );

    // grab the rows and add a new row to it
    final XulRows stepRows = (XulRows) document.getElementById( STEP_ROWS_ID );
    stepRow = (XulRow) document.createElement( XUL_ROW_TYPE );
    stepRows.addChild( stepRow );

    // Create and add the activeImage to the row (goes in the first column)
    stepImage = (XulImage) document.createElement( XUL_IMAGE_TYPE );
    stepImage.setSrc( STEP_IMAGE_SRC );
    stepImage.setId( this.getStepName() );
    stepImage.setVisible( false );
    ( (GwtImage) stepImage ).setAttribute( "pen:classname", "pentaho-chevron" ); //$NON-NLS-1$ //$NON-NLS-2$
    stepRow.addChild( stepImage );

    // Create and add the text label to the row (goes in the second column)
    stepLabel = (XulLabel) document.createElement( XUL_LABEL_TYPE );
    stepLabel.setValue( this.getStepName() );
    stepLabel.setFlex( 1 );
    stepLabel.setTooltiptext( this.getStepName() );
    stepRow.addChild( stepLabel );

    stepGrid.update();
    activated = true;
  }

  public void deactivate() {

    XulGrid stepGrid = (XulGrid) document.getElementById( STEP_GRID_ID );
    XulRows stepRows = (XulRows) document.getElementById( STEP_ROWS_ID );
    stepRows.removeChild( stepRow );
    stepGrid.update();
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled( boolean disabled ) {
    boolean oldDisabled = this.disabled;
    this.disabled = disabled;
    if ( stepLabel != null ) {
      stepLabel.setDisabled( this.disabled );
    }

    this.firePropertyChange( DISABLED_PROPERTY_NAME, oldDisabled, this.disabled );
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#stepActivatingForward()
   */
  public void stepActivatingForward() {
    setStepImageVisible( true );
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#stepActivatingReverse()
   */
  public void stepActivatingReverse() {
    setStepImageVisible( true );
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#stepDeactivatingForward()
   */
  public boolean stepDeactivatingForward() {
    setStepImageVisible( false );
    return true;
  }

  public void setStepImageVisible( boolean visible ) {
    if ( stepImage != null ) {
      stepImage.setVisible( visible );
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#stepDeactivatingReverse()
   */
  public boolean stepDeactivatingReverse() {
    return stepDeactivatingForward();
  }

}
