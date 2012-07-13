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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.user.client.Window;

@SuppressWarnings("all")
public class AnalysisImportDialogController extends AbstractXulDialogController<AnalysisImportDialogModel> implements IImportPerspective {
  private static Logger logger = Logger.getLogger("AnalysisImportDialogController");

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
  private FlowPanel hiddenSubmitPanel;

  private static final Integer PARAMETER_MODE = 1;
	private static final Integer DATASOURCE_MODE = 0;

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
			Binding analysisParametersBinding = bf.createBinding(importDialogModel, "analysisParameters", analysisParametersTree, "elements");

      createWorkingForm();

      String moduleBaseURL = GWT.getModuleBaseURL();
      String moduleName = GWT.getModuleName();
      String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
      importURL = contextURL + "/pentaho/plugin/data-access/api/mondrian/importSchema";

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
    analysisUpload.addChangeHandler(new ChangeHandler(){
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
    // If user selects available data source, then pass the datasource as part of the parameters.
    // If user selects manual data source, pass in whatever parameters they specify even if it is empty.
    String parameters = importDialogModel.getParameters();
    if (availableRadio.isSelected()) {
      parameters = connectionList.getValue();
    }

    // Parameters would contain either the data source from connectionList drop-down
    // or the parameters manually entered (even if list is empty)
    Hidden queryParameters = new Hidden("parameters", parameters);
    hiddenFormSubmitPanel.add(queryParameters);

    formPanel.setAction(importURL);
    formPanel.submit();
  }


  // TODO - remove!!!
	public void concreteUploadCallback(String fileName, String uploadedFile) {
    logger.info("************** concreteUploadCallback() - filename = " + fileName + ", uploadedFile = " + uploadedFile);
    acceptButton.setDisabled(!isValid());
	}

  // TODO - remove!!!
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
			boolean isDisabled = StringUtils.isEmpty(paramNameTextBox.getValue()) || StringUtils.isEmpty(paramValueTextBox.getValue());
			parametersAcceptButton.setDisabled(isDisabled);
		}
	}
}