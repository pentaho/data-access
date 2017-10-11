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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/


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
