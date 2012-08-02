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
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MetadataImportDialogController extends AbstractXulDialogController<MetadataImportDialogModel> implements IImportPerspective {
  /**
   * 
   */
  private static final String METADATA_IMPORT_URL = "plugin/data-access/api/metadata/postimport";
  private static Integer FILE_UPLOAD_SUFFIX = 0;
  private BindingFactory bf;
  private XulButton acceptButton;
  private XulTree localizedBundlesTree;
  private XulTextbox domainIdText;
  private XulLabel metaFileLocation;
  private XulDialog importDialog;
  private ResourceBundle resBundle;
  private MetadataImportDialogModel importDialogModel;
  private XulLabel fileLabel;
  private FlowPanel mainFormPanel;
  private FlowPanel propertiesFileImportPanel;
  private XulVbox hiddenArea;
  
  // GWT controls
  private FormPanel formPanel;
  private FileUpload metadataFileUpload;
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
      metaFileLocation = (XulLabel) document.getElementById("uploadFileLabel");
      acceptButton = (XulButton) document.getElementById("importDialog_accept");
      hiddenArea = (XulVbox)document.getElementById("metadataImportCard");
      acceptButton.setDisabled(true);

      bf.setBindingType(Binding.Type.ONE_WAY);
      Binding localizedBundlesBinding = bf.createBinding(importDialogModel, "localizedBundles", localizedBundlesTree, "elements");
      localizedBundlesBinding.fireSourceChanged();      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void createWorkingForm() {
    String importURL = METADATA_IMPORT_URL;
    
    formPanel = new FormPanel();
    formPanel.setMethod(FormPanel.METHOD_POST);
    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
    formPanel.setAction(importURL);
    formPanel.getElement().getStyle().setProperty("position", "absolute");
    formPanel.getElement().getStyle().setProperty("visibility", "hidden");
    formPanel.getElement().getStyle().setProperty("overflow", "hidden");
    formPanel.getElement().getStyle().setProperty("clip", "rect(0px,0px,0px,0px)");
    mainFormPanel = new FlowPanel();
    propertiesFileImportPanel = new FlowPanel();
    mainFormPanel.add(propertiesFileImportPanel);
    formPanel.add(mainFormPanel);
    formDomainIdText = new TextBox();
    formDomainIdText.setName("domainId");
    mainFormPanel.add(formDomainIdText);
    metadataFileUpload = new FileUpload();
    metadataFileUpload.setName("metadataFile");
    metadataFileUpload.getElement().setId("metaFileUpload");
    metadataFileUpload.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        metaFileLocation.setValue(((FileUpload)event.getSource()).getFilename());
        importDialogModel.setUploadedFile(((FileUpload)event.getSource()).getFilename());
      }      
    });
    mainFormPanel.add(metadataFileUpload);
    VerticalPanel vp = (VerticalPanel)hiddenArea.getManagedObject();
    vp.add(formPanel);
  }

  public XulDialog getDialog() {
    return importDialog;
  }

  public MetadataImportDialogModel getDialogResult() {
    return importDialogModel;
  }
  
  public boolean isValid() {
    return importDialogModel.isValid();
  }

  @Bindable
  public void setMetadataFile() {
     jsClickUpload(metadataFileUpload.getElement().getId());
  }
  
  @Bindable
  public void removeLocalizedBundle() {
    int[] selectedRows = localizedBundlesTree.getSelectedRows();
    if (selectedRows.length == 1) {
      propertiesFileImportPanel.remove(selectedRows[0]);
      importDialogModel.removeLocalizedBundle(selectedRows[0]);
    }
  }
  
  @Bindable
  public void addLocalizedBundle() {
    final FileUpload localizedBundleUpload = new FileUpload();
    localizedBundleUpload.setName("localeFiles");
    localizedBundleUpload.getElement().setId("propertyFileUpload" + FILE_UPLOAD_SUFFIX++);
    localizedBundleUpload.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String fileName = ((FileUpload)event.getSource()).getFilename();
        if (fileName == null || fileName.length()<1) {  // Trying to detect a cancel
          propertiesFileImportPanel.remove(localizedBundleUpload);
        } else {
          importDialogModel.addLocalizedBundle(fileName, fileName);
        }
      }      
    });
    propertiesFileImportPanel.add(localizedBundleUpload);
    jsClickUpload(localizedBundleUpload.getElement().getId());
  }

  native void jsClickUpload(String uploadElement) /*-{
    $doc.getElementById(uploadElement).click();
  }-*/;
  
  private void reset() {
    metaFileLocation.setValue(resBundle.getString("importDialog.SELECT_METAFILE_LABEL", "Browse for metadata file"));
    importDialogModel.removeAllLocalizedBundles();
    VerticalPanel gwtHiddenArea = (VerticalPanel)hiddenArea.getManagedObject();
    if (formPanel != null && gwtHiddenArea.getWidgetIndex(formPanel) != -1) {
      gwtHiddenArea.remove(formPanel);
    }
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
    createWorkingForm();
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
  }

  public String getName() {
    return "metadataImportDialogController";
  }
  
  public FormPanel getFormPanel() {
    return formPanel;
  }
  
  class DomainIdChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      formDomainIdText.setText(evt.getNewValue().toString());
      importDialogModel.setDomainId(evt.getNewValue().toString());
      acceptButton.setDisabled(!isValid());
    }
  }

}
