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



package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.junit.Test;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim_Polynkov.
 */
public class AnalysisImportDialogControllerTest {

  @Test
  public void testDatasourceParamGetsQuoted() throws Exception {
    AnalysisImportDialogController controller = new AnalysisImportDialogController();
    assertEquals( "Datasource=\"semi;name\"", controller.datasourceParam( "semi;name" ) );
  }

  @Test
  public void testHandleParam() throws Exception {
    final StringBuilder dsInputName = new StringBuilder( "DataSource" );
    final StringBuilder dsInputValue = new StringBuilder( "DS &quot;Test's&quot; & <Fun>" );
    final String dsExpectedValue = "DS \"Test's\" & <Fun>";

    final StringBuilder dspInputName = new StringBuilder( "DynamicSchemaProcessor" );
    final StringBuilder dspInputValue = new StringBuilder( "DSP's & &quot;Other&quot; <stuff>" );
    final String dspExpectedValueDSP = "DSP's & \"Other\" <stuff>";

    AnalysisImportDialogController controller = new AnalysisImportDialogController();
    AnalysisImportDialogModel importDialogModel = new AnalysisImportDialogModel();

    importDialogModel.setAnalysisParameters( new ArrayList<>() );
    importDialogModel.setParameterMode( true );

    Field modelField = FieldUtils.getDeclaredField( AnalysisImportDialogController.class, "importDialogModel", true );
    modelField.setAccessible( true );
    FieldUtils.writeField( modelField, controller, importDialogModel );

    controller.handleParam( dsInputName, dsInputValue );
    controller.handleParam( dspInputName, dspInputValue );

    ParameterDialogModel parameterFirst = controller.getDialogResult().getAnalysisParameters().get( 0 );
    ParameterDialogModel parameterSecond = controller.getDialogResult().getAnalysisParameters().get( 1 );

    assertEquals( parameterFirst.getValue(), dsExpectedValue );
    assertEquals( parameterSecond.getValue(), dspExpectedValueDSP );
    assertEquals( controller.getDialogResult().getParameters(),
      "DataSource=\"DS &quot;Test's&quot; & <Fun>\";DynamicSchemaProcessor=\"DSP's & &quot;Other&quot; <stuff>\"" );
  }
}
