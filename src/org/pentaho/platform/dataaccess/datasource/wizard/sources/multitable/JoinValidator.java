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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.MultitableGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinRelationshipGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinedTableGuiModel;

public class JoinValidator {

	private MultitableGuiModel joinGuiModel;
	private IWizardModel wizardModel;
	private JoinError error;

	public JoinValidator(MultitableGuiModel joinGuiModel, IWizardModel wizardModel) {
		this.joinGuiModel = joinGuiModel;
		this.wizardModel = wizardModel;
	}

	public boolean isValid(JoinRelationshipGuiModel join) {
		// validate against duplicate joins.
		// and
		// validate against joining to the same table.
		// and
		// validate against circular joins
		return notDuplicate(join) && notSelfJoin(join) && notCircularJoins(join);
	}

	private boolean notDuplicate(JoinRelationshipGuiModel newJoin) {
		boolean notDuplicate = true;
		for (JoinRelationshipGuiModel join : this.joinGuiModel.getJoins()) {
			if (newJoin.equals(join)) {
				notDuplicate = false;
				this.error = new JoinError(MessageHandler.getString("multitable.DUPLICATE_JOIN_TITLE"), MessageHandler.getString("multitable.DUPLICATE_JOIN_ERROR"));
				break;
			}
		}
		return notDuplicate;
	}

	private boolean notSelfJoin(JoinRelationshipGuiModel newJoin) {
		boolean notSelfJoin = !newJoin.getLeftKeyFieldModel().getParentTable().equals(newJoin.getRightKeyFieldModel().getParentTable());
		if (!notSelfJoin) {
			this.error = new JoinError(MessageHandler.getString("multitable.SELF_JOIN_TITLE"), MessageHandler.getString("multitable.SELF_JOIN_ERROR"));
		}
		return notSelfJoin;
	}

	private boolean notCircularJoins(JoinRelationshipGuiModel newJoin) {
		// TODO pending
		boolean notCircularJoin = true;
		/*
		 * JoinTableModel targetTable =
		 * newJoin.getRightKeyFieldModel().getParentTable(); for (JoinModel join
		 * : this.joinGuiModel.getJoins()) { JoinTableModel sourceTable =
		 * join.getLeftKeyFieldModel().getParentTable(); if
		 * (targetTable.equals(sourceTable)) { notCircularJoin = false;
		 * errors.append("Circular Join Detected\n"); // TODO: i18n break; } }
		 */
		return notCircularJoin;
	}

	public boolean hasTablesSelected() {
		return !this.joinGuiModel.getSelectedTables().isEmpty();
	}

	private boolean isSingleTable() {
		return this.joinGuiModel.getSelectedTables().size() == 1;
	}

	public boolean allTablesJoined() {

		if (isSingleTable()) {
			return true;
		}
		List<String> orphanedTables = new ArrayList<String>();
		next: for (JoinedTableGuiModel table : this.joinGuiModel.getSelectedTables()) {
			for (JoinRelationshipGuiModel join : this.joinGuiModel.getJoins()) {
				JoinedTableGuiModel table1 = join.getLeftKeyFieldModel().getParentTable();
				JoinedTableGuiModel table2 = join.getRightKeyFieldModel().getParentTable();
				if (table.equals(table1) || table.equals(table2)) {
					continue next;
				}
			}
			orphanedTables.add(table.getName());
		}
		if (!orphanedTables.isEmpty()) {
			StringBuffer tables = new StringBuffer();
			for (String table : orphanedTables) {
				tables.append(" " + table + ", ");
			}
			this.error = new JoinError(MessageHandler.getString("multitable.ORPHANED_TABLES_TITLE"), MessageHandler.getString("multitable.ORPHANED_TABLES") + tables.substring(0, tables.lastIndexOf(",")));
		}
		return orphanedTables.isEmpty();
	}

	public JoinError getError() {
		return this.error;
	}
}
