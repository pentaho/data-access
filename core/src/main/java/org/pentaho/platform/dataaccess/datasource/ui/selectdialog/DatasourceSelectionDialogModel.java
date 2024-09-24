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

package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.ui.xul.XulEventSourceAdapter;
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

  public void setLogicalModelSummaries( final List<LogicalModelSummary> logicalModelSummaries ) {
    final List<LogicalModelSummary> previousVal = this.logicalModelSummaries;
    this.logicalModelSummaries =
      logicalModelSummaries == null ? null : new ArrayList<LogicalModelSummary>( logicalModelSummaries );
    this.firePropertyChange( "logicalModelSummaries", previousVal, logicalModelSummaries ); //$NON-NLS-1$
  }

  public List<LogicalModelSummary> getLogicalModelSummaries() {
    return this.logicalModelSummaries == null ? null : new ArrayList<LogicalModelSummary>( logicalModelSummaries );
  }

  public void setSelectedLogicalModel( String domainId, String modelId ) {
    for ( int i = 0; i < logicalModelSummaries.size(); i++ ) {
      LogicalModelSummary summary = logicalModelSummaries.get( i );
      if ( summary.getDomainId().equals( domainId ) && summary.getModelId().equals( modelId ) ) {
        setSelectedIndex( i );
        return;
      }
    }
    setSelectedIndex( -1 );
  }

  @Bindable
  public void setSelectedIndex( final int selectedIndex ) {
    this.selectedIndex = selectedIndex;
    // we want this to fire every time. setting prevval to always be different.
    this.firePropertyChange( "selectedIndex", selectedIndex + 1, selectedIndex ); //$NON-NLS-1$
  }

  @Bindable
  public int getSelectedIndex() {
    return selectedIndex;
  }

}
