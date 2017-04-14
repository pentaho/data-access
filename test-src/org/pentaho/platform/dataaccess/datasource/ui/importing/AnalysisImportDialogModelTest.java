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
* Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
*/

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
