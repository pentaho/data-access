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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinTableModel;

public class JoinValidator {
	
	
	private JoinGuiModel joinGuiModel;
	private IWizardModel wizardModel;
	private StringBuffer errors;
	
	public JoinValidator(JoinGuiModel joinGuiModel, IWizardModel wizardModel) {
		this.joinGuiModel = joinGuiModel;
		this.wizardModel = wizardModel;
	}
	
	public boolean isValid(JoinModel join) {
		// validate against duplicate joins.
		// and
		// validate against joining to the same table.
		// and
		// validate against circular joins
		this.errors = new StringBuffer();
		return notDuplicate(join) && notSelfJoin(join) && notCircularJoins(join);
	}
	
	private boolean notDuplicate(JoinModel newJoin) {
		boolean notDuplicate = true;
		for (JoinModel join : this.joinGuiModel.getJoins()) {
			if (newJoin.equals(join)) {
				notDuplicate = false;
				errors.append("Duplicate Join Detected\n");  // TODO: i18n
				break;
			}
		}
		return notDuplicate;
	}

	private boolean notSelfJoin(JoinModel newJoin) {
		boolean notSelfJoin = !newJoin.getLeftKeyFieldModel().getParentTable().equals(newJoin.getRightKeyFieldModel().getParentTable());
		if (!notSelfJoin) {
			errors.append("Self Join Detected\n");  // TODO: i18n
		}
		return notSelfJoin;
	}

	private boolean notCircularJoins(JoinModel newJoin) {
		//TODO pending
		boolean notCircularJoin = true;
		/*JoinTableModel targetTable = newJoin.getRightKeyFieldModel().getParentTable();
		for (JoinModel join : this.joinGuiModel.getJoins()) {
			JoinTableModel sourceTable = join.getLeftKeyFieldModel().getParentTable();
			if (targetTable.equals(sourceTable)) {
				notCircularJoin = false;
				errors.append("Circular Join Detected\n");  // TODO: i18n
				break;
			}
		}*/
		return notCircularJoin;
	}

	public boolean isFinishable() {
		//Can only finish if all the tables are joined and datasource name is present.
		return allTablesJoined();
	}

	private boolean allTablesJoined() {

		boolean allTablesJoined = true;
		next: for (JoinTableModel table : this.joinGuiModel.getSelectedTables()) {
			for (JoinModel join : this.joinGuiModel.getJoins()) {
				JoinTableModel table1 = join.getLeftKeyFieldModel().getParentTable();
				JoinTableModel table2 = join.getRightKeyFieldModel().getParentTable();
				if (table.equals(table1) || table.equals(table2)) {
					continue next;
				}
			}
			allTablesJoined = false;
			break;
		}
		return allTablesJoined;
	}
	
	public String getErrors() {
		return this.errors.toString();
	}
}
