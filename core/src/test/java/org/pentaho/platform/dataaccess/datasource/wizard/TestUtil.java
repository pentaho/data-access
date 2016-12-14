/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

/**
 * Created by rfellows on 11/11/16.
 */
public class TestUtil {
  public static ModelInfo createModel() {
    CsvFileInfo fileInfo = new CsvFileInfo();
    fileInfo.setTmpFilename( "unit_test.csv" );
    fileInfo.setProject( "testsolution" );
    fileInfo.setHeaderRows( 1 );
    fileInfo.setDelimiter( "," );
    fileInfo.setEnclosure( "\"" );

    ColumnInfo[] columns = new ColumnInfo[ 9 ];
    columns[ 0 ] = new ColumnInfo();
    columns[ 0 ].setDataType( DataType.NUMERIC );
    columns[ 0 ].setPrecision( 0 );
    columns[ 0 ].setId( "PC_0" );
    columns[ 0 ].setTitle( "REGIONC" );
    columns[ 0 ].setIndex( true );
    columns[ 0 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 0 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 1 ] = new ColumnInfo();
    columns[ 1 ].setDataType( DataType.NUMERIC );
    columns[ 1 ].setId( "PC_1" );
    columns[ 1 ].setTitle( "NWEIGHT" );
    columns[ 1 ].setPrecision( 5 );
    columns[ 1 ].setIndex( true );
    columns[ 1 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 1 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 2 ] = new ColumnInfo();
    columns[ 2 ].setDataType( DataType.NUMERIC );
    columns[ 2 ].setId( "PC_2" );
    columns[ 2 ].setTitle( "Int" );
    columns[ 2 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 2 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 3 ] = new ColumnInfo();
    columns[ 3 ].setDataType( DataType.DATE );
    columns[ 3 ].setId( "PC_3" );
    columns[ 3 ].setTitle( "xdate" );
    columns[ 3 ].setFormat( "mm/dd/yy" );
    columns[ 3 ].setIndex( true );
    columns[ 3 ].setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    columns[ 3 ].setAggregateType( AggregationType.NONE.toString() );

    columns[ 4 ] = new ColumnInfo();
    columns[ 4 ].setDataType( DataType.STRING );
    columns[ 4 ].setId( "PC_4" );
    columns[ 4 ].setTitle( "" );
    columns[ 4 ].setIgnore( true );
    columns[ 4 ].setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    columns[ 4 ].setAggregateType( AggregationType.NONE.toString() );

    columns[ 5 ] = new ColumnInfo();
    columns[ 5 ].setDataType( DataType.STRING );
    columns[ 5 ].setId( "PC_5" );
    columns[ 5 ].setTitle( "Location" );
    columns[ 5 ].setIndex( true );
    columns[ 5 ].setLength( 60 );
    columns[ 5 ].setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    columns[ 5 ].setAggregateType( AggregationType.NONE.toString() );

    columns[ 6 ] = new ColumnInfo();
    columns[ 6 ].setDataType( DataType.NUMERIC );
    columns[ 6 ].setId( "PC_6" );
    columns[ 6 ].setTitle( "charlen" );
    columns[ 6 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 6 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 7 ] = new ColumnInfo();
    columns[ 7 ].setDataType( DataType.NUMERIC );
    columns[ 7 ].setId( "PC_7" );
    columns[ 7 ].setTitle( "xfactor" );
    columns[ 7 ].setPrecision( 7 );
    columns[ 7 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 7 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 8 ] = new ColumnInfo();
    columns[ 8 ].setDataType( DataType.BOOLEAN );
    columns[ 8 ].setId( "PC_8" );
    columns[ 8 ].setTitle( "Flag" );
    columns[ 8 ].setIndex( true );
    columns[ 8 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 8 ].setAggregateType( AggregationType.SUM.toString() );

    ModelInfo info = new ModelInfo();
    info.setFileInfo( fileInfo );
    info.setColumns( columns );
    info.setStageTableName( "UNIT_TESTS" );

    return info;
  }
}
