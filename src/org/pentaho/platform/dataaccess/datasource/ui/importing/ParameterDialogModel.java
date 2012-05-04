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

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ParameterDialogModel extends XulEventSourceAdapter {

	private String name;
	private String value;

	public ParameterDialogModel() {
	}

	public ParameterDialogModel(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Bindable
	public String getName() {
		return name;
	}

	@Bindable
	public void setName(String name) {
		this.name = name;
	}

	@Bindable
	public String getValue() {
		return value;
	}

	@Bindable
	public void setValue(String value) {
		this.value = value;
	}
}
