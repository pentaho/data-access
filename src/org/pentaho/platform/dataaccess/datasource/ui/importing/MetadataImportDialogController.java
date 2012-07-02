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

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

//import com.google.gwt.dom.client.Element;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class MetadataImportDialogController extends AbstractXulDialogController<MetadataImportDialogModel> implements IImportPerspective {

	private BindingFactory bf;
	private XulButton acceptButton;
	private XulTree localizedBundlesTree;
	private XulTextbox domainIdText;
	private XulDialog importDialog;
	private ResourceBundle resBundle;
	private MetadataImportDialogModel importDialogModel;
	private XulLabel fileLabel;
	private FlowPanel mainFormPanel;
	
	// GWT controls
	private FormPanel formPanel;
	private TextBox formDomainIdText;
	
	public void init() {
		try {
			resBundle = (ResourceBundle) super.getXulDomContainer().getResourceBundles().get(0);
			importDialogModel = new MetadataImportDialogModel();
			localizedBundlesTree = (XulTree) document.getElementById("localizedBundlesTree");
			domainIdText = (XulTextbox) document.getElementById("domainIdText");
			domainIdText.addPropertyChangeListener(new DomainIdChangeListener());
			importDialog = (XulDialog) document.getElementById("importDialog");
			fileLabel = (XulLabel) document.getElementById("fileLabel");

			acceptButton = (XulButton) document.getElementById("importDialog_accept");
			acceptButton.setDisabled(true);

			bf.setBindingType(Binding.Type.ONE_WAY);
			Binding localizedBundlesBinding = bf.createBinding(importDialogModel, "localizedBundles", localizedBundlesTree, "elements");
			createWorkingForm();
			localizedBundlesBinding.fireSourceChanged();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createWorkingForm() {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String importURL = contextURL + "/pentaho/plugin/data-access/api/metadata/import";

	  formPanel = new FormPanel();
    formPanel.setMethod(FormPanel.METHOD_POST);
    formPanel.setAction(importURL);
    formPanel.setVisible(false);
    mainFormPanel = new FlowPanel();
    formPanel.add(mainFormPanel);
    formDomainIdText = new TextBox();
    formDomainIdText.setName("domainId");
    mainFormPanel.add(formDomainIdText);
    
    RootPanel.get().add(formPanel);
	}

	public XulDialog getDialog() {
		return importDialog;
	}

	public MetadataImportDialogModel getDialogResult() {
	  formPanel.submit();
		return importDialogModel;
	}
	
	public boolean isValid() {
		return importDialogModel.isValid();
	}

	@Bindable
	public void removeLocalizedBundle() {
		int[] selectedRows = localizedBundlesTree.getSelectedRows();
		if (selectedRows.length == 1) {
			importDialogModel.removeLocalizedBundle(selectedRows[0]);
		}
	}
	
	@Bindable
	public void addLocalizedBundle() {
	  FileUpload localizedBundleUpload = new FileUpload();
	  localizedBundleUpload.setName("localeFiles");
	  mainFormPanel.add(localizedBundleUpload);
	  jsClickUpload(localizedBundleUpload.getElement());
	}

	native void jsClickUpload(Element uploadElement) /*-{
	  uploadElement.click();
	}-*/;
	
	private void reset() {
		importDialogModel.removeAllLocalizedBundles();
		acceptButton.setDisabled(true);
		domainIdText.setValue("");
	}

	public void concreteUploadCallback(String fileName, String uploadedFile) {
		importDialogModel.addLocalizedBundle(fileName, uploadedFile);
	}

	public void genericUploadCallback(String uploadedFile) {
		importDialogModel.setUploadedFile(uploadedFile);
		acceptButton.setDisabled(!isValid());
	}

	public void showDialog() {
		reset();
		importDialog.setTitle(resBundle.getString("importDialog.IMPORT_METADATA", "Import Metadata"));
		fileLabel.setValue(resBundle.getString("importDialog.XMI_FILE", "XMI File") + ":");
		super.showDialog();
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	public String getName() {
		return "metadataImportDialogController";
	}
	
    class DomainIdChangeListener implements PropertyChangeListener {
		
		public void propertyChange(PropertyChangeEvent evt) {
	    formDomainIdText.setText(evt.getNewValue().toString());
			importDialogModel.setDomainId(evt.getNewValue().toString());
			acceptButton.setDisabled(!isValid());
		}
	}
}
