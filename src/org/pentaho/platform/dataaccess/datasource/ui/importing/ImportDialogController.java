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
import java.util.HashMap;
import java.util.Map;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Window;

public class ImportDialogController extends AbstractXulEventHandler {

	private XulFileUpload genericFileUpload;
//	private XulFileUpload concreteFileUpload;
	private XulDeck importDeck;
	private Map<Integer, IImportPerspective> importPerspectives;
	private IImportPerspective activeImportPerspective;

	public ImportDialogController() {
		importPerspectives = new HashMap<Integer, IImportPerspective>();
	}

	public void init() {
		importDeck = (XulDeck) document.getElementById("importDeck");

		genericFileUpload = (XulFileUpload) document.getElementById("genericFileUpload");
		genericFileUpload.addPropertyChangeListener(new FileUploadPropertyChangeListener());

//		concreteFileUpload = (XulFileUpload) document.getElementById("concreteFileUpload");
//		concreteFileUpload.addPropertyChangeListener(new FileUploadPropertyChangeListener());
	}

	public void addImportPerspective(int index, IImportPerspective importPerspective) {
		importPerspectives.put(index, importPerspective);
	}

	public String getName() {
		return "importDialogController";
	}

	public void show(int index) {
		reset();
		importDeck.setSelectedIndex(index);
		activeImportPerspective = importPerspectives.get(index);
		activeImportPerspective.showDialog();
	}

	private void reset() {
		genericFileUpload.setSelectedFile("");
//		concreteFileUpload.setSelectedFile("");
	}

	@Bindable
	public void genericUploadSuccess(String uploadedFile) {
		try {
			activeImportPerspective.genericUploadCallback(uploadedFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Bindable
	public void concreteUploadSuccess(String uploadedFile) {
//		try {
//			String selectedFile = concreteFileUpload.getSeletedFile();
//			activeImportPerspective.concreteUploadCallback(selectedFile, uploadedFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Bindable
	public void uploadFailure(Throwable error) {
		error.printStackTrace();
		Window.alert(error.getMessage());
	}

	@Bindable
	public void closeDialog() {
		activeImportPerspective.onDialogCancel();
	}

	@Bindable
	public void acceptDialog() {
		if (activeImportPerspective.isValid()) {
			activeImportPerspective.onDialogAccept();
			closeDialog();
		}
	}

	class FileUploadPropertyChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
			XulFileUpload uploadControl = (XulFileUpload) propertyChangeEvent.getSource();
			String value = uploadControl.getSeletedFile();
			if (!StringUtils.isEmpty(value)) {
				uploadControl.addParameter("file_name", uploadControl.getSeletedFile());
				uploadControl.addParameter("mark_temporary", "true");
				uploadControl.addParameter("unzip", "true");
				uploadControl.submit();
			}
		}
	}
}
