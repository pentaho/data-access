package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;

import com.google.gwt.core.client.GWT;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: Sep 28, 2010
 * Time: 1:46:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class GwtDatasourceManageDialog extends GwtDatasourceSelectionDialog {
  public GwtDatasourceManageDialog(final IXulAsyncDatasourceService datasourceService,
                                   final EmbeddedWizard gwtDatasourceEditor,
                                   final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener) {
    this.context = "manage";
    this.gwtDatasourceEditor = gwtDatasourceEditor;
    this.datasourceService = datasourceService;
    this.constructorListener = constructorListener;
    try {
      AsyncXulLoader.loadXulFromUrl(GWT.getModuleBaseURL() + "datasourceManageDialog.xul", GWT.getModuleBaseURL() + "datasourceSelectionDialog", this); //$NON-NLS-1$//$NON-NLS-2$
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    checkInitialized();
    datasourceSelectionDialogController.showDialog();
  }


}
