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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created May 26, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;


public class CsvModelData extends XulEventSourceAdapter{

  private List<CsvModelDataRow> csvDataRows = new ArrayList<CsvModelDataRow>();

  public CsvModelData(Domain domain) {
    List<LogicalModel> logicalModels = domain.getLogicalModels();
    int i=0;
    for (LogicalModel logicalModel : logicalModels) {
      List<Category> categories = logicalModel.getCategories();
      for (Category category : categories) {
        List<LogicalColumn> logicalColumns = category.getLogicalColumns();
        for (LogicalColumn logicalColumn : logicalColumns) {
          addCsvModelDataRow(logicalColumn);
        }
      }
    }
  }

  public void addCsvModelDataRow(LogicalColumn column) {
  /*  this.dataRows.add(new ModelDataRow(column, data));*/
    firePropertyChange("csvDataRows", null, csvDataRows);//$NON-NLS-1$
  }

  @Bindable
  public List<CsvModelDataRow> getModelData() {
    return csvDataRows;
  }


  @Bindable
  public void setModelData(List<CsvModelDataRow> csvDataRows) {
    this.csvDataRows = csvDataRows;
    firePropertyChange("csvDataRows", null, csvDataRows);//$NON-NLS-1$
  }


}
