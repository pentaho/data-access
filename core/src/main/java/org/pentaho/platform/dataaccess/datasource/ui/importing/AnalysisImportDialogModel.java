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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.utils.DataSourceInfoUtil;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class AnalysisImportDialogModel extends XulEventSourceAdapter {

  private List<IDatabaseConnection> connectionList;
  private List<ParameterDialogModel> analysisParameters;
  private String uploadedFile;
  private IDatabaseConnection connection;
  private boolean isParameterMode;
  private ParameterDialogModel selectedAnalysisParameter;

  public AnalysisImportDialogModel() {
    connectionList = new ArrayList<IDatabaseConnection>();
    analysisParameters = new ArrayList<ParameterDialogModel>();
  }

  public void addParameter( String name, String value ) {

    if ( selectedAnalysisParameter == null ) {
      if ( !containsParameter( name ) ) {
        analysisParameters.add( new ParameterDialogModel( name, value ) );
      }
    } else {
      selectedAnalysisParameter.setName( name );
      selectedAnalysisParameter.setValue( value );
    }
    this.firePropertyChange( "analysisParameters", null, analysisParameters );
  }

  public void removeParameter( int paramIndex ) {
    selectedAnalysisParameter = null;
    analysisParameters.remove( paramIndex );
    this.firePropertyChange( "analysisParameters", null, analysisParameters );
  }

  public void removeAllParameters() {
    analysisParameters.clear();
    this.firePropertyChange( "analysisParameters", null, analysisParameters );
  }

  public String getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile( String uploadedFile ) {
    this.uploadedFile = uploadedFile;
  }

  @Bindable
  public List<ParameterDialogModel> getAnalysisParameters() {
    return analysisParameters;
  }

  @Bindable
  public void setAnalysisParameters( List<ParameterDialogModel> value ) {
    List<ParameterDialogModel> previousValue = analysisParameters;
    this.analysisParameters = value;
    this.firePropertyChange( "analysisParameters", previousValue, value );
  }

  @Bindable
  public List<IDatabaseConnection> getConnectionList() {
    return connectionList;
  }

  @Bindable
  public void setConnectionList( List<IDatabaseConnection> value ) {
    List<IDatabaseConnection> previousValue = connectionList;
    this.connectionList = value;
    this.firePropertyChange( "connectionList", previousValue, value );
  }

  @Bindable
  public IDatabaseConnection getConnection() {
    return connection;
  }

  @Bindable
  public void setConnection( IDatabaseConnection value ) {
    IDatabaseConnection previousValue = connection;
    connection = value;
    firePropertyChange( "connection", previousValue, value );
  }

  public String getParameters() {
    String result = "";
    if ( isParameterMode ) {
      String sep = ";";
      String eq = "=";
      String quot = "\"";
      String value;
      for ( ParameterDialogModel currentParameter : analysisParameters ) {
        //add separator if necessary
        if ( !result.isEmpty() ) {
          result += sep;
        }
        //add name / value pair
        //Escape is used, because value can contain quotes and it is parse-unsafe.
        value = DataSourceInfoUtil.escapeQuotes( currentParameter.getValue() );
        if ( //if the value is not quoted
          ( !( value.startsWith( quot ) && value.endsWith( quot ) ) )
            //and the value contains the separator
            && value.contains( sep ) ) {  //then quote the value:
          value = quot + value + quot;
        }
        result = result + currentParameter.getName() + eq + value;
      }
    }
    return result;
  }

  private boolean containsParameter( String name ) {
    boolean containsParameter = false;
    for ( ParameterDialogModel parameter : analysisParameters ) {
      if ( parameter.getName().equalsIgnoreCase( name ) ) {
        containsParameter = true;
        break;
      }
    }
    return containsParameter;
  }

  public void setParameterMode( boolean value ) {
    isParameterMode = value;
  }

  public boolean isValid() {
    boolean isValid = true;
    if ( isParameterMode ) {
      isValid = analysisParameters.size() > 0;
    }
    return isValid && uploadedFile != null && connection != null;
  }

  public void setSelectedAnalysisParameter( int index ) {
    if ( index > -1 ) {
      selectedAnalysisParameter = analysisParameters.get( index );
    } else {
      selectedAnalysisParameter = null;
    }
  }

  public ParameterDialogModel getSelectedAnalysisParameter() {
    return selectedAnalysisParameter;
  }
}
