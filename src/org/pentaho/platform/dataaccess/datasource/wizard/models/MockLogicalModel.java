package org.pentaho.platform.dataaccess.datasource.wizard.models;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.TargetColumnType;

/**
 * Provides a mocked <code>LogicalModel</code> that can be used for testing the persistence layer.
 * 
 * @author tkafalas
 *
 */
public class MockLogicalModel {
  public static LogicalModel buildDefaultModel() {
    try {
      final LogicalModel model = new LogicalModel();

      final LogicalTable bt1 = new LogicalTable();
      bt1.setId( "bt1" ); //$NON-NLS-1$
      bt1.setProperty( SqlPhysicalTable.TARGET_TABLE, "pt1" ); //$NON-NLS-1$
      final LogicalColumn bc1 = new LogicalColumn();
      bc1.setId( "bc1" ); //$NON-NLS-1$
      bc1.setProperty( SqlPhysicalColumn.TARGET_COLUMN, "pc1" ); //$NON-NLS-1$
      bc1.setLogicalTable( bt1 );
      bc1.setDataType( DataType.NUMERIC );
      bt1.addLogicalColumn( bc1 );
      bt1.setProperty( SqlPhysicalTable.RELATIVE_SIZE, 1 );

      final LogicalTable bt2 = new LogicalTable();
      bt2.setId( "bt2" ); //$NON-NLS-1$
      bt2.setProperty( SqlPhysicalTable.TARGET_TABLE, "pt2" ); //$NON-NLS-1$
      final LogicalColumn bc2 = new LogicalColumn();
      bc2.setId( "bc2" ); //$NON-NLS-1$
      bc2.setProperty( SqlPhysicalColumn.TARGET_COLUMN, "pc2" ); //$NON-NLS-1$
      bc2.setLogicalTable( bt2 );
      bt2.addLogicalColumn( bc2 );

      final LogicalColumn bce2 = new LogicalColumn();
      bce2.setId( "bce2" ); //$NON-NLS-1$
      bce2.setProperty( SqlPhysicalColumn.TARGET_COLUMN_TYPE, TargetColumnType.OPEN_FORMULA );
      bce2.setProperty( SqlPhysicalColumn.TARGET_COLUMN, "[bt2.bc2] * 2" ); //$NON-NLS-1$
      bce2.setLogicalTable( bt2 );
      bt2.addLogicalColumn( bce2 );
      bt2.setProperty( SqlPhysicalTable.RELATIVE_SIZE, 1 );

      final LogicalTable bt3 = new LogicalTable();
      bt3.setId( "bt3" ); //$NON-NLS-1$
      bt3.setProperty( SqlPhysicalTable.TARGET_TABLE, "pt3" ); //$NON-NLS-1$
      final LogicalColumn bc3 = new LogicalColumn();
      bc3.setId( "bc3" ); //$NON-NLS-1$
      bc3.setProperty( SqlPhysicalColumn.TARGET_COLUMN, "pc3" ); //$NON-NLS-1$
      bc3.setLogicalTable( bt3 );
      bt3.addLogicalColumn( bc3 );
      bt3.setProperty( SqlPhysicalTable.RELATIVE_SIZE, 1 );

      final LogicalTable bt4 = new LogicalTable();
      bt4.setId( "bt4" ); //$NON-NLS-1$
      bt4.setProperty( SqlPhysicalTable.TARGET_TABLE, "pt4" ); //$NON-NLS-1$
      final LogicalColumn bc4 = new LogicalColumn();
      bc4.setId( "bc4" ); //$NON-NLS-1$
      bc4.setProperty( SqlPhysicalColumn.TARGET_COLUMN, "pc4" ); //$NON-NLS-1$
      bc4.setLogicalTable( bt4 );
      bt4.addLogicalColumn( bc4 );
      bt4.setProperty( SqlPhysicalTable.RELATIVE_SIZE, 1 );

      final LogicalTable bt5 = new LogicalTable();
      bt5.setId( "bt5" ); //$NON-NLS-1$
      bt5.setProperty( SqlPhysicalTable.TARGET_TABLE, "pt5" ); //$NON-NLS-1$
      final LogicalColumn bc5 = new LogicalColumn();
      bc5.setId( "bc5" ); //$NON-NLS-1$
      bc5.setProperty( SqlPhysicalColumn.TARGET_COLUMN, "pc5" ); //$NON-NLS-1$
      bc5.setLogicalTable( bt5 );
      bt5.addLogicalColumn( bc5 );
      bt5.setProperty( SqlPhysicalTable.RELATIVE_SIZE, 1 );
      final LogicalRelationship rl1 = new LogicalRelationship();

      rl1.setFromTable( bt1 );
      rl1.setFromColumn( bc1 );
      rl1.setToTable( bt2 );
      rl1.setToColumn( bc2 );

      final LogicalRelationship rl2 = new LogicalRelationship();

      rl2.setFromTable( bt2 );
      rl2.setFromColumn( bc2 );
      rl2.setToTable( bt3 );
      rl2.setToColumn( bc3 );

      final LogicalRelationship rl3 = new LogicalRelationship();

      rl3.setFromTable( bt3 );
      rl3.setFromColumn( bc3 );
      rl3.setToTable( bt4 );
      rl3.setToColumn( bc4 );

      final LogicalRelationship rl4 = new LogicalRelationship();

      rl4.setFromTable( bt4 );
      rl4.setFromColumn( bc4 );
      rl4.setToTable( bt5 );
      rl4.setToColumn( bc5 );

      model.getLogicalTables().add( bt1 );
      model.getLogicalTables().add( bt2 );
      model.getLogicalTables().add( bt3 );
      model.getLogicalTables().add( bt4 );
      model.getLogicalTables().add( bt5 );

      model.getLogicalRelationships().add( rl1 );
      model.getLogicalRelationships().add( rl2 );
      model.getLogicalRelationships().add( rl3 );
      model.getLogicalRelationships().add( rl4 );

      return model;
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }
  
  public static LogicalModel buildLogicalModelWithTemplateModel() {
    LogicalModel lm = buildDefaultModel();
    lm.setProperty(DSWModelStorage.TEMPLATE_ID_PROPERTY, "CSV");
    lm.setProperty(DSWModelStorage.TEMPLATE_MODEL_PROPERTY, "DSWTemplateModelImage");
    return lm;
  }
}
