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
 * Created June 2, 2009
 * @author mlowery
 */
package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.agilebi.modeler.models.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * The state (a.k.a. model) of this dialog.
 * 
 * @author mlowery
 */
public class DatasourceSelectionDialogModel extends XulEventSourceAdapter {

  /**
   * A cached version of the logicalModelSummaries from the <code>DatasourceService</code>.
   */
  private List<LogicalModelSummary> logicalModelSummaries;

  /**
   * The index of the selected datasource.
   */
  private int selectedIndex;

  public void setLogicalModelSummaries(final List<LogicalModelSummary> logicalModelSummaries) {
    final List<LogicalModelSummary> previousVal = this.logicalModelSummaries;
    this.logicalModelSummaries = logicalModelSummaries == null ? null : new ArrayList<LogicalModelSummary>(logicalModelSummaries);
    this.firePropertyChange("logicalModelSummaries", previousVal, logicalModelSummaries); //$NON-NLS-1$
  }

  public List<LogicalModelSummary> getLogicalModelSummaries() {
    return this.logicalModelSummaries == null ? null : new ArrayList<LogicalModelSummary>(logicalModelSummaries);
  }
  
  public void setSelectedLogicalModel(String domainId, String modelId) {
    for (int i = 0; i < logicalModelSummaries.size(); i++) {
      LogicalModelSummary summary = logicalModelSummaries.get(i);
      if (summary.getDomainId().equals(domainId) && summary.getModelId().equals(modelId)) {
        setSelectedIndex(i);
        return;
      }
    }
    setSelectedIndex(-1);
  }

  @Bindable
  public void setSelectedIndex(final int selectedIndex) {
    final int previousVal = this.selectedIndex;
    this.selectedIndex = selectedIndex;
    this.firePropertyChange("selectedIndex", previousVal, selectedIndex); //$NON-NLS-1$
  }

  @Bindable
  public int getSelectedIndex() {
    return selectedIndex;
  }

}
