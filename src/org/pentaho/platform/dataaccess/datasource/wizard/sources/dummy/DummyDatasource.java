/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

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
 * User: nbaker
 * Date: 3/23/11
 */
public class DummyDatasource extends AbstractXulEventHandler implements IWizardDatasource{
  SelectDatasourceStep datasourceStep;
  private IWizardModel wizardModel;

  public DummyDatasource(){
    datasourceStep = new SelectDatasourceStep(this);

  }

  @Override
  public void activating() throws XulException {
  }

  @Override
  @Bindable
  public String getName() {
    return MessageHandler.getString("datasourceDialog.SelectDatabaseType");
  }

  @Override
  public List<IWizardStep> getSteps() {
    return Collections.emptyList();
  }

  @Override
  public void onFinish(XulServiceCallback<IDatasourceSummary> callback) {
  }

  @Override
  public void init(XulDomContainer container, IWizardModel wizardModel) throws XulException {
    this.wizardModel = wizardModel;
    container.addEventHandler(datasourceStep);
    datasourceStep.init(wizardModel);
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
  public void setFinishable(boolean isFinishable) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void restoreSavedDatasource(Domain previousDomain, XulServiceCallback<Void> callback) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public SelectDatasourceStep getSelectDatasourceStep() {
    return this.datasourceStep;
  }

  @Override
  public void reset() {
  }
}
