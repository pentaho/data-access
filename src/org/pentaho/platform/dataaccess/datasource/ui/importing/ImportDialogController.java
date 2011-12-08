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

public class ImportDialogController extends AbstractXulEventHandler {

	private XulDialog importDialog;
	private XulDialog fileUploadDialog;
	private XulTextbox uploadedFile;
	private XulFileUpload fileUpload;
	private XulDeck importDeck;
	private Map<Integer, IImportPerspective> importPerspectives;
	private IImportPerspective activeImportPerspective;

	public ImportDialogController() {
		importPerspectives = new HashMap<Integer, IImportPerspective>();
	}

	public void init() {
		fileUploadDialog = (XulDialog) document.getElementById("fileUploadDialog"); //$NON-NLS-1$
		importDeck = (XulDeck) document.getElementById("importDeck"); //$NON-NLS-1$
		importDialog = (XulDialog) document.getElementById("importDialog"); //$NON-NLS-1$
		fileUpload = (XulFileUpload) document.getElementById("fileUpload"); //$NON-NLS-1$
		uploadedFile = (XulTextbox) document.getElementById("uploadedFile");
	}

	public void addImportPerspective(int index, IImportPerspective importPerspective) {
		importPerspectives.put(index, importPerspective);
	}

	@Bindable
	public void showFileUploadDialog() {
		fileUploadDialog.show();
	}

	@Bindable
	public void uploadSuccess() {
	}

	@Bindable
	public void uploadFailure() {
	}

	@Bindable
	public void uploadFile() {
		try {
			String selectedFile = fileUpload.getSeletedFile();
			uploadedFile.setValue(selectedFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeUpload();
	}

	@Bindable
	public void closeUpload() {
		fileUploadDialog.hide();
	}

	public String getName() {
		return "importDialogController";
	}

	private void reset() {
		uploadedFile.setValue("");
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
		activeImportPerspective = importPerspectives.get(index);
		importDeck.setSelectedIndex(index);
		importDialog.show();
	}
}
