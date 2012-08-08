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

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.GWT;

public class GwtImportDialog implements IXulLoaderCallback {

	private MetadataImportDialogController metadataImportDialogController;
	private AnalysisImportDialogController analysisImportDialogController;
	private AsyncConstructorListener<GwtImportDialog> constructorListener;
	private ImportDialogController importDialogController;

	public GwtImportDialog(AsyncConstructorListener<GwtImportDialog> constructorListener) {
		this.constructorListener = constructorListener;
		try {
			AsyncXulLoader.loadXulFromUrl(GWT.getModuleBaseURL() + "importDialog.xul", GWT.getModuleBaseURL() + "importDialog", this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void xulLoaded(GwtXulRunner runner) {
		try {
			GwtXulDomContainer container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);

			BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());

			metadataImportDialogController = new MetadataImportDialogController();
			metadataImportDialogController.setBindingFactory(bf);
			container.addEventHandler(metadataImportDialogController);

			analysisImportDialogController = new AnalysisImportDialogController();
			analysisImportDialogController.setBindingFactory(bf);
			container.addEventHandler(analysisImportDialogController);

			importDialogController = new ImportDialogController();
			importDialogController.addImportPerspective(0, metadataImportDialogController);
			importDialogController.addImportPerspective(1, analysisImportDialogController);
			container.addEventHandler(importDialogController);

			runner.initialize();
			runner.start();

			importDialogController.init();
			metadataImportDialogController.init();
			analysisImportDialogController.init();

			if (constructorListener != null) {
				constructorListener.asyncConstructorDone(this);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showMetadataImportDialog(DialogListener<MetadataImportDialogModel> listener) {
		metadataImportDialogController.addDialogListener(listener);
		importDialogController.show(0);
	}

	public void showAnalysisImportDialog(DialogListener<AnalysisImportDialogModel> listener) {
		analysisImportDialogController.addDialogListener(listener);
		importDialogController.show(1);
	}

  public MetadataImportDialogController getMetadataImportDialogController() {
    return metadataImportDialogController;
  }

  public AnalysisImportDialogController getAnalysisImportDialogController() {
    return analysisImportDialogController;
  }

	public void overlayLoaded() {
	}

	public void overlayRemoved() {
	}
}
