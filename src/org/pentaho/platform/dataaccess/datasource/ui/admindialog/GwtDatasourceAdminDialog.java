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
package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncGenericDatasourceServiceManager;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController;

import com.google.gwt.core.client.GWT;

public class GwtDatasourceAdminDialog implements IXulLoaderCallback, DialogController<IGenericDatasourceInfo> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  protected DatasourceAdminDialogController datasourceAdminDialogController;

  protected IXulAsyncGenericDatasourceServiceManager genericDatasourceServiceManager;

  protected AsyncConstructorListener<GwtDatasourceAdminDialog> constructorListener;

  private boolean initialized;

  // ~ Constructors ====================================================================================================

  public GwtDatasourceAdminDialog(final IXulAsyncGenericDatasourceServiceManager genericDatasourceServiceManager,
      final AsyncConstructorListener<GwtDatasourceAdminDialog> constructorListener) {
    this.genericDatasourceServiceManager = genericDatasourceServiceManager;
    this.constructorListener = constructorListener;
    try {
      AsyncXulLoader.loadXulFromUrl(GWT.getModuleBaseURL() + "datasourceAdminDialog.xul", GWT.getModuleBaseURL() + "datasourceAdminDialog", this); //$NON-NLS-1$//$NON-NLS-2$
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected GwtDatasourceAdminDialog() {
  }
  // ~ Methods =========================================================================================================

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayLoaded() {
  }
  
  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayRemoved() {
  }

  public void xulLoaded(final GwtXulRunner runner) {
    try {
      GwtXulDomContainer container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);

      BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());

      datasourceAdminDialogController = new DatasourceAdminDialogController();
      datasourceAdminDialogController.setBindingFactory(bf);
      datasourceAdminDialogController.setGenericDatasourceServiceManager(genericDatasourceServiceManager);
      container.addEventHandler(datasourceAdminDialogController);


      runner.initialize();

      runner.start();

      initialized = true;

      if (constructorListener != null) {
        constructorListener.asyncConstructorDone(this);
      }
      
      datasourceAdminDialogController.onDialogReady();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("You must wait until the constructor listener is notified."); //$NON-NLS-1$
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<IGenericDatasourceInfo> listener) {
    checkInitialized();
    datasourceAdminDialogController.addDialogListener(listener);
    datasourceAdminDialogController.onDialogReady();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    checkInitialized();
    datasourceAdminDialogController.hideDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<IGenericDatasourceInfo> listener) {
    checkInitialized();
    datasourceAdminDialogController.removeDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    checkInitialized();
    datasourceAdminDialogController.showDialog();
  }
}
