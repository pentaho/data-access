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
 * Created December 08, 2011
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.ui.importing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;
import org.pentaho.ui.xul.util.XulDialogCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

@SuppressWarnings("all")
public class AnalysisImportDialogController extends AbstractXulDialogController<AnalysisImportDialogModel> implements
    IImportPerspective {

  private static final String MONDRIAN_POSTANALYSIS_URL = "plugin/data-access/api/mondrian/postAnalysis";

  private static Logger logger = Logger.getLogger(AnalysisImportDialogController.class.getName());

  private BindingFactory bf;

  private XulMenuList connectionList;

  private XulTree analysisParametersTree;

  private XulDialog importDialog;

  private XulDialog analysisParametersDialog;

  private ResourceBundle resBundle;

  private AnalysisImportDialogModel importDialogModel;

  private IXulAsyncConnectionService connectionService;

  private XulTextbox paramNameTextBox;

  private XulTextbox paramValueTextBox;

  private XulDeck analysisPreferencesDeck;

  private XulRadio availableRadio;

  private XulRadio manualRadio;

  private XulButton acceptButton;

  private XulButton parametersAcceptButton;

  private XulLabel fileLabel;

  private FileUpload analysisUpload;

  private XulLabel schemaNameLabel;

  private String importURL;

  private boolean overwrite = false;

  private static final Integer PARAMETER_MODE = 1;

  private static final Integer DATASOURCE_MODE = 0;

  protected static final CharSequence SUCCESS = "3";//to do - chnage to Integer

  protected static final int PUBLISH_SCHEMA_EXISTS_ERROR = 8;

  private static SubmitCompleteHandler submitHandler = null;

  // GWT controls
  private FormPanel formPanel;

  private FlowPanel hiddenFormSubmitPanel;

  public void init() {
    try {
      resBundle = (ResourceBundle) super.getXulDomContainer().getResourceBundles().get(0);
      connectionService = new ConnectionServiceGwtImpl();
      importDialogModel = new AnalysisImportDialogModel();
      connectionList = (XulMenuList) document.getElementById("connectionList");
      analysisParametersTree = (XulTree) document.getElementById("analysisParametersTree");
      importDialog = (XulDialog) document.getElementById("importDialog");
      analysisParametersDialog = (XulDialog) document.getElementById("analysisParametersDialog");
      paramNameTextBox = (XulTextbox) document.getElementById("paramNameTextBox");
      paramNameTextBox.addPropertyChangeListener(new ParametersChangeListener());
      paramValueTextBox = (XulTextbox) document.getElementById("paramValueTextBox");
      paramValueTextBox.addPropertyChangeListener(new ParametersChangeListener());
      analysisPreferencesDeck = (XulDeck) document.getElementById("analysisPreferencesDeck");
      availableRadio = (XulRadio) document.getElementById("availableRadio");
      manualRadio = (XulRadio) document.getElementById("manualRadio");
      fileLabel = (XulLabel) document.getElementById("fileLabel");
      schemaNameLabel = (XulLabel) document.getElementById("schemaNameLabel");

      acceptButton = (XulButton) document.getElementById("importDialog_accept");
      acceptButton.setDisabled(true);

      parametersAcceptButton = (XulButton) document.getElementById("analysisParametersDialog_accept");
      parametersAcceptButton.setDisabled(true);

      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(connectionList, "selectedItem", importDialogModel, "connection");
      bf.createBinding(manualRadio, "checked", this, "preference", new PreferencesBindingConvertor());

      Binding connectionListBinding = bf.createBinding(importDialogModel, "connectionList", connectionList, "elements");
      Binding analysisParametersBinding = bf.createBinding(importDialogModel, "analysisParameters",
          analysisParametersTree, "elements");

      createWorkingForm();
      addSubmitHandler();
  
      connectionListBinding.fireSourceChanged();
      analysisParametersBinding.fireSourceChanged();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createWorkingForm() {
    formPanel = new FormPanel();
    formPanel.setMethod(FormPanel.METHOD_POST);
    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);

    formPanel.setVisible(false);

    analysisUpload = new FileUpload();
    analysisUpload.setName("uploadAnalysis");
    analysisUpload.setVisible(true);   
    analysisUpload.setStyleName("gwt-FileUpload");
    analysisUpload.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        setSelectedFile(analysisUpload.getFilename());
        acceptButton.setDisabled(!isValid());
      }
    });

    // Create a hidden panel so we can pass data source parameters
    // as part of the form submit
    hiddenFormSubmitPanel = new FlowPanel();
    hiddenFormSubmitPanel.add(analysisUpload);

    formPanel.add(hiddenFormSubmitPanel);

    RootPanel.get().add(formPanel);
  }

  /**
   * Called by importDialog XUL file.  When user selects a schema file from File Browser
   * then this is the callback to set the file.  We need to call a native method to
   * simulate a click on the file browser control.
   */
  @Bindable
  public void setAnalysisFile() {
    jsClickUpload(analysisUpload.getElement());
  }

  private native void jsClickUpload(Element uploadElement) /*-{
                                                           uploadElement.click();
                                                           }-*/;

  @Bindable
  public void setSelectedFile(String name) {
    schemaNameLabel.setValue(name);
    importDialogModel.setUploadedFile(name);

    firePropertyChange("selectedFile", null, name); //$NON-NLS-1$
  }

  public XulDialog getDialog() {
    return importDialog;
  }

  public AnalysisImportDialogModel getDialogResult() {
    return importDialogModel;
  }

  private void reset() {
    reloadConnections();
    importDialogModel.removeAllParameters();
    importDialogModel.setUploadedFile(null);
    availableRadio.setSelected(true);
    acceptButton.setDisabled(true);
    schemaNameLabel.setValue("");
    setPreference(DATASOURCE_MODE);
    overwrite = false;
    removeHiddenPanels();
  }

  private void removeHiddenPanels() {
    // Remove all previous hidden form parameters otherwise parameters
    // from a previous import would get included in current form submit
    for (int i = 0; i < hiddenFormSubmitPanel.getWidgetCount(); i++) {
      if (hiddenFormSubmitPanel.getWidget(i).getClass().equals(Hidden.class)) {
        hiddenFormSubmitPanel.remove(hiddenFormSubmitPanel.getWidget(i));
      }
    }
  }

  private void reloadConnections() {
    if (connectionService != null) {
      connectionService.getConnections(new XulServiceCallback<List<Connection>>() {
        public void error(String message, Throwable error) {
          error.printStackTrace();
          Window.alert(message);
        }

        public void success(List<Connection> connections) {
          importDialogModel.setConnectionList(connections);
        }
      });
    }
  }

  public boolean isValid() {
    return importDialogModel.isValid();
  }

  /**
   * Called when the accept button is clicked.
   */
  @Bindable
  public void onDialogAccept() {
    removeHiddenPanels();
    buildAndSetParameters();
    formPanel.setAction(MONDRIAN_POSTANALYSIS_URL);
    // Add an event handlers to the formPanel.    
    //make sure this does not get registered twice for each accept       
    formPanel.submit();
  }

  /**
   * Initialize this in the form init() 
   * return values are numeric -
   */

  private void addSubmitHandler() {

    if (submitHandler == null) {
      formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
        @Override
        public void onSubmit(SubmitEvent event) {

        }
      });
      submitHandler = new FormPanel.SubmitCompleteHandler() {

        @Override
        public void onSubmitComplete(SubmitCompleteEvent event) {
          if (event.getResults().contains("SUCCESS") || event.getResults().contains("3")) {
            showMessagebox("SUCCESS", "Mondrian Analysis File " + schemaNameLabel.getValue() + " has been uploaded");
            refreshParentDialog();
          } else {
            String message = event.getResults();
            //message = message.substring(4, message.length() - 6);
            if (message != null && !"".equals(message) && message.length() == 1) {
              int code = new Integer(message).intValue();
              if (code == PUBLISH_SCHEMA_EXISTS_ERROR && !overwrite) {//Existing FIle Dialog
                overwriteFileDialog();
              } else {
                showMessagebox("ERROR", convertToNLSMessage(event.getResults(),schemaNameLabel.getValue()));
              }
            } else {
              showMessagebox("Server Error", convertToNLSMessage(event.getResults(), schemaNameLabel.getValue()));
            }
          }
        }

      };
      formPanel.addSubmitCompleteHandler(submitHandler);
    }
  }

  private void refreshParentDialog() {
    //send a message to the dialog parent to refresh the datasources
  

  }

  /**
   * Convert to $NLS$
   * @param results
   * @return msg
   *    int PUBLISH_TO_SERVER_FAILED = 1;
        int PUBLISH_GENERAL_ERROR = 2;
        int PUBLISH_DATASOURCE_ERROR = 6;
        int PUBLISH_USERNAME_PASSWORD_FAIL = 5;
        int PUBLISH_XMLA_CATALOG_EXISTS = 7;
        int PUBLISH_SCHEMA_EXISTS_ERROR = 8;
   */
  private String convertToNLSMessage(String results,String fileName) {
    String msg = results;
    int code = new Integer(results).intValue();
    switch (code) {
      case 1:
        msg = "Publish to server failed";
      case 2:
        msg = "Publish to server general error";
      case 5:
        msg = "Username/Password failed";
      case 6:
        msg = "Publish to datasource exists";
      case 7:
        msg = "XMLA Catalog Exists";
      case 8:
        msg ="Existing Schema File";
      default:
        msg = "General Error ["+results+"]";
        break;
    }
    return msg + " Mondrian File: "+fileName;
  }

  private void buildAndSetParameters() {
    // If user selects available data source, then pass the datasource as part of the parameters.
    // If user selects manual data source, pass in whatever parameters they specify even if it is empty.
    String parameters = importDialogModel.getParameters();
    if (availableRadio.isSelected()) {
      parameters = "Datasource=" + connectionList.getValue();
      parameters += ";overwrite=" + String.valueOf(overwrite);
    }

    // Parameters would contain either the data source from connectionList drop-down
    // or the parameters manually entered (even if list is empty)
    Hidden queryParameters = new Hidden("parameters", parameters);
    hiddenFormSubmitPanel.add(queryParameters);
  }

  // TODO - this method should be removed after it is removed by MetadataImportDialogController
  public void concreteUploadCallback(String fileName, String uploadedFile) {
    acceptButton.setDisabled(!isValid());
  }

  // TODO - this method should be removed after it is removed by MetadataImportDialogController
  public void genericUploadCallback(String uploadedFile) {
    importDialogModel.setUploadedFile(uploadedFile);
    acceptButton.setDisabled(!isValid());
  }

  @Bindable
  public void setPreference(Integer preference) {
    analysisPreferencesDeck.setSelectedIndex(preference);
    importDialogModel.setParameterMode(preference == PARAMETER_MODE);
    acceptButton.setDisabled(!isValid());
  }

  @Bindable
  public void removeParameter() {
    int[] selectedRows = analysisParametersTree.getSelectedRows();
    if (selectedRows.length == 1) {
      importDialogModel.removeParameter(selectedRows[0]);
      acceptButton.setDisabled(!isValid());
    }
  }

  @Bindable
  public void addParameter() {
    String paramName = paramNameTextBox.getValue();
    String paramValue = paramValueTextBox.getValue();
    if (!StringUtils.isEmpty(paramName) && !StringUtils.isEmpty(paramValue)) {
      importDialogModel.addParameter(paramName, paramValue);
      closeParametersDialog();
      acceptButton.setDisabled(!isValid());
    }
  }

  @Bindable
  public void closeParametersDialog() {
    paramNameTextBox.setValue("");
    paramValueTextBox.setValue("");
    importDialogModel.setSelectedAnalysisParameter(-1);
    analysisParametersTree.clearSelection();
    analysisParametersDialog.hide();
  }

  @Bindable
  public void editParameter() {
    int[] selectedRows = analysisParametersTree.getSelectedRows();
    if (selectedRows.length == 1) {
      importDialogModel.setSelectedAnalysisParameter(selectedRows[0]);
      ParameterDialogModel parameter = importDialogModel.getSelectedAnalysisParameter();
      paramNameTextBox.setValue(parameter.getName());
      paramValueTextBox.setValue(parameter.getValue());
      analysisParametersDialog.show();
    }
  }

  @Bindable
  public void overwriteFileDialog() {
    //Experiment
    XulConfirmBox confirm = null;
    try {
      confirm = (XulConfirmBox) document.createElement("confirmbox");
    } catch (XulException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    confirm.setTitle("Confirmation");
    confirm.setMessage("Existing Analysis file.  Overwrite Analysis file in repository?");
    confirm.setAcceptLabel("Ok");
    confirm.setCancelLabel("Cancel");
    confirm.addDialogCallback(new XulDialogCallback<String>() {
      public void onClose(XulComponent component, Status status, String value) {
        if (status == XulDialogCallback.Status.ACCEPT) {
          overwrite = true;
          removeHiddenPanels();
          buildAndSetParameters();
          formPanel.submit();
        }
      }

      public void onError(XulComponent component, Throwable err) {
        return;
      }
    });
    confirm.open();
  }

  @Bindable
  public void openParametersDialog() {
    analysisParametersDialog.show();
  }

  public void showDialog() {
    reset();
    importDialog.setTitle(resBundle.getString("importDialog.IMPORT_MONDRIAN", "Import Analysis"));
    fileLabel.setValue(resBundle.getString("importDialog.MONDRIAN_FILE", "Mondrian File") + ":");
    super.showDialog();
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
  }

  public String getName() {
    return "analysisImportDialogController";
  }

  class PreferencesBindingConvertor extends BindingConvertor<Boolean, Integer> {

    public Integer sourceToTarget(Boolean value) {
      int result = 0;
      if (value) {
        result = 1;
      }
      return result;
    }

    public Boolean targetToSource(Integer value) {
      return true;
    }
  }

  class ParametersChangeListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      boolean isDisabled = StringUtils.isEmpty(paramNameTextBox.getValue())
          || StringUtils.isEmpty(paramValueTextBox.getValue());
      parametersAcceptButton.setDisabled(isDisabled);
    }
  }

  /**
   * Shows a informational dialog.
   * 
   * @param title
   *          title of dialog
   * @param message
   *          message within dialog
   */
  private void showMessagebox(final String title, final String message) {
    try {
      XulMessageBox messagebox = (XulMessageBox) document.createElement("messagebox");//$NON-NLS-1$

      messagebox.setTitle(title);
      messagebox.setMessage(message);
      int option = messagebox.open();
    } catch (XulException e) {
      Window.alert(e.getMessage());
    }

  }
}