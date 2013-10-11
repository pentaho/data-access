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

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeChildren;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class WizardRelationalDatasourceController extends AbstractXulEventHandler {
  public static final int MAX_SAMPLE_DATA_ROWS = 5;

  public static final int MAX_COL_SIZE = 13;

  public static final String EMPTY_STRING = ""; //$NON-NLS-1$

  public static final String COMMA = ","; //$NON-NLS-1$

  private XulDialog connectionDialog;

  private XulDialog applyQueryConfirmationDialog = null;


  private XulDialog previewResultsDialog = null;

  private IXulAsyncDSWDatasourceService service;

  private DatasourceModel datasourceModel;

  BindingFactory bf;

  XulTree previewResultsTable = null;

  XulTextbox datasourceName = null;

  XulListbox connections = null;

  XulTextbox query = null;

  XulTreeCols previewResultsTreeCols = null;

  XulTextbox previewLimit = null;

  XulButton editConnectionButton = null;

  XulButton removeConnectionButton = null;

  XulButton editQueryButton = null;

  XulButton previewButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  //private XulTreeCol columnFormatTreeCol = null;\
  XulTree sampleDataTree = null;

  XulDialog aggregationEditorDialog = null;

  XulDialog sampleDataDialog = null;

  private String connectionNamesListProp = "connectionNames"; //$NON-NLS-1$

  public WizardRelationalDatasourceController() {

  }

  @Bindable
  public void init(final DatasourceModel datasourceModel) {
    this.datasourceModel = datasourceModel;
    bf = new GwtBindingFactory(document);
    sampleDataTree = (XulTree) document.getElementById("relationalSampleDataTable"); //$NON-NLS-1$
    aggregationEditorDialog = (XulDialog) document.getElementById("relationalAggregationEditorDialog"); //$NON-NLS-1$
    sampleDataDialog = (XulDialog) document.getElementById("relationalSampleDataDialog"); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    applyQueryConfirmationDialog = (XulDialog) document.getElementById("applyQueryConfirmationDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("datasourceName"); //$NON-NLS-1$
    connections = (XulListbox) document.getElementById("connectionList"); //$NON-NLS-1$
    query = (XulTextbox) document.getElementById("query"); //$NON-NLS-1$
    connectionDialog = (XulDialog) document.getElementById("connectionDialog");//$NON-NLS-1$
    previewResultsDialog = (XulDialog) document.getElementById("previewResultsDialog");//$NON-NLS-1$
    previewResultsTable = (XulTree) document.getElementById("previewResultsTable"); //$NON-NLS-1$
    previewResultsTreeCols = (XulTreeCols) document.getElementById("previewResultsTreeCols"); //$NON-NLS-1$
    previewLimit = (XulTextbox) document.getElementById("previewLimit"); //$NON-NLS-1$
    editConnectionButton = (XulButton) document.getElementById("editConnection"); //$NON-NLS-1$
    removeConnectionButton = (XulButton) document.getElementById("removeConnection"); //$NON-NLS-1$
    editQueryButton = (XulButton) document.getElementById("editQuery"); //$NON-NLS-1$
    previewButton = (XulButton) document.getElementById("preview"); //$NON-NLS-1$

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(datasourceModel.getGuiStateModel(), "relationalPreviewValidated", previewButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$

    List<Binding> bindingsThatNeedInitialized = new ArrayList<Binding>();

    BindingConvertor<IDatabaseConnection, Boolean> buttonConvertor = new BindingConvertor<IDatabaseConnection, Boolean>() {

      @Override
      public Boolean sourceToTarget(IDatabaseConnection value) {
        return !(value == null);
      }

      @Override
      public IDatabaseConnection targetToSource(Boolean value) {
        return null;
      }

    };

    bf.setBindingType(Binding.Type.ONE_WAY);
    final Binding domainBinding = bf.createBinding(datasourceModel.getGuiStateModel(),
        "connections", this, "relationalConnections"); //$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding(this, connectionNamesListProp, connections, "elements"); //$NON-NLS-1$

    bf.createBinding(datasourceModel,
        "selectedRelationalConnection", editConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding(datasourceModel,
        "selectedRelationalConnection", removeConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(
        datasourceModel,
        "selectedRelationalConnection", connections, "selectedIndex", new BindingConvertor<IDatabaseConnection, Integer>() { //$NON-NLS-1$ //$NON-NLS-2$

          @Override
          public Integer sourceToTarget(IDatabaseConnection connection) {
            if (connection != null) {
              return datasourceModel.getGuiStateModel().getConnectionIndex(connection);
            }

            return -1;
          }

          @Override
          public IDatabaseConnection targetToSource(Integer value) {
            if (value >= 0) {
              return datasourceModel.getGuiStateModel().getConnections().get(value);
            }

            return null;
          }

        });
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel.getGuiStateModel(), "previewLimit", previewLimit, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "query", query, "value"); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }

    for (Binding b : bindingsThatNeedInitialized) {
      try {
        b.fireSourceChanged();

      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }

  }

  @Bindable
  public void setRelationalConnections(List<IDatabaseConnection> connections) {
    List<String> names = new ArrayList<String>();
    for (IDatabaseConnection conn : connections) {
      names.add(conn.getName());
    }

    firePropertyChange(connectionNamesListProp, null, names);
  }

  public String getName() {
    return "relationalDatasourceController"; //$NON-NLS-1$
  }

  @Bindable
  public void closeConnectionDialog() {
    connectionDialog.hide();
  }

  @Bindable
  public void closeApplyQueryConfirmationDialog() {
    applyQueryConfirmationDialog.hide();
  }

  @Bindable
  public void displayPreview() {

    showWaitingDialog(MessageHandler.getString("DatasourceController.GENERATE_PREVIEW_DATA"), //$NON-NLS-1$
        MessageHandler.getString("DatasourceController.WAIT")); //$NON-NLS-1$
    service.doPreview(datasourceModel.getSelectedRelationalConnection().getName(), datasourceModel.getQuery(),
        datasourceModel.getGuiStateModel().getPreviewLimit(), new XulServiceCallback<SerializedResultSet>() {

          public void error(String message, Throwable error) {
            hideWaitingDialog();
            displayErrorMessage(error);
          }

          public void success(SerializedResultSet rs) {
            try {
              List<List<String>> data = rs.getData();
              String[] columns = rs.getColumns();
              int columnCount = columns.length;

              //  Show the dialog.  We do this here so browsers such as IE can render 
              //  column sizes and provide column resizing 
              previewResultsDialog.show();

              previewResultsTable.suppressLayout(true);
              XulTreeChildren treeChildren = previewResultsTable.getRootChildren();
              if (treeChildren != null) {
                treeChildren.removeAll();
              }

              // Remove all the existing columns
              previewResultsTable.getColumns().getChildNodes().clear();

              // Recreate the colums
              XulTreeCols treeCols = previewResultsTable.getColumns();
              // Setting column data
              for (int i = 0; i < columnCount; i++) {
                try {
                  XulTreeCol treeCol = (XulTreeCol) document.createElement("treecol"); //$NON-NLS-1$
                  treeCol.setLabel(columns[i]);
                  treeCol.setWidth(100);
                  treeCols.addColumn(treeCol);
                } catch (XulException e) {

                }
              }
              // Create the tree children and setting the data
              try {
                for (int i = 0; i < data.size(); i++) {
                  XulTreeRow row = (XulTreeRow) document.createElement("treerow"); //$NON-NLS-1$
                  for (int j = 0; j < columnCount; j++) {
                    XulTreeCell cell = (XulTreeCell) document.createElement("treecell"); //$NON-NLS-1$
                    cell.setLabel(getCellData(data, i, j));
                    row.addCell(cell);
                  }

                  previewResultsTable.addTreeRow(row);
                }
                previewResultsTable.suppressLayout(false);
                previewResultsTable.update();
                hideWaitingDialog();
              } catch (XulException e) {
                // TODO: add logging
                hideWaitingDialog();
                System.out.println(e.getMessage());
                e.printStackTrace();
              }
            } catch (Exception e) {
              e.printStackTrace();
              hideWaitingDialog();
              displayErrorMessage(e);
            }
          }
        });
  }

  @Bindable
  public void closePreviewResultsDialog() {
    previewResultsDialog.hide();
  }

  public IXulAsyncDSWDatasourceService getService() {
    return service;
  }

  public void setService(IXulAsyncDSWDatasourceService service) {
    this.service = service;
  }

  @Bindable
  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  @Bindable
  public void showWaitingDialog(String title, String message) {
    MessageHandler.showBusyIndicator(title, message);
  }

  @Bindable
  public void hideWaitingDialog() {
    MessageHandler.hideBusyIndicator();
  }

  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th,
        MessageHandler.getString("DatasourceEditor.USER_ERROR_TITLE"))); //$NON-NLS-1$
    errorLabel.setValue(ExceptionParser.getErrorMessage(th,
        MessageHandler.getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED"))); //$NON-NLS-1$
    errorDialog.show();
  }

  public boolean supportsBusinessData(BusinessData businessData) {
    return (businessData.getDomain().getPhysicalModels().get(0) instanceof SqlPhysicalModel);
  }

  private String getCellData(List<List<String>> data, int rowNumber, int columnNumber) {
    String returnValue = null;
    int rowCount = 0;
    for (List<String> row : data) {
      if (rowCount == rowNumber) {
        returnValue = row.get(columnNumber);
      }
      rowCount++;
    }
    return returnValue;
  }

  public boolean finishing() {

    //    metaStep.updateDomain(datasourceModel.getRelationalModel()
    //          .getSelectedConnection().getName(), null, this.query.getValue());
    return true;
  }

}
