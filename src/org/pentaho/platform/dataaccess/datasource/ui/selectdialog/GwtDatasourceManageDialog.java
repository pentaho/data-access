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

package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;

import com.google.gwt.core.client.GWT;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: Sep 28, 2010 Time: 1:46:58 PM To change this template use File |
 * Settings | File Templates.
 */
public class GwtDatasourceManageDialog extends GwtDatasourceSelectionDialog {
  public GwtDatasourceManageDialog( final IXulAsyncDSWDatasourceService datasourceService,
                                    final EmbeddedWizard gwtDatasourceEditor,
                                    final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener ) {
    this.context = "manage";
    this.gwtDatasourceEditor = gwtDatasourceEditor;
    this.datasourceService = datasourceService;
    this.constructorListener = constructorListener;
    try {
      AsyncXulLoader.loadXulFromUrl( GWT.getModuleBaseURL() + "datasourceManageDialog.xul",
        GWT.getModuleBaseURL() + "datasourceSelectionDialog", this ); //$NON-NLS-1$//$NON-NLS-2$
    } catch ( Exception e ) {
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
