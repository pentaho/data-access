/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;

public class JoinValidator {

  private MultitableGuiModel joinGuiModel;
  private JoinError error;

  public JoinValidator( MultitableGuiModel joinGuiModel, IWizardModel wizardModel ) {
    this.joinGuiModel = joinGuiModel;
  }

  public boolean isValid( JoinRelationshipModel join ) {
    // validate against duplicate joins.
    // and
    // validate against joining to the same table.
    // and
    // validate against circular joins
    return notDuplicate( join ) && notSelfJoin( join ) && notCircularJoins( join );
  }

  private boolean notDuplicate( JoinRelationshipModel newJoin ) {
    boolean notDuplicate = true;
    for ( JoinRelationshipModel join : this.joinGuiModel.getJoins() ) {
      if ( newJoin.equals( join ) ) {
        notDuplicate = false;
        this.error = new JoinError( MessageHandler.getString( "multitable.DUPLICATE_JOIN_TITLE" ),
          MessageHandler.getString( "multitable.DUPLICATE_JOIN_ERROR" ) );
        break;
      }
    }
    return notDuplicate;
  }

  private boolean notSelfJoin( JoinRelationshipModel newJoin ) {
    boolean notSelfJoin =
      !newJoin.getLeftKeyFieldModel().getParentTable().equals( newJoin.getRightKeyFieldModel().getParentTable() );
    if ( !notSelfJoin ) {
      this.error = new JoinError( MessageHandler.getString( "multitable.SELF_JOIN_TITLE" ),
        MessageHandler.getString( "multitable.SELF_JOIN_ERROR" ) );
    }
    return notSelfJoin;
  }

  private boolean notCircularJoins( JoinRelationshipModel newJoin ) {
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

    if ( isSingleTable() ) {
      return true;
    }
    List<String> orphanedTables = new ArrayList<String>();
  next:
    for ( JoinTableModel table : this.joinGuiModel.getSelectedTables() ) {
      for ( JoinRelationshipModel join : this.joinGuiModel.getJoins() ) {
        JoinTableModel table1 = join.getLeftKeyFieldModel().getParentTable();
        JoinTableModel table2 = join.getRightKeyFieldModel().getParentTable();
        if ( table.equals( table1 ) || table.equals( table2 ) ) {
          continue next;
        }
      }
      orphanedTables.add( table.getName() );
    }
    if ( !orphanedTables.isEmpty() ) {
      StringBuffer tables = new StringBuffer();
      for ( String table : orphanedTables ) {
        tables.append( " " + table + ", " );
      }
      this.error = new JoinError( MessageHandler.getString( "multitable.ORPHANED_TABLES_TITLE" ),
        MessageHandler.getString( "multitable.ORPHANED_TABLES" ) + tables.substring( 0, tables.lastIndexOf( "," ) ) );
    }
    return orphanedTables.isEmpty();
  }

  public JoinError getError() {
    return this.error;
  }
}
