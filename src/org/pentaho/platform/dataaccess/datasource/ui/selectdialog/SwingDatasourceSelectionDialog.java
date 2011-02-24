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

import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDebugImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDebugImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swing.SwingXulLoader;
import org.pentaho.ui.xul.swing.SwingXulRunner;
import org.pentaho.ui.xul.util.DialogController;

/**
 * @author mlowery
 */
public class SwingDatasourceSelectionDialog implements DialogController<LogicalModelSummary> {

  private XulRunner runner;

  private DatasourceSelectionDialogController datasourceSelectionDialogController;

  public SwingDatasourceSelectionDialog(final IXulAsyncDatasourceService datasourceService,
      final EmbeddedWizard datasourceDialogController) throws XulException {
    XulDomContainer container = new SwingXulLoader()
        .loadXul("org/pentaho/platform/dataaccess/datasource/wizard/public/datasourceSelectionDialog.xul"); //$NON-NLS-1$

    runner = new SwingXulRunner();
    runner.addContainer(container);

    BindingFactory bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());

    datasourceSelectionDialogController = new DatasourceSelectionDialogController();
    datasourceSelectionDialogController.setBindingFactory(bf);

    container.addEventHandler(datasourceSelectionDialogController);
    datasourceSelectionDialogController.setDatasourceService(datasourceService);

    datasourceSelectionDialogController.setDatasourceDialogController(datasourceDialogController);

    datasourceSelectionDialogController.addDialogListener(new DialogListener<LogicalModelSummary>() {
      public void onDialogAccept(LogicalModelSummary logicalModelSummary) {
//        System.out.printf("OK (returned %s)\n", logicalModelSummary);
      }

      public void onDialogCancel() {
//        System.out.println("Cancel");
      }

      public void onDialogReady() {
//      System.out.println("Ready");
      }
    });
    runner.initialize();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<LogicalModelSummary> listener) {
    datasourceSelectionDialogController.addDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    datasourceSelectionDialogController.hideDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<LogicalModelSummary> listener) {
    datasourceSelectionDialogController.removeDialogListener(listener);
  }

  public void showDialog() {
    datasourceSelectionDialogController.showDialog();
  }

  /**
   * For debug/demo purposes only.
   */
  public static void main(String[] args) throws XulException {
    IXulAsyncConnectionService connectionService = new ConnectionServiceDebugImpl();
    IXulAsyncDatasourceService datasourceService = new DatasourceServiceDebugImpl();

    EmbeddedWizard editor = new EmbeddedWizard(datasourceService, connectionService, null, false);
    SwingDatasourceSelectionDialog selectDialog = new SwingDatasourceSelectionDialog(datasourceService, editor);
    selectDialog.showDialog();
  }

}
