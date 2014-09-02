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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

public class WizardDatasourceController extends AbstractXulDialogController<Domain> {
  public static final int DEFAULT_RELATIONAL_TABLE_ROW_COUNT = 5;
  public static final int DEFAULT_CSV_TABLE_ROW_COUNT = 7;
  private DatasourceMessages datasourceMessages;
  private XulDialog datasourceDialog;
  private IXulAsyncDSWDatasourceService service;
  public static final int RELATIONAL_TAB = 0;
  public static final int CSV_TAB = 1;
  private DatasourceModel datasourceModel;

  BindingFactory bf;
  XulTextbox csvDatasourceName = null;
  XulButton okButton = null;

  XulButton cancelButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  /**
   * The domain being edited.
   */
  private Domain domainToBeSaved;

  private XulTree modelDataTable = null;

  private XulTree csvDataTable = null;
  private XulDialog clearModelWarningDialog = null;

  private DatasourceType tabValueSelected = null;
  private boolean clearModelWarningShown = false;
  private XulTabbox datasourceTabbox = null;

  public WizardDatasourceController() {
  }

  @Bindable
  public void init() {
    clearModelWarningDialog = (XulDialog) document.getElementById( "clearModelWarningDialog" ); //$NON-NLS-1$
    csvDataTable = (XulTree) document.getElementById( "csvDataTable" ); //$NON-NLS-1$
    modelDataTable = (XulTree) document.getElementById( "modelDataTable" ); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById( "errorDialog" ); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById( "errorLabel" ); //$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById( "successDialog" ); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById( "successLabel" ); //$NON-NLS-1$
    csvDatasourceName = (XulTextbox) document.getElementById( "datasourceName" ); //$NON-NLS-1$
    datasourceDialog = (XulDialog) document.getElementById( "datasourceDialog" ); //$NON-NLS-1$
    okButton = (XulButton) document.getElementById( "datasourceDialog_accept" ); //$NON-NLS-1$
    cancelButton = (XulButton) document.getElementById( "datasourceDialog_cancel" ); //$NON-NLS-1$
    datasourceTabbox = (XulTabbox) document.getElementById( "datasourceDialogTabbox" ); //$NON-NLS-1$

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( datasourceModel, "validated", okButton, "!disabled" ); //$NON-NLS-1$ //$NON-NLS-2$

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );
    final Binding domainBinding =
      bf.createBinding( datasourceModel.getGuiStateModel(), "datasourceName", csvDatasourceName,
        "value" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( datasourceModel, "datasourceName", csvDatasourceName, "value" ); //$NON-NLS-1$ //$NON-NLS-2$
    BindingConvertor<DatasourceType, Integer> tabIndexConvertor = new BindingConvertor<DatasourceType, Integer>() {
      @Override
      public Integer sourceToTarget( DatasourceType value ) {
        Integer returnValue = null;
        if ( DatasourceType.SQL == value ) {
          returnValue = 0;
        } else if ( DatasourceType.CSV == value ) {
          returnValue = 1;
        } else if ( DatasourceType.OLAP == value ) {
          return 2;
        } else if ( DatasourceType.NONE == value ) {
          return 0;
        }
        return returnValue;
      }

      @Override
      public DatasourceType targetToSource( Integer value ) {
        DatasourceType type = null;
        if ( value == 0 ) {
          type = DatasourceType.SQL;
        } else if ( value == 1 ) {
          type = DatasourceType.CSV;
        } else if ( value == 2 ) {
          type = DatasourceType.OLAP;
        }
        return type;
      }
    };
    bf.createBinding( datasourceModel, "datasourceType", datasourceTabbox, "selectedIndex",
      tabIndexConvertor ); //$NON-NLS-1$ //$NON-NLS-2$
    okButton.setDisabled( true );
    initialize();
    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();

    } catch ( Exception e ) {
      System.out.println( e.getMessage() );
      e.printStackTrace();
    }
  }

  public void initialize() {
    datasourceModel.clearModel();
  }

  @Override
  public void showDialog() {
    super.showDialog();
    setFocus();
  }

  private void setFocus() {
    csvDatasourceName.setFocus();
  }

  public void setBindingFactory( BindingFactory bf ) {
    this.bf = bf;
  }

  @Bindable
  public void setDatasourceModel( DatasourceModel model ) {
    this.datasourceModel = model;
  }

  @Bindable
  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "datasourceController"; //$NON-NLS-1$
  }

  private void showClearModelWarningDialog( DatasourceType value ) {
    tabValueSelected = value;
    clearModelWarningDialog.show();
  }

  @Bindable
  public void closeClearModelWarningDialog() {
    clearModelWarningDialog.hide();
    clearModelWarningShown = false;
  }

  @Bindable
  public void switchTab() {
    closeClearModelWarningDialog();
    if ( tabValueSelected == DatasourceType.SQL ) {
      modelDataTable.update();
      datasourceModel.setDatasourceType( DatasourceType.SQL );
    } else if ( tabValueSelected == DatasourceType.CSV ) {
      csvDataTable.update();
      datasourceModel.getGuiStateModel().clearModel();
      datasourceModel.setDatasourceType( DatasourceType.CSV );
    }
  }

  @Bindable
  public Boolean beforeTabSwitch( Integer tabIndex ) {
    if ( RELATIONAL_TAB == tabIndex ) {
      if ( !clearModelWarningShown && datasourceModel.getModelInfo() != null ) {
        showClearModelWarningDialog( DatasourceType.SQL );
        clearModelWarningShown = true;
        return false;
      } else {
        return true;
      }
    } else if ( CSV_TAB == tabIndex ) {
      if ( !clearModelWarningShown && datasourceModel.getQuery() != null
        && datasourceModel.getQuery().length() > 0 ) {
        showClearModelWarningDialog( DatasourceType.CSV );
        clearModelWarningShown = true;
        return false;
      } else {
        return true;
      }
    }
    return true;
  }

  public IXulAsyncDSWDatasourceService getService() {
    return service;
  }

  public void setService( IXulAsyncDSWDatasourceService service ) {
    this.service = service;
  }

  @Bindable
  public void openErrorDialog( String title, String message ) {
    errorDialog.setTitle( title );
    errorLabel.setValue( message );
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if ( !errorDialog.isHidden() ) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void openSuccesDialog( String title, String message ) {
    successDialog.setTitle( title );
    successLabel.setValue( message );
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if ( !successDialog.isHidden() ) {
      successDialog.hide();
    }
  }

  @Override
  protected XulDialog getDialog() {
    return datasourceDialog;
  }

  @Override
  protected Domain getDialogResult() {
    return domainToBeSaved;
  }


  @Bindable
  private void saveModelDone() {
    super.onDialogAccept();
  }

  public void displayErrorMessage( Throwable th ) {
    errorDialog.setTitle( ExceptionParser
      .getErrorHeader( th, getDatasourceMessages().getString( "DatasourceEditor.USER_ERROR_TITLE" ) ) ); //$NON-NLS-1$
    errorLabel.setValue( ExceptionParser.getErrorMessage( th,
      getDatasourceMessages().getString( "DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED" ) ) ); //$NON-NLS-1$
    errorDialog.show();
  }

  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages( DatasourceMessages datasourceMessages ) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }
}
