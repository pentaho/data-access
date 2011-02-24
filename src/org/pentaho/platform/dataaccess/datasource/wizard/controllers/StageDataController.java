package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.FactoryBasedBindingProvider;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeItem;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StageDataController extends AbstractXulEventHandler implements IDatasourceTypeController {

  private static final String MSG_STAGING_DATA = "physicalDatasourceDialog.STAGING_DATA"; //$NON-NLS-1$
  private static final String MSG_ROWS_STAGED = "physicalDatasourceDialog.ROWS_STAGED"; //$NON-NLS-1$
  private static final String MSG_STAGING_FILE = "physicalDatasourceDialog.STAGING_FILE"; //$NON-NLS-1$
  private static final String MSG_STAGING_ERRORS = "physicalDatasourceDialog.STAGING_ERRORS"; //$NON-NLS-1$
  
  private DatasourceMessages messages ;

  private XulDialog errorDialog = null;
  
  private XulLabel errorLabel = null;

  private XulDialog waitingDialog = null;
  
  private XulLabel waitingLabel = null;

  private XulDialog successDialog = null;
  
  private XulLabel successLabel = null;

  private DatasourceModel model;

  private XulDialog previewDialog = null;

  private BindingFactory bf = null;

  private XulLabel previewLabel = null;

  @Bindable
  public void init() {
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingLabel = (XulLabel) document.getElementById("waitingDialogLabel"); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel"); //$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel"); //$NON-NLS-1$
    previewDialog = (XulDialog) document.getElementById("csvPreviewDialog"); //$NON-NLS-1$
    previewLabel = (XulLabel) document.getElementById("csvTextPreviewLabel"); //$NON-NLS-1$
  }
  
  public boolean finishing() {
//    stageData();
    return true;
  }

  public void initializeBusinessData(BusinessData businessData) {
    // TODO Auto-generated method stub

  }

  public boolean supportsBusinessData(BusinessData businessData) {
    // TODO Auto-generated method stub
    return false;
  }

  @Bindable
  public DatasourceModel getDatasourceModel() {
    return model;
  }

  @Bindable
  public void setDatasourceModel(DatasourceModel datasourceModel) {
    this.model = datasourceModel;
  }
  
  @Bindable
  public ModelInfo getModelInfo() {
    return model.getModelInfo();
  }

  @Bindable
  public void setModelInfo(ModelInfo modelInfo) {
    model.setModelInfo(modelInfo);
  }
  
  @Bindable
  public void closeErrorDialog() {
	  errorDialog.hide();
  }
  public void showErrorDialog(String message) {
    errorLabel.setValue(message);
    errorDialog.show();
  }

  public void closeWaitingDialog() {
    waitingDialog.hide();
  }
  public void showWaitingDataStageDialog() {
    waitingLabel.setValue(messages.getString(MSG_STAGING_DATA));    
    waitingDialog.show();
  }
  public void showWaitingFileStageDialog() {
    waitingLabel.setValue(messages.getString(MSG_STAGING_FILE));    
    waitingDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    successDialog.hide();
  }
  public void showSuccessDialog(String message) {
    successLabel.setValue(message);
    successDialog.show();
  }

  public void setDatasourceMessages( DatasourceMessages datasourceMessages ) {
    this.messages = datasourceMessages;
  }

  public void setBindingFactory( BindingFactory bindingFactory ) {
    this.bf = bindingFactory;
  }

  @Override
  public String getName() {
    return "stageDataController";
  }
  
  @Bindable
  public void closePreviewDialog() {
    previewDialog.hide();
  }
  
  @Bindable
  public void showPreviewDialog() throws Exception {
    previewLabel.setValue(getModelInfo().getFileInfo().formatSampleContents());
    previewDialog.show();
  }

  public void clearColumnGrid() throws XulException {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    tree.setElements(null);
    tree.update();
  }

  @Bindable
  public void refreshColumnGrid() {
    generateDataTypeDisplay_horizontal();
  }
  
  private void generateDataTypeDisplay_horizontal() {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    tree.setRows(model.getModelInfo().getColumns().length);

    tree.setBindingProvider(new FactoryBasedBindingProvider(bf) {
      @Override
      public BindingConvertor getConvertor(XulEventSource source, String prop1, XulEventSource target, String prop2) {
        if (source instanceof ColumnInfo) {
          if (prop1.equals("length") || prop1.equals("precision")) { //$NON-NLS-1$ //$NON-NLS-2$
            return BindingConvertor.integer2String();
          }
          else if (prop1.equals("include") && prop2.equals("value")) {  //$NON-NLS-1$//$NON-NLS-2$
            // this is the binding from the cell to the value of the checkbox
            return null;
          } else if (prop1.equals("include")) { //$NON-NLS-1$
            // this binding is from the model to the checkbox
            return BindingConvertor.boolean2String();
          } else if (prop1.equals("availableDataTypes")) { //$NON-NLS-1$
            return new BindingConvertor<List, Vector>() {
              @SuppressWarnings("unchecked")
              public Vector sourceToTarget(List value) {
               return new Vector(value);
              }
              @SuppressWarnings("unchecked")
              public List targetToSource(Vector value) {
                return new ArrayList(value);
              }
            };
          } else if (prop1.equals("formatStrings")) { //$NON-NLS-1$
              return new BindingConvertor<List, Vector>() {
                @SuppressWarnings("unchecked")
                public Vector sourceToTarget(List value) {
                 return new Vector(value);
                }
                @SuppressWarnings("unchecked")
                public List targetToSource(Vector value) {
                  return new ArrayList(value);
                }
              };
          } else if (prop1.equals("dataType") && prop2.equals("selectedIndex")) { //$NON-NLS-1$ //$NON-NLS-2$
            return new BindingConvertor<DataType, Integer>() {
              @Override
              public Integer sourceToTarget(DataType value) {
                List<DataType> types = ColumnInfo.getAvailableDataTypes();
                for(int i = 0; i < types.size(); i++) {
                  if (types.get(i).equals(value)) {
                    return i;
                  }
                }
                return 0;
              }
              @Override
              public DataType targetToSource(Integer value) {
                return ColumnInfo.getAvailableDataTypes().get(value);
              }

            };
          } else if(prop1.equals("formatStringsDisabled")){
            return null;
          } else {
            return BindingConvertor.string2String();
          }
        } else {
          return null;
        }
      }
    });

    tree.setElements(Arrays.asList(model.getModelInfo().getColumns()));
    if(model.getModelInfo().getColumns().length > 0){
      tree.setSelectedRows(new int[]{0});
    }
    tree.update();
  }

  @Bindable
  public void selectAll() {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    for (XulComponent component : tree.getRootChildren().getChildNodes()) {
      XulTreeItem item = (XulTreeItem)component;
      for (XulComponent childComp : item.getChildNodes()) {
        XulTreeRow row = (XulTreeRow)childComp;
        XulTreeCell cell = row.getCell(0);
        cell.setValue(true);
      }
    }
    model.getModelInfo().validate();
  }
  
  @Bindable
  public void deselectAll() {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    for (XulComponent component : tree.getRootChildren().getChildNodes()) {
      XulTreeItem item = (XulTreeItem)component;
      for (XulComponent childComp : item.getChildNodes()) {
        XulTreeRow row = (XulTreeRow)childComp;
        XulTreeCell cell = row.getCell(0);
        cell.setValue(false);
      }
    }
    model.getModelInfo().validate();
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.controllers.IDatasourceTypeController#initializeBusinessData(org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel)
   */
  public void initializeBusinessData(DatasourceModel model) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.controllers.IDatasourceTypeController#supportsBusinessData(org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel)
   */
  public boolean supportsBusinessData(DatasourceModel model) {
    // TODO Auto-generated method stub
    return false;
  }

}
