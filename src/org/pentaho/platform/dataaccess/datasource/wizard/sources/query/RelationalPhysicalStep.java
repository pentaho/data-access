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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.sources.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtWaitingDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardRelationalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.AbstractWizardStep;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;

@SuppressWarnings("unchecked")
public class RelationalPhysicalStep extends AbstractWizardStep {

  public static final int DEFAULT_RELATIONAL_TABLE_ROW_COUNT = 5;
  private DatasourceMessages datasourceMessages;
  private IXulAsyncDatasourceService datasourceService;
  XulTextbox datasourceNameTextBox = null;
  XulButton okButton = null;

  XulButton cancelButton = null;

  private XulTree csvDataTable = null;

  private EmbeddedWizard outerController;
  
  private WizardConnectionController connectionController = null;

  private IXulAsyncConnectionService connectionService;

  public RelationalPhysicalStep(IXulAsyncDatasourceService datasourceService, IXulAsyncConnectionService connectionService, DatasourceMessages messages, EmbeddedWizard embeddedWizard) {
    this.datasourceService = datasourceService;
    this.connectionService = connectionService;
    outerController = embeddedWizard;
    datasourceMessages = messages;

//    csvDatasourceService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
//    ServiceDefTarget endpoint = (ServiceDefTarget) csvDatasourceService;
//    endpoint.setServiceEntryPoint(PhysicalDatasourceController.getDatasourceURL());
  }

  @Override
  public void activating() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public XulComponent getUIComponent() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Bindable
  public void init(final XulDomContainer mainWizardContainer) throws XulException {
    super.init(mainWizardContainer);
    datasourceNameTextBox = (XulTextbox) getDocument().getElementById("datasourceName"); //$NON-NLS-1$
    
    GwtWaitingDialog waitingDialog = new GwtWaitingDialog(datasourceMessages.getString("waitingDialog.previewLoading"),datasourceMessages.getString("waitingDialog.generatingPreview"));
    
    connectionController = new WizardConnectionController(mainWizardContainer.getDocumentRoot());
    connectionController.setDatasourceModel(getDatasourceModel());
    mainWizardContainer.addEventHandler(connectionController);

    WizardRelationalDatasourceController relationalDatasourceController = new WizardRelationalDatasourceController();
    relationalDatasourceController.setXulDomContainer(mainWizardContainer);
    relationalDatasourceController.setBindingFactory(getBindingFactory());
    relationalDatasourceController.setDatasourceMessages(datasourceMessages);
    relationalDatasourceController.setWaitingDialog(waitingDialog);
    relationalDatasourceController.setDatasourceModel(getDatasourceModel());
    relationalDatasourceController.setService(datasourceService);
    relationalDatasourceController.init();
    
    mainWizardContainer.addEventHandler(relationalDatasourceController);

    connectionController.setDatasourceMessages(datasourceMessages);
    relationalDatasourceController.setDatasourceModel(getDatasourceModel());
    connectionController.setDatasourceModel(getDatasourceModel());

    initialize();
  }

  public void initialize() {
    getDatasourceModel().clearModel();
  }

  @Bindable
  public void setDatasourceModel(DatasourceModel model) {
    setDatasourceModel(model);
  }

  @Bindable
  public DatasourceModel getDatasourceModel() {
    return this.getDatasourceModel();
  }

  public IXulAsyncConnectionService getConnectionService() {
    return connectionService;
  }

  public void setConnectionService(IXulAsyncConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public String getName() {
    return "datasourceController"; //$NON-NLS-1$
  }

  @Bindable
  public void selectCsv() {
    csvDataTable.update();
    getDatasourceModel().setDatasourceType(DatasourceType.CSV);
  }

  
  @Bindable
  public void selectSql() {
    getDatasourceModel().setDatasourceType(DatasourceType.SQL);
  }

  public IXulAsyncDatasourceService getDatasourceService() {
    return datasourceService;
  }

  public void setDatasourceService(IXulAsyncDatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }
  
  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#getStepName()
   */
  public String getStepName() {
    return datasourceMessages.getString("wizardStepName.SOURCE"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#setBindings()
   */
  public void setBindings() {
    getBindingFactory().setBindingType(Binding.Type.BI_DIRECTIONAL);
    getBindingFactory().createBinding(getDatasourceModel(), "datasourceName", datasourceNameTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$

    // create a binding from the headerRows property of the CsvFileInfo to the first-row-is-header check box

    
    getDatasourceModel().addPropertyChangeListener("datasourceName", new QueryAndDatasourceNamePropertyChangeListener()); //$NON-NLS-1$
    getDatasourceModel().addPropertyChangeListener("query", new QueryAndDatasourceNamePropertyChangeListener()); //$NON-NLS-1$

    getBindingFactory().setBindingType(Binding.Type.ONE_WAY);

    getBindingFactory().setBindingType(Binding.Type.ONE_WAY);
    getBindingFactory().createBinding(getDatasourceModel(), "datasourceName", getDatasourceModel().getModelInfo(), "stageTableName");

    getBindingFactory().createBinding(getDatasourceModel().getGuiStateModel(), "editing", datasourceNameTextBox, "disabled");
    
  }

  public void setFocus() {
    datasourceNameTextBox.setFocus();
    setStepImageVisible(true);
  }
  
  private class QueryAndDatasourceNamePropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      String newValue = (String) evt.getNewValue();
      if(newValue == null || newValue.trim().length() == 0) {
        setFinishable(false);
      }
    }
  }
  
  
  public boolean stepDeactivatingForward(){
    return super.stepDeactivatingForward();
  }


}
