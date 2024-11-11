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


package org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.Collections;
import java.util.List;

/**
 * User: nbaker Date: 3/23/11
 */
public class DummyDatasource extends AbstractXulEventHandler implements IWizardDatasource {
  SelectDatasourceStep datasourceStep;

  public DummyDatasource() {
    datasourceStep = new SelectDatasourceStep( this );

  }

  @Override
  public void activating() throws XulException {
  }

  @Override
  @Bindable
  public String getName() {
    return MessageHandler.getString( "datasourceDialog.SelectDatabaseType" );
  }

  @Override
  public List<IWizardStep> getSteps() {
    return Collections.emptyList();
  }

  @Override
  public void onFinish( XulServiceCallback<IDatasourceSummary> callback ) {
  }

  @Override
  public void init( XulDomContainer container, IWizardModel wizardModel ) throws XulException {
    container.addEventHandler( datasourceStep );
    datasourceStep.init( wizardModel );
  }

  @Override
  public void deactivating() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getId() {
    return "dummy";
  }

  @Override
  @Bindable
  public boolean isFinishable() {
    return false;
  }

  @Override
  @Bindable
  public void setFinishable( boolean isFinishable ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void restoreSavedDatasource( Domain previousDomain, XulServiceCallback<Void> callback ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public SelectDatasourceStep getSelectDatasourceStep() {
    return this.datasourceStep;
  }

  @Override
  public void reset() {
  }
}
