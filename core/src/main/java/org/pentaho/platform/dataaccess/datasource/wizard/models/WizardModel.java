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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.DummyDatasource;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * User: nbaker Date: 3/30/11
 */
public class WizardModel extends XulEventSourceAdapter implements IWizardModel {
  private LinkedHashSet<IWizardDatasource> datasources = new LinkedHashSet<IWizardDatasource>();
  private String datasourceName;
  private boolean editing;
  private List<Class<? extends IWizardDatasource>> ignoredDatasources =
    new ArrayList<Class<? extends IWizardDatasource>>();
  private IWizardDatasource selectedDatasource;
  private boolean reportingOnlyValid = true;

  public WizardModel() {
    addDatasource( new DummyDatasource() );
  }

  @Override
  @Bindable
  public String getDatasourceName() {
    return datasourceName;
  }

  @Override
  @Bindable
  public void setDatasourceName( String datasourceName ) {
    String prevVal = this.datasourceName;
    this.datasourceName = datasourceName;
    firePropertyChange( "datasourceName", prevVal, datasourceName );
  }

  @Override
  @Bindable
  public Set getDatasources() {
    return datasources;
  }

  @Override
  public void addDatasource( IWizardDatasource datasource ) {
    // due to initialization order, datasources may be added after a call to remove them by type (cleaning out
    // built-ins)
    if ( ignoredDatasources.contains( datasource.getClass() ) ) {
      return;
    }
    boolean reallyAdded = this.datasources.add( datasource );
    if ( reallyAdded ) {
      firePropertyChange( "datasources", null, datasources );
    }
    if ( selectedDatasource == null ) {
      setSelectedDatasource( datasources.iterator().next() );
    }
  }

  @Bindable
  public void setSelectedDatasource( IWizardDatasource datasource ) {
    IWizardDatasource prevSelection = selectedDatasource;
    selectedDatasource = datasource;
    firePropertyChange( "selectedDatasource", prevSelection, selectedDatasource );
  }

  @Bindable
  public IWizardDatasource getSelectedDatasource() {
    return selectedDatasource;
  }

  @Override
  public void removeDatasourceByType( Class<? extends IWizardDatasource> datasource ) {
    ignoredDatasources.add( datasource );
    IWizardDatasource matchedSource = null;
    for ( IWizardDatasource source : datasources ) {
      if ( source.getClass().equals( datasource ) ) {
        matchedSource = source;
        break;
      }
    }
    if ( matchedSource != null ) {
      datasources.remove( matchedSource );
    }
  }

  @Override
  public boolean isEditing() {
    return editing;
  }

  @Override
  public void setEditing( boolean editing ) {
    this.editing = editing;
    firePropertyChange( "editing", !this.editing, this.editing );
  }

  @Override
  @Bindable
  public boolean isReportingOnlyValid() {
    return reportingOnlyValid;
  }

  @Bindable
  public void setReportingOnlyValid( boolean reportingOnlyValid ) {
    this.reportingOnlyValid = reportingOnlyValid;
    firePropertyChange( "reportingOnlyValid", !this.reportingOnlyValid, this.reportingOnlyValid );
  }

  @Override
  public void reset() {
    this.setDatasourceName( "" );
    this.setSelectedDatasource( datasources.iterator().next() );
    this.setReportingOnlyValid( true );
    for ( IWizardDatasource source : datasources ) {
      source.reset();
    }
  }
}
