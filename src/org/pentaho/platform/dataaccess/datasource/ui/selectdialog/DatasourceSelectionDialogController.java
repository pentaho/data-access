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
 * Created June 2, 2009
 * @author mlowery
 */
package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.Collections;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.modeler.ModelerDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

public class DatasourceSelectionDialogController extends AbstractXulDialogController<LogicalModelSummary> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private BindingFactory bf;

  private IXulAsyncDatasourceService datasourceService;

  private DatasourceSelectionDialogModel datasourceSelectionDialogModel = new DatasourceSelectionDialogModel();

  private XulDialog datasourceSelectionDialog;

  private XulDialog removeDatasourceConfirmationDialog;

  private boolean administrator;

  /**
   * The controller for the datasource dialog, which is shown when the user clicks the Add button in this dialog.
   */
  private EmbeddedWizard datasourceEditor;

  private XulButton addDatasourceButton;
  private XulButton editDatasourceButton;
  private XulButton removeDatasourceButton;

  private XulListbox datasourceListbox;

  private Binding editDatasourceButtonBinding; 
  private Binding removeDatasourceButtonBinding;
  private ModelerDialog modeler;


  // ~ Constructors ====================================================================================================

  public DatasourceSelectionDialogController() {

  }

  // ~ Methods =========================================================================================================

  /**
   * Sets up bindings.
   */
  @Bindable
  public void init() {
    internalInit();    
    datasourceService.hasPermission(new XulServiceCallback<Boolean>() {
      public void error(final String message, final Throwable error) {
        showMessagebox("Error", error.getLocalizedMessage()); //$NON-NLS-1$
      }

      public void success(final Boolean administrator) {
        // now we have admin status; update add datasource and remove datasource buttons' enabled status
        DatasourceSelectionDialogController.this.administrator = administrator;
        addDatasourceButton.setVisible(administrator);
        editDatasourceButton.setVisible(administrator);
        removeDatasourceButton.setVisible(administrator);
        try {
          removeDatasourceButtonBinding.fireSourceChanged();
          editDatasourceButtonBinding.fireSourceChanged();
        } catch (Exception e) {
          showMessagebox("Error", e.getLocalizedMessage()); //$NON-NLS-1$
        }
      }
    });
  }
  
  public void reset() {
	  if(datasourceListbox != null) { 
		  datasourceListbox.setSelectedIndex(0);
	  }
  }

  private void internalInit() {
    try {
      datasourceListbox = (XulListbox) safeGetElementById(document, "datasourceListbox"); //$NON-NLS-1$
      datasourceSelectionDialog = (XulDialog) safeGetElementById(document, "datasourceSelectionDialog"); //$NON-NLS-1$
      
      removeDatasourceConfirmationDialog = (XulDialog) safeGetElementById(document,
          "removeDatasourceConfirmationDialog"); //$NON-NLS-1$

      XulButton acceptButton = null;
      try {
         acceptButton = (XulButton) safeGetElementById(document, "datasourceSelectionDialog_accept"); //$NON-NLS-1$
      } catch (Exception e) {
        // this might not be available
      }
      addDatasourceButton = (XulButton) safeGetElementById(document, "addDatasource"); //$NON-NLS-1$
      editDatasourceButton = (XulButton) safeGetElementById(document, "editDatasource"); //$NON-NLS-1$
      removeDatasourceButton = (XulButton) safeGetElementById(document, "removeDatasource"); //$NON-NLS-1$

      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(DatasourceSelectionDialogController.this.datasourceSelectionDialogModel,
          "logicalModelSummaries", datasourceListbox, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(datasourceListbox, "selectedIndex", //$NON-NLS-1$
          DatasourceSelectionDialogController.this.datasourceSelectionDialogModel, "selectedIndex"); //$NON-NLS-1$

      // setup binding to disable accept button until user selects a datasource
      bf.setBindingType(Binding.Type.ONE_WAY);
      if (acceptButton != null) {
        BindingConvertor<Integer, Boolean> acceptButtonConvertor = new BindingConvertor<Integer, Boolean>() {
          @Override
          public Boolean sourceToTarget(final Integer value) {
            return value > -1;
          }

          @Override
          public Integer targetToSource(final Boolean value) {
            throw new UnsupportedOperationException();
          }
        };
        bf.createBinding(DatasourceSelectionDialogController.this.datasourceSelectionDialogModel, "selectedIndex", //$NON-NLS-1$
            acceptButton, "!disabled", acceptButtonConvertor); //$NON-NLS-1$
      }
      // setup binding to disable remove datasource button until user selects a datasource
      bf.setBindingType(Binding.Type.ONE_WAY);
      BindingConvertor<Integer, Boolean> removeDatasourceButtonConvertor = new BindingConvertor<Integer, Boolean>() {
        @Override
        public Boolean sourceToTarget(final Integer value) {
          return value > -1 && administrator;
        }

        @Override
        public Integer targetToSource(final Boolean value) {
          throw new UnsupportedOperationException();
        }
      };
      removeDatasourceButtonBinding = bf.createBinding(DatasourceSelectionDialogController.this.datasourceSelectionDialogModel, "selectedIndex", //$NON-NLS-1$
          removeDatasourceButton, "!disabled", removeDatasourceButtonConvertor); //$NON-NLS-1$
      BindingConvertor<Integer, Boolean> editDatasourceButtonConvertor = new BindingConvertor<Integer, Boolean>() {
        @Override
        public Boolean sourceToTarget(final Integer value) {
          return 
              value > -1 && administrator && 
              datasourceSelectionDialogModel.getLogicalModelSummaries().get(value).getModelId().equals("MODEL_1") //$NON-NLS-1$
          ;
        }

        @Override
        public Integer targetToSource(final Boolean value) {
          throw new UnsupportedOperationException();
        }
      };
      editDatasourceButtonBinding = bf.createBinding(DatasourceSelectionDialogController.this.datasourceSelectionDialogModel, "selectedIndex", //$NON-NLS-1$
          editDatasourceButton, "!disabled", editDatasourceButtonConvertor); //$NON-NLS-1$

      
      datasourceListbox.setSelectedIndex(-1);
      // workaround for bug in some XulListbox implementations (doesn't fire event on setSelectedIndex call)
      DatasourceSelectionDialogController.this.datasourceSelectionDialogModel.setSelectedIndex(-1);

    } catch (Exception e) {
      e.printStackTrace();
      showMessagebox("Error", e.getLocalizedMessage()); //$NON-NLS-1$
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
    XulMessageBox messagebox = null;
    try {
      messagebox = (XulMessageBox) document.createElement("messagebox"); //$NON-NLS-1$
    } catch (XulException e) {
      e.printStackTrace();
      return;
    }
    messagebox.setTitle(title);
    messagebox.setMessage(message);
    messagebox.open();
  }

  /**
   * A fail-quickly version of <code>getElementById()</code>.
   */
  private XulComponent safeGetElementById(final Document doc, final String id) {
    XulComponent elem = doc.getElementById(id);
    if (elem != null) {
      return elem;
    } else {
      throw new NullPointerException("element with id \"" + id + "\" is null"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private void refreshDatasources(final String domainId, final String modelId) {
    datasourceService.getLogicalModels(new XulServiceCallback<List<LogicalModelSummary>>() {

      public void error(final String message, final Throwable error) {
        System.out.println(message);
      }

      public void success(final List<LogicalModelSummary> logicalModelSummaries) {
        Collections.sort(logicalModelSummaries);
        datasourceSelectionDialogModel.setLogicalModelSummaries(logicalModelSummaries);
        if (domainId != null && modelId != null) {
          datasourceSelectionDialogModel.setSelectedLogicalModel(domainId, modelId);
          datasourceListbox.setSelectedIndex(datasourceSelectionDialogModel.getSelectedIndex());
        } else if (logicalModelSummaries.isEmpty()) {
          datasourceListbox.setSelectedIndex(-1);
          datasourceSelectionDialogModel.setSelectedIndex(-1);
        } else {
          datasourceListbox.setSelectedIndex(0);
          datasourceSelectionDialogModel.setSelectedIndex(0);
        }
      }

    });
    // test code......
    // List<IDatasource> datasourceList = new ArrayList<IDatasource>();
    // IDatasource ds = new Datasource();
    // ds.setDatasourceName("hello");
    // datasourceList.add(ds);
    // ds = new Datasource();
    // ds.setDatasourceName("goodbye");
    // datasourceList.add(ds);
    // DatasourceSelectionDialogController.this.datasourceSelectionDialogModel.setDatasources(datasourceList);
  }

  /**
   * ID of this controller. This is how event handlers are referenced in <code>.xul</code> files.
   */
  @Override
  public String getName() {
    return "datasourceSelectionDialogController"; //$NON-NLS-1$
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
  }

  public void setDatasourceService(final IXulAsyncDatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  /**
   * @return selected datasource or <code>null</code> if no selected datasource
   */
  @Override
  protected LogicalModelSummary getDialogResult() {
    int selectedIndex = datasourceSelectionDialogModel.getSelectedIndex();
    if (selectedIndex > -1) {
      return datasourceSelectionDialogModel.getLogicalModelSummaries().get(selectedIndex);
    } else {
      return null;
    }
  }

  @Override
  protected XulDialog getDialog() {
    return datasourceSelectionDialog;
  }

  public void setDatasourceDialogController(final EmbeddedWizard gwtDatasourceEditor) {
    this.datasourceEditor = gwtDatasourceEditor;
  }

  private void enableWaitCursor(final boolean enable) {
    if (enable) {
      DOM.setStyleAttribute(RootPanel.get().getElement(), "cursor", "wait"); 
    } else {
      DOM.setStyleAttribute(RootPanel.get().getElement(), "cursor", "default");  
    }
  }
  
  @Bindable
  public void addDatasource() {
    enableWaitCursor(true);
    datasourceEditor.addDialogListener(new DialogListener<Domain>() {
      public void onDialogAccept(final Domain domain) {
        refreshDatasources(domain.getId(), domain.getLogicalModels().get(0).getId());
      }

      public void onDialogCancel() {
      }

      public void onDialogReady() {
        enableWaitCursor(false);
      }
    });
    datasourceEditor.showDialog();
  }

  @Bindable
  public void editDatasource() {

    modeler = ModelerDialog.getInstance(new AsyncConstructorListener<ModelerDialog>(){
      public void asyncConstructorDone(ModelerDialog dialog) {
        enableWaitCursor(true);
        final DialogListener<Domain> listener = new DialogListener<Domain>(){
          public void onDialogCancel() {
          }
          public void onDialogAccept(final Domain domain) {
            refreshDatasources(domain.getId(), domain.getLogicalModels().get(0).getId());
          }
          public void onDialogReady() {
            enableWaitCursor(false);
          }
        };
        LogicalModelSummary logicalModelSummary = getDialogResult();
        dialog.addDialogListener(listener);
        dialog.showDialog(logicalModelSummary.getDomainId(), logicalModelSummary.getModelId());
      }
    });
    
  }

  @Bindable
  public void removeDatasourceConfirm() {
    removeDatasourceConfirmationDialog.show();
  }

  @Bindable
  public void removeDatasourceCancel() {
    removeDatasourceConfirmationDialog.hide();
  }

  @Bindable
  public void removeDatasourceAccept() {
    LogicalModelSummary logicalModelSummary = getDialogResult();
    datasourceService.deleteLogicalModel(logicalModelSummary.getDomainId(), logicalModelSummary.getModelId(), new XulServiceCallback<Boolean>() {
      public void error(String message, Throwable error) {
        showMessagebox("Error", error.getLocalizedMessage()); //$NON-NLS-1$
      }

      public void success(Boolean retVal) {
        refreshDatasources(null, null);
        removeDatasourceConfirmationDialog.hide();
      }
    });
  }

  @Override
  public void showDialog() {    
    super.showDialog();
    refreshDatasources(null, null);
  }
}
