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

import java.util.HashMap;
import java.util.Map;

import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Window;

public class ImportDialogController extends AbstractXulEventHandler {

	private XulDialog importDialog;
	private XulDialog fileUploadDialog;
	private XulTextbox uploadedFileTextbox;
	private XulFileUpload fileUpload;
	private XulDeck importDeck;
	private Map<Integer, IImportPerspective> importPerspectives;
	private IImportPerspective activeImportPerspective;
	private boolean isGenericUpload;

	public ImportDialogController() {
		isGenericUpload = true;
		importPerspectives = new HashMap<Integer, IImportPerspective>();
	}

	public void init() {
		fileUploadDialog = (XulDialog) document.getElementById("fileUploadDialog");
		importDeck = (XulDeck) document.getElementById("importDeck");
		importDialog = (XulDialog) document.getElementById("importDialog");
		fileUpload = (XulFileUpload) document.getElementById("fileUpload");
		uploadedFileTextbox = (XulTextbox) document.getElementById("uploadedFile");
	}

	public void addImportPerspective(int index, IImportPerspective importPerspective) {
		importPerspectives.put(index, importPerspective);
	}

	@Bindable
	public void showFileUploadDialog(boolean isGenericUpload) {
		fileUpload.setSelectedFile("");
		this.isGenericUpload = isGenericUpload;
		fileUploadDialog.show();
	}

	@Bindable
	public void uploadSuccess(String uploadedFile) {
		try {
			String selectedFile = fileUpload.getSeletedFile();
			if (isGenericUpload) {
				uploadedFileTextbox.setValue(selectedFile);
				activeImportPerspective.genericUploadCallback(uploadedFile);
			} else {
				activeImportPerspective.concreteUploadCallback(selectedFile, uploadedFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeUpload();
	}

	@Bindable
	public void uploadFailure(Throwable error) {
		Window.alert(error.getMessage());
	}

	@Bindable
	public void uploadFile() {
		fileUpload.addParameter("file_name", fileUpload.getSeletedFile());
		fileUpload.addParameter("mark_temporary", "true");
		fileUpload.addParameter("unzip", "true");
		fileUpload.submit();
	}

	@Bindable
	public void closeUpload() {
		fileUploadDialog.hide();
	}

	public String getName() {
		return "importDialogController";
	}

	private void reset() {
		fileUpload.setSelectedFile("");
		uploadedFileTextbox.setValue("");
		isGenericUpload = true;
	}

	@Bindable
	public void closeDialog() {
		importDialog.hide();
	}

	@Bindable
	public void acceptDialog() {
		activeImportPerspective.processImport();
		closeDialog();
	}

	public void show(int index) {
		reset();
		importDeck.setSelectedIndex(index);
		activeImportPerspective = importPerspectives.get(index);
		activeImportPerspective.show();
	}
}
