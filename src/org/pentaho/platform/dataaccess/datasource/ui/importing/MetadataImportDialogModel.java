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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MetadataImportDialogModel extends XulEventSourceAdapter {

	private List<LocalizedBundleDialogModel> localizedBundles;
	private String uploadedFile;
	private String domainId;

	public MetadataImportDialogModel() {
		localizedBundles = new ArrayList<LocalizedBundleDialogModel>();
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public void addLocalizedBundle(String fileName, String uploadedFile) {
		localizedBundles.add(new LocalizedBundleDialogModel(fileName, uploadedFile));
		this.firePropertyChange("localizedBundles", null, localizedBundles);
	}

	public void removeLocalizedBundle(int paramIndex) {
		localizedBundles.remove(paramIndex);
		this.firePropertyChange("localizedBundles", null, localizedBundles);
	}

	public void removeAllLocalizedBundles() {
		localizedBundles.clear();
		this.firePropertyChange("localizedBundles", null, localizedBundles);
	}

	public String getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(String uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	@Bindable
	public List<LocalizedBundleDialogModel> getLocalizedBundles() {
		return localizedBundles;
	}

	@Bindable
	public void setLocalizedBundles(List<LocalizedBundleDialogModel> value) {
		List<LocalizedBundleDialogModel> previousValue = localizedBundles;
		this.localizedBundles = value;
		this.firePropertyChange("localizedBundles", previousValue, value);
	}
	
	public String getLocalizedBundleEntries() {
		String result = "";
		for (LocalizedBundleDialogModel currentParameter : localizedBundles) {
			result = result + currentParameter.getFileName() + "=" + currentParameter.getUploadedFile() + ";";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
}
