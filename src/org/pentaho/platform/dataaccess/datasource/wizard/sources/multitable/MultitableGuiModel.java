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

package org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class MultitableGuiModel extends XulEventSourceAdapter {

  private List<String> schemas;
  private AbstractModelList<JoinRelationshipModel> joins;
  private AbstractModelList<JoinTableModel> selectedTables;
  private AbstractModelList<JoinTableModel> availableTables;
  private AbstractModelList<JoinTableModel> leftTables;
  private AbstractModelList<JoinTableModel> rightTables;
  private JoinTableModel leftJoinTable;
  private JoinTableModel rightJoinTable;
  private JoinFieldModel leftKeyField;
  private JoinFieldModel rightKeyField;
  private JoinRelationshipModel selectedJoin;
  private JoinTableModel factTable;
  private boolean doOlap;

  public MultitableGuiModel() {
    this.availableTables = new AbstractModelList<JoinTableModel>();
    this.selectedTables = new AbstractModelList<JoinTableModel>();
    this.leftTables = new AbstractModelList<JoinTableModel>();
    this.rightTables = new AbstractModelList<JoinTableModel>();
    this.joins = new AbstractModelList<JoinRelationshipModel>();
    this.leftJoinTable = new JoinTableModel();
    this.rightJoinTable = new JoinTableModel();
    this.selectedJoin = new JoinRelationshipModel();
    this.schemas = new AbstractModelList<String>();
  }

  @Bindable
  public AbstractModelList<JoinTableModel> getAvailableTables() {
    return this.availableTables;
  }

  @Bindable
  public void setAvailableTables( AbstractModelList<JoinTableModel> availableTables ) {
    this.availableTables.setChildren( availableTables );
  }

  @Bindable
  public void setSchemas( List<String> schemas ) {
    List<String> previousValue = this.schemas;
    this.schemas = schemas;
    this.firePropertyChange( "schemas", previousValue, schemas );
  }

  @Bindable
  public List<String> getSchemas() {
    return this.schemas;
  }

  @Bindable
  public AbstractModelList<JoinTableModel> getSelectedTables() {
    return this.selectedTables;
  }

  @Bindable
  public void setSelectedTables( AbstractModelList<JoinTableModel> selectedTables ) {
    this.selectedTables = selectedTables;
  }

  @Bindable
  public JoinTableModel getLeftJoinTable() {
    return this.leftJoinTable;
  }

  @Bindable
  public void setLeftJoinTable( JoinTableModel leftJoinTable ) {
    this.leftJoinTable = leftJoinTable;
  }

  @Bindable
  public JoinTableModel getRightJoinTable() {
    return this.rightJoinTable;
  }

  @Bindable
  public void setRightJoinTable( JoinTableModel rightJoinTable ) {
    this.rightJoinTable = rightJoinTable;
  }

  @Bindable
  public JoinFieldModel getLeftKeyField() {
    return this.leftKeyField;
  }

  @Bindable
  public void setLeftKeyField( JoinFieldModel leftKeyField ) {
    this.leftKeyField = leftKeyField;
  }

  @Bindable
  public JoinFieldModel getRightKeyField() {
    return this.rightKeyField;
  }

  @Bindable
  public void setRightKeyField( JoinFieldModel rightKeyField ) {
    this.rightKeyField = rightKeyField;
  }

  @Bindable
  public AbstractModelList<JoinRelationshipModel> getJoins() {
    return this.joins;
  }

  @Bindable
  public void setJoins( AbstractModelList<JoinRelationshipModel> joins ) {
    this.joins = joins;
  }

  @Bindable
  public JoinRelationshipModel getSelectedJoin() {
    return this.selectedJoin;
  }

  @Bindable
  public void setSelectedJoin( JoinRelationshipModel selectedJoin ) {
    this.selectedJoin = selectedJoin;
  }

  @Bindable
  public JoinTableModel getFactTable() {
    return factTable;
  }

  @Bindable
  public void setFactTable( JoinTableModel factTable ) {
    this.factTable = factTable;
  }

  public void addJoin( JoinRelationshipModel join ) {
    this.joins.add( join );
    this.selectedJoin = join;
  }

  public void removeSelectedJoin() {
    this.joins.remove( this.selectedJoin );
    this.selectedJoin = ( joins == null || joins.asList().isEmpty() ) ? null : joins.asList().get( 0 );
  }

  public void addSelectedTable( JoinTableModel table ) {
    this.availableTables.remove( table );
    this.selectedTables.add( table );
  }

  public void addSelectedTables( List<JoinTableModel> selected ) {
    this.availableTables.removeAll( selected );
    this.selectedTables.addAll( selected );
  }

  public void removeSelectedTables( List<JoinTableModel> selected ) {
    this.selectedTables.removeAll( selected );
    this.availableTables.addAll( selected );
  }

  public void removeSelectedTable( JoinTableModel table ) {
    this.selectedTables.remove( table );
    this.availableTables.add( table );
  }

  @Bindable
  public AbstractModelList<JoinTableModel> getLeftTables() {
    return this.leftTables;
  }

  @Bindable
  public AbstractModelList<JoinTableModel> getRightTables() {
    return this.rightTables;
  }

  @Bindable
  public boolean isDoOlap() {
    return this.doOlap;
  }

  @Bindable
  public void setDoOlap( boolean isStar ) {
    boolean origStar = this.doOlap;
    this.doOlap = isStar;
    firePropertyChange( "doOlap", origStar, isStar );
  }

  public void computeJoinDefinitionStepTables() {
    this.leftTables.clear();
    this.rightTables.clear();
    if ( this.doOlap ) {
      this.leftTables.add( this.factTable );
      for ( JoinTableModel table : this.selectedTables ) {
        if ( table.equals( this.factTable ) ) {
          continue;
        } else {
          this.rightTables.add( table );
        }
      }
    } else {
      this.leftTables.addAll( this.selectedTables );
      this.rightTables.addAll( this.selectedTables );
    }
  }

  public void processAvailableTables( List<String> tables ) {

    List<JoinTableModel> joinTables = new ArrayList<JoinTableModel>();
    if ( tables.size() > 0 ) {

      mainLoop:
      for ( String table : tables ) {
        JoinTableModel joinTable = new JoinTableModel();
        joinTable.setName( table );
        for ( JoinTableModel selectedTable : selectedTables.getChildren() ) {
          if ( selectedTable.equals( joinTable ) ) {
            continue mainLoop;
          }
        }
        joinTables.add( joinTable );
      }

      Collections.sort( joinTables, new Comparator<JoinTableModel>() {
        @Override
        public int compare( JoinTableModel joinTableModel, JoinTableModel joinTableModel1 ) {
          return joinTableModel.getName().compareTo( joinTableModel1.getName() );
        }
      } );
    }
    setAvailableTables( new AbstractModelList<JoinTableModel>( joinTables ) );
  }

  @Deprecated
  public List<LogicalRelationship> generateLogicalRelationships( List<JoinRelationshipModel> joins ) {
    String locale = LocalizedString.DEFAULT_LOCALE;
    List<LogicalRelationship> logicalRelationships = new ArrayList<LogicalRelationship>();
    for ( JoinRelationshipModel join : joins ) {
      LogicalTable fromTable = new LogicalTable();
      fromTable.setName( new LocalizedString( locale, join.getLeftKeyFieldModel().getParentTable().getName() ) );

      LogicalTable toTable = new LogicalTable();
      toTable.setName( new LocalizedString( locale, join.getRightKeyFieldModel().getParentTable().getName() ) );

      LogicalColumn fromColumn = new LogicalColumn();
      fromColumn.setName( new LocalizedString( locale, join.getLeftKeyFieldModel().getName() ) );

      LogicalColumn toColumn = new LogicalColumn();
      toColumn.setName( new LocalizedString( locale, join.getRightKeyFieldModel().getName() ) );

      LogicalRelationship logicalRelationship = new LogicalRelationship();
      logicalRelationship.setFromTable( fromTable );
      logicalRelationship.setToTable( toTable );
      logicalRelationship.setFromColumn( fromColumn );
      logicalRelationship.setToColumn( toColumn );
      logicalRelationships.add( logicalRelationship );
    }
    return logicalRelationships;
  }

  public MultiTableDatasourceDTO createMultiTableDatasourceDTO( String dsName ) {
    MultiTableDatasourceDTO dto = new MultiTableDatasourceDTO();
    dto.setDoOlap( this.doOlap );
    dto.setDatasourceName( dsName );
    List<String> selectedTables = new ArrayList<String>();
    for ( JoinTableModel tbl : this.selectedTables ) {
      selectedTables.add( tbl.getName() );
    }
    dto.setSelectedTables( selectedTables );
    SchemaModel schema = new SchemaModel();
    schema.setJoins( this.getJoins() );
    schema.setFactTable( this.factTable );
    dto.setSchemaModel( schema );
    return dto;
  }


  public void populateJoinGuiModel( Domain domain, MultiTableDatasourceDTO dto, List tables ) {
    this.selectedTables.clear();
    // Populate "selectedTables" from availableTables using logicalRelationships.
    AbstractModelList<JoinTableModel> selectedTablesList = new AbstractModelList<JoinTableModel>();
    if ( dto != null ) {
      for ( String selectedTable : dto.getSelectedTables() ) {
        this.selectTable( selectedTable, selectedTablesList, tables );
      }
      this.selectedTables.addAll( selectedTablesList );

      // Populates joins.  
      this.computeJoins( dto );

      // Populate fact table.
      boolean isDoOlap = dto.isDoOlap();
      this.setDoOlap( isDoOlap );
      if ( isDoOlap ) {
        for ( JoinTableModel table : this.selectedTables ) {
          if ( tablesAreEqual( table.getName(), dto.getSchemaModel().getFactTable().getName() ) ) {
            this.setFactTable( table );
            break;
          }
        }
      }
    }

    // Populate available tables discarding selected tables.
    this.processAvailableTables( tables );

    if ( domain != null ) {
      // Existing joinTableModels will not have fields. We can add these from the domain.
      this.addFieldsToTables( domain, this.availableTables );
    }
  }

  private void computeJoins( MultiTableDatasourceDTO dto ) {
    this.joins.clear();
    for ( JoinRelationshipModel join : dto.getSchemaModel().getJoins() ) {
      for ( JoinTableModel selectedTable : this.selectedTables ) {
        if ( tablesAreEqual( selectedTable.getName(), join.getLeftKeyFieldModel().getParentTable().getName() ) ) {
          join.getLeftKeyFieldModel().getParentTable().setName( selectedTable.getName() );
        } else {
          if ( tablesAreEqual( selectedTable.getName(), join.getRightKeyFieldModel().getParentTable().getName() ) ) {
            join.getRightKeyFieldModel().getParentTable().setName( selectedTable.getName() );
          }
        }
      }
    }
    this.joins.addAll( dto.getSchemaModel().getJoins() );
  }

  /**
   * try to identify the correct selected table index from the joins
   *
   * @return int > 0 if found
   */
  public int getTableIndex( JoinTableModel joinTable ) {
    return ( this.getSelectedTables() == null || this.getSelectedTables().isEmpty() ) ? 0 :
      this.getSelectedTables().indexOf( joinTable );
  }

  private void addFieldsToTables( Domain domain, AbstractModelList<JoinTableModel> availableTables ) {

    String locale = LocalizedString.DEFAULT_LOCALE;
    Outter:
    for ( JoinTableModel table : availableTables ) {
      for ( LogicalTable tbl : domain.getLogicalModels().get( 0 ).getLogicalTables() ) {
        if ( tbl.getPhysicalTable().getProperty( "target_table" ).equals( table.getName() ) ) {
          for ( LogicalColumn col : tbl.getLogicalColumns() ) {
            JoinFieldModel field = new JoinFieldModel();
            field.setName( col.getName( locale ) );
            field.setParentTable( table );
            table.getFields().add( field );
          }
          continue Outter;
        }
      }
    }
  }

  private void selectTable( String selectedTable, AbstractModelList<JoinTableModel> selectedTablesList,
                            List databaseTables ) {
    for ( Object table : databaseTables ) {
      if ( tablesAreEqual( table.toString(), selectedTable ) ) {
        if ( !selectedTablesList.contains( table ) ) {
          JoinTableModel joinTable = new JoinTableModel();
          joinTable.setName( table.toString() );
          selectedTablesList.add( joinTable );
        }
      }
    }
  }

  public void reset() {
    this.availableTables.clear();
    this.selectedTables.clear();
    this.joins.clear();
    this.schemas.clear();
    this.leftTables.clear();
    this.rightTables.clear();
    this.leftKeyField = null;
    this.rightKeyField = null;
    this.selectedJoin = null;
    this.factTable = null;
    this.leftJoinTable = null;
    this.rightJoinTable = null;
  }

  private boolean tablesAreEqual( String table1, String table2 ) {
    String tableName1 = getSchemaTablePair( table1 )[ 1 ];
    String tableName2 = getSchemaTablePair( table2 )[ 1 ];
    return tableName1.equals( tableName2 );
  }

  private String[] getSchemaTablePair( String table ) {
    if ( table.indexOf( "." ) < 0 ) {
      return new String[] { "", table };
    }
    String[] pair = new String[ 2 ];
    String[] parts = table.split( "\\." );
    pair[ 0 ] = parts[ 0 ];

    String tableName = "";
    for ( int i = 1; i < parts.length; i++ ) {
      tableName = tableName + "." + parts[ i ];
    }
    pair[ 1 ] = tableName.substring( 1 );
    return pair;
  }
}
