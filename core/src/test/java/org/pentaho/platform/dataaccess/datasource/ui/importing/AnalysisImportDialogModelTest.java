/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.junit.Test;
import org.junit.Assert;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Vadim_Polynkov.
 */
public class AnalysisImportDialogModelTest {

  @Test
  public void testGetParameters_OnlyQuotesEscaped() {
    AnalysisImportDialogModel analysisImportDialogModel = new AnalysisImportDialogModel();
    List<ParameterDialogModel> parameters = new ArrayList<>();

    final String expectedValue = "DataSource=\"DS &quot;Test's&quot; & <Fun>\";"
      + "DynamicSchemaProcessor=\"DSP's & &quot;Other&quot; <stuff>\"";

    ParameterDialogModel parameter = new ParameterDialogModel( "DataSource", "DS \"Test's\" & <Fun>" );
    parameters.add( 0, parameter );
    parameter = new ParameterDialogModel( "DynamicSchemaProcessor", "DSP's & \"Other\" <stuff>" );
    parameters.add( 1, parameter );

    analysisImportDialogModel.setAnalysisParameters( parameters );
    analysisImportDialogModel.setParameterMode( true );
    String dataSourceInfo = analysisImportDialogModel.getParameters();

    Assert.assertEquals( dataSourceInfo, expectedValue );
  }
}
