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
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.steps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtWaitingDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.IDatasourceTypeController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.PhysicalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardRelationalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IModelInfoValidationListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IRelationalModelValidationListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

@SuppressWarnings("unchecked")
public class PhysicalStep extends AbstractWizardStep<DatasourceModel> implements IRelationalModelValidationListener, IModelInfoValidationListener {

  //TODO BusinessData reviewed.
  
  public static final int DEFAULT_RELATIONAL_TABLE_ROW_COUNT = 5;
  public static final int DEFAULT_CSV_TABLE_ROW_COUNT = 7;
  private DatasourceMessages datasourceMessages;
  private IXulAsyncDatasourceService datasourceService;
  private ICsvDatasourceServiceAsync csvDatasourceService;
  public static final int RELATIONAL_TAB = 0;
  public static final int CSV_TAB = 1;
  private List<IDatasourceTypeController> datasourceTypeControllers;
  XulTextbox datasourceNameTextBox = null;
  XulButton okButton = null;

  XulButton cancelButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulTextbox uploadedFileTextBox = null;

  /**
   * The domain being edited.
   */
  private Domain domainToBeSaved;
  
  private XulTree csvDataTable = null;
  private XulDialog clearModelWarningDialog = null;
  private XulDialog overwriteDialog = null;
  private XulMenuList<String> encodingTypeMenuList = null;  

  private Domain overwriteDomain = null;
  private DatasourceType overwriteDatasourceType = null;
  private DatasourceType tabValueSelected = null;
  private boolean clearModelWarningShown = false;
  private EmbeddedWizard outerController;
  
  private WizardConnectionController connectionController = null;
  private IDatasourceTypeController selectedType = null;
  private PhysicalDatasourceController csvDatasourceController;

  private IXulAsyncConnectionService connectionService;
  
  private static final List<String> ENCODINGS = Arrays.asList("","UTF-8","UTF-16BE","UTF-16LE","UTF-32BE","UTF-32LE","Shift_JIS","ISO-2022-JP","ISO-2022-CN","ISO-2022-KR","GB18030","Big5","EUC-JP","EUC-KR","ISO-8859-1","ISO-8859-2","ISO-8859-5","ISO-8859-6","ISO-8859-7","ISO-8859-8","windows-1251","windows-1256","KOI8-R","ISO-8859-9");
  
  public PhysicalStep(IXulAsyncDatasourceService datasourceService, IXulAsyncConnectionService connectionService, DatasourceMessages messages, EmbeddedWizard embeddedWizard) {
    this.datasourceService = datasourceService;
    this.connectionService = connectionService;
    outerController = embeddedWizard;
    datasourceMessages = messages;

    csvDatasourceService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) csvDatasourceService;
    endpoint.setServiceEntryPoint(PhysicalDatasourceController.getDatasourceURL());
  }
  
  public void setDatasourceTypeControllers(List<IDatasourceTypeController> datasourceTypeControllers) {
    this.datasourceTypeControllers = datasourceTypeControllers;
  }

  @Bindable
  public void createPresentationComponent(final XulDomContainer mainWizardContainer) throws XulException {
    super.createPresentationComponent(mainWizardContainer);
    
    clearModelWarningDialog = (XulDialog) getDocument().getElementById("clearModelWarningDialog");//$NON-NLS-1$
    csvDataTable = (XulTree) getDocument().getElementById("csvDataTable");//$NON-NLS-1$
    errorDialog = (XulDialog) getDocument().getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) getDocument().getElementById("errorLabel");//$NON-NLS-1$    
    successDialog = (XulDialog) getDocument().getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) getDocument().getElementById("successLabel");//$NON-NLS-1$    
    datasourceNameTextBox = (XulTextbox) getDocument().getElementById("datasourceName"); //$NON-NLS-1$
    uploadedFileTextBox = (XulTextbox) getDocument().getElementById("uploadedFile"); //$NON-NLS-1$
    overwriteDialog = (XulDialog)getDocument().getElementById("overwriteDialog"); //$NON-NLS-1$
    encodingTypeMenuList = (XulMenuList<String>) mainWizardContainer.getDocumentRoot().getElementById("encodingTypeMenuList");
    encodingTypeMenuList.setElements(ENCODINGS);

    GwtWaitingDialog waitingDialog = new GwtWaitingDialog(datasourceMessages.getString("waitingDialog.previewLoading"),datasourceMessages.getString("waitingDialog.generatingPreview"));
    
    connectionController = new WizardConnectionController(mainWizardContainer.getDocumentRoot());
    connectionController.setConnectionService(outerController.getConnectionService());
    connectionController.setDatasourceModel(getModel());
    mainWizardContainer.addEventHandler(connectionController);
    
    csvDatasourceController = new PhysicalDatasourceController();
    csvDatasourceController.setXulDomContainer(mainWizardContainer);
    csvDatasourceController.setDatasourceMessages(datasourceMessages);
    csvDatasourceController.setDatasourceModel(getModel());
    csvDatasourceController.setModelInfo(getModel().getModelInfo());
    csvDatasourceController.init();
    mainWizardContainer.addEventHandler(csvDatasourceController);

    WizardRelationalDatasourceController relationalDatasourceController = new WizardRelationalDatasourceController();
    relationalDatasourceController.setXulDomContainer(mainWizardContainer);
    relationalDatasourceController.setBindingFactory(getBindingFactory());
    relationalDatasourceController.setDatasourceMessages(datasourceMessages);
    relationalDatasourceController.setWaitingDialog(waitingDialog);
    relationalDatasourceController.setDatasourceModel(getModel());
    relationalDatasourceController.setService(datasourceService);
    relationalDatasourceController.init();
    
    mainWizardContainer.addEventHandler(relationalDatasourceController);
    getModel().getGuiStateModel().addRelationalModelValidationListener(this);
    getModel().getGuiStateModel().setConnectionService(this.connectionService);

    getModel().getModelInfo().addModelInfoValidationListener(this);
    
    connectionController.setDatasourceMessages(datasourceMessages);      
    csvDatasourceController.setDatasourceModel(getModel());
    relationalDatasourceController.setDatasourceModel(getModel());
    connectionController.setDatasourceModel(getModel());
    
    List<IDatasourceTypeController> dsControllers = new ArrayList<IDatasourceTypeController>();
    dsControllers.add(new NoopDatasourceController());
    dsControllers.add(relationalDatasourceController);
    dsControllers.add(csvDatasourceController);

    selectedType = relationalDatasourceController;
    setDatasourceTypeControllers(dsControllers);

    initialize();
  }

  public void initialize() {
    getModel().clearModel();
  }

  @Bindable
  public void setDatasourceModel(DatasourceModel model) {
    setModel(model);
  }

  @Bindable
  public DatasourceModel getDatasourceModel() {
    return this.getModel();
  }

  public IXulAsyncConnectionService getConnectionService() {
    return connectionService;
  }

  public void setConnectionService(IXulAsyncConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public String getName() {
    return "datasourceController"; //$NON-NLS-1$
  }

  private void handleSaveError(DatasourceModel datasourceModel, Throwable xe) {
    String datasourceName = null;
    if(datasourceModel.getDatasourceType() == DatasourceType.CSV) {
      datasourceName =  datasourceModel.getModelInfo().getStageTableName();
    } else if(datasourceModel.getDatasourceType() == DatasourceType.SQL) {
      datasourceName =  datasourceModel.getDatasourceName();
    }
    openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString("DatasourceController.ERROR_0003_UNABLE_TO_SAVE_MODEL",datasourceName,xe.getLocalizedMessage())); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Bindable
  public void overwriteDialogCancel() {
    overwriteDialog.hide();
  }
  
  @Deprecated
  private void saveCsvModel(final Domain domain, final boolean overwrite) throws DatasourceServiceException {
      // TODO setting value to false to always create a new one. Save as is not yet implemented
      datasourceService.saveLogicalModel(domain, overwrite, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          if (error.getMessage().indexOf("0013") >= 0) { //$NON-NLS-1$
            // prompt for overwrite
            overwriteDomain = domain;
            overwriteDatasourceType = DatasourceType.CSV;
            overwriteDialog.show();
          } else {
            handleSaveError(getModel(), error);
          }
        }

        public void success(Boolean value) {
          domainToBeSaved = getModel().getDomain();
          saveModelDone();
        }
      });
  }


  private void showClearModelWarningDialog(DatasourceType value) {
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
    if(tabValueSelected == DatasourceType.SQL) {
      getModel().setModelInfo(new ModelInfo());
      getModel().setDatasourceType(DatasourceType.SQL);      
    } else if(tabValueSelected == DatasourceType.CSV) {
      csvDataTable.update();
      getModel().getGuiStateModel().clearModel();
      getModel().setDatasourceType(DatasourceType.CSV);
    }
  }
  
  @Bindable
  public Boolean beforeTabSwitch(Integer tabIndex) {
    if(RELATIONAL_TAB == tabIndex) {
      if(!clearModelWarningShown) {
        showClearModelWarningDialog(DatasourceType.SQL);
        clearModelWarningShown = true;
        return false;
      } else {
        return true;
      }
    } else if(CSV_TAB == tabIndex) {
      if(!clearModelWarningShown  && getModel().getQuery() != null
          && getModel().getQuery().length() > 0) {
        showClearModelWarningDialog(DatasourceType.CSV);
        clearModelWarningShown = true;
        return false;
      } else {
        return true;
      }
    }
    return true;
  }

  @Bindable
  public void selectCsv() {
    csvDataTable.update();
    getModel().setDatasourceType(DatasourceType.CSV);
  }

  @Bindable
  public void selectOlap() {

  }
  
  @Bindable
  public void selectSql() {
    getModel().setDatasourceType(DatasourceType.SQL);      
  }

  public void selectMql() {

  }

  public void selectXml() {

  }

  public IXulAsyncDatasourceService getDatasourceService() {
    return datasourceService;
  }

  public void setDatasourceService(IXulAsyncDatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  @Bindable
  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  private void saveModelDone() {
    outerController.onDialogAccept();
  }
  
  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th, getDatasourceMessages().getString("DatasourceEditor.USER_ERROR_TITLE"))); //$NON-NLS-1$
    errorLabel.setValue(ExceptionParser.getErrorMessage(th, getDatasourceMessages().getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED"))); //$NON-NLS-1$
    errorDialog.show();
  }

  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#getStepName()
   */
  public String getStepName() {
    return datasourceMessages.getString("wizardStepName.SOURCE"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#setBindings()
   */
  public void setBindings() {
    getBindingFactory().setBindingType(Binding.Type.BI_DIRECTIONAL);
    final Binding domainBinding = getBindingFactory().createBinding(getModel(), "datasourceName", datasourceNameTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    getBindingFactory().createBinding(getModel(), "datasourceName", datasourceNameTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$    
    BindingConvertor<DatasourceType, Integer> popupIndexConverter = new BindingConvertor<DatasourceType, Integer>() {
      @Override
      public Integer sourceToTarget(DatasourceType value) {
        Integer returnValue = null;
        if (DatasourceType.NONE == value) {
          returnValue = 0;
        } else if (DatasourceType.SQL == value) {
          returnValue = 1;
        } else if (DatasourceType.CSV == value) {
          returnValue = 2;
        }
        return returnValue;
      }

      @Override
      public DatasourceType targetToSource(Integer value) {
        DatasourceType type = null;
        if (value == 0) {
          type = DatasourceType.NONE;
         } else if (value == 1) {
          type = DatasourceType.SQL;
         } else if (value == 2) {
           type = DatasourceType.CSV;
         }
        return type;
      }
    };
    final Binding dataSourceTypeBinding = getBindingFactory().createBinding(getModel(), "datasourceType", "datatypeMenuList", "selectedIndex", popupIndexConverter);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    getBindingFactory().createBinding("datatypeMenuList", "selectedIndex", this, "selectedPhysicalType");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    getBindingFactory().createBinding("datatypeMenuList", "selectedIndex", "datasourceDialogDeck", "selectedIndex");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    // create a binding from the headerRows property of the CsvFileInfo to the first-row-is-header check box
    BindingConvertor<Integer, Boolean> isFirstRowHeaderConverter = BindingConvertor.integer2Boolean();
    getBindingFactory().createBinding(
        getModel().getModelInfo().getFileInfo(), 
        CsvFileInfo.HEADER_ROWS_ATTRIBUTE, 
        "isHeaderCheckBox",  //$NON-NLS-1$
        "checked", //$NON-NLS-1$
        isFirstRowHeaderConverter);

    // Binding convertor to between Delimiter and radio group selected value
    BindingConvertor<String, String> delimiterBindingConvertor = new BindingConvertor<String, String>() {
      public String sourceToTarget(String source) {
        Delimiter delimiter = Delimiter.lookupValue(source);
        if (delimiter != null) {
          return Delimiter.lookupValue(source).getName();
        } else {
          return source;
        }
      }
      public String targetToSource(String target) {
        Delimiter delimiter = Delimiter.lookupName(target);
        if (delimiter != null) {
          return delimiter.getValue();
        } else {
          return target;
        }
      }      
    };
    
    // add binding for the Delimiter to it's corresponding radio group
    getBindingFactory().createBinding(
        getModel().getModelInfo().getFileInfo(),
        CsvFileInfo.DELIMITER_ATTRIBUTE,
        "delimiterRadioGroup", //$NON-NLS-1$
        "value", //$NON-NLS-1$
        delimiterBindingConvertor);
        
    // Binding convertor to between Enclosure and radio group selected value
    BindingConvertor<String, String> enclosureBindingConvertor = new BindingConvertor<String, String>() {
      public String sourceToTarget(String source) {
        Enclosure e = Enclosure.lookupValue(source);
        if (e == null) {
          e = Enclosure.NONE;
        }
        return e.getName();
      }
      public String targetToSource(String target) {
        Enclosure e = Enclosure.lookupName(target);
        if (e == Enclosure.NONE) {
          return null;
        } else {
          return e.getValue();
        }
      }      
    };
    
    // add binding for the Enclosure to it's corresponding radio group
    getBindingFactory().createBinding(getModel().getModelInfo().getFileInfo(),
        CsvFileInfo.ENCLOSURE_ATTRIBUTE,
        "enclosureRadioGroup", //$NON-NLS-1$
        "value", //$NON-NLS-1$
        enclosureBindingConvertor);
    
    // when the delimiter changes, we need to refresh the preview
    getModel().getModelInfo().getFileInfo().addPropertyChangeListener(CsvFileInfo.DELIMITER_ATTRIBUTE, new RefreshPreviewPropertyChangeListener());

    // when the enclosure changes, we need to refresh the preview
    getModel().getModelInfo().getFileInfo().addPropertyChangeListener(CsvFileInfo.ENCLOSURE_ATTRIBUTE, new RefreshPreviewPropertyChangeListener());

    // when the first-row-is-header flag changes, we need to refresh the preview
    getModel().getModelInfo().getFileInfo().addPropertyChangeListener(CsvFileInfo.HEADER_ROWS_ATTRIBUTE, new RefreshPreviewPropertyChangeListener());
    
    getModel().addPropertyChangeListener("datasourceName", new QueryAndDatasourceNamePropertyChangeListener()); //$NON-NLS-1$
    getModel().addPropertyChangeListener("query", new QueryAndDatasourceNamePropertyChangeListener()); //$NON-NLS-1$    

    getBindingFactory().setBindingType(Binding.Type.ONE_WAY);
    
    uploadedFileTextBox.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("value")) {

          // set the defaults if none already selected
          String delimiter = getModel().getModelInfo().getFileInfo().getDelimiter();
          if (delimiter == null || delimiter.equals("")) {
            getModel().getModelInfo().getFileInfo().setDelimiter(",");
            getModel().getModelInfo().getFileInfo().setHeaderRows(1);
          }
          String enclosure = getModel().getModelInfo().getFileInfo().getEnclosure();
          if (enclosure == null || enclosure.equals("")) {
            getModel().getModelInfo().getFileInfo().setEnclosure("\"");
          }

          csvDatasourceController.syncModelInfo();
          getModel().getGuiStateModel().setDirty(true);
          getModel().getModelInfo().validate();
        }
      }
    });

    getBindingFactory().setBindingType(Binding.Type.ONE_WAY);
    getBindingFactory().createBinding(getModel(), "datasourceName", getModel().getModelInfo(), "stageTableName");
    
    // binding to set the first-row-is-header checkbox's enabled property based on the selectedItem in the filesList
    getBindingFactory().createBinding(uploadedFileTextBox, "value", "isHeaderCheckBox", "!disabled", BindingConvertor.object2Boolean());
    // binding to set the delimiters enabled property based on the selectedItem in the filesList
    getBindingFactory().createBinding(uploadedFileTextBox, "value", "delimiterRadioGroup", "!disabled", BindingConvertor.object2Boolean());
    // binding to set the enclosures enabled property based on the selectedItem in the filesList
    getBindingFactory().createBinding(uploadedFileTextBox, "value", "enclosureRadioGroup", "!disabled", BindingConvertor.object2Boolean());

    getBindingFactory().createBinding(getModel().getGuiStateModel(), "editing", datasourceNameTextBox, "disabled");
    getBindingFactory().createBinding(getModel().getModelInfo().getFileInfo(), "friendlyFilename", uploadedFileTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    getBindingFactory().createBinding(uploadedFileTextBox, "value", "encodingTypeMenuList", "!disabled", BindingConvertor.object2Boolean());
    
    BindingConvertor<String, String> encodingBindingConvertor = new BindingConvertor<String, String>() {
        public String sourceToTarget(String source) {
        	return source;
        }
        public String targetToSource(String target) {
        	Collection<String> encodings = encodingTypeMenuList.getElements();
        	if(!encodings.contains(target)) {
        		encodings.add(target);
        		encodingTypeMenuList.setElements(encodings);
        	}
        	return target;
        }      
    };
    getBindingFactory().setBindingType(Binding.Type.BI_DIRECTIONAL);    
    getBindingFactory().createBinding(encodingTypeMenuList, "value", getModel().getModelInfo().getFileInfo(), CsvFileInfo.ENCODING, encodingBindingConvertor); //$NON-NLS-1$ //$NON-NLS-2$
    getModel().getModelInfo().getFileInfo().addPropertyChangeListener(CsvFileInfo.ENCODING, new RefreshPreviewPropertyChangeListener());
    
    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();
      dataSourceTypeBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public void setFocus() {
    datasourceNameTextBox.setFocus();
    setStepImageVisible(true);
  }

  /**
   * Executes when refresh of preview is required (also effective when dirty flag should be set)
   */
  private class RefreshPreviewPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      getModel().getGuiStateModel().setDirty(true);
      try {
          if(evt.getPropertyName().equals(CsvFileInfo.ENCODING)) {
        	  
        	  csvDatasourceService.getPreviewRows(getModel().getModelInfo().getFileInfo().getTmpFilename(),
        			  getModel().getModelInfo().getFileInfo().getHeaderRows() > 0,
  		            10, getModel().getModelInfo().getFileInfo().getEncoding(),
  		            new AsyncCallback<List<String>>()  {
	    		          public void onSuccess(List<String> lines) {
	    		            try {
	    		            	getModel().getModelInfo().getFileInfo().setContents(lines);
	    		            	csvDatasourceController.refreshPreview();
	    		            } catch (Exception e) {
	    		              GWT.log("Had an issue refreshing the data preview", e); //$NON-NLS-1$
	    		            }
	    		          }
	    		          public void onFailure(Throwable th) {
	    		        	  GWT.log(th.toString());
	    		          }
  		        });
          }
        	
        	
          csvDatasourceController.refreshPreview();
      } catch (Exception e) {
    	  GWT.log(e.toString());       
      }
      getModel().getModelInfo().validate();
    }
  }
  
  private class QueryAndDatasourceNamePropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      String newValue = (String) evt.getNewValue();
      if(newValue == null || newValue.trim().length() == 0) {
        setFinishable(false);
      }
    }
  }
  
  @Bindable
  public void setSelectedPhysicalType(final int idx){
    setValid(false);
    setFinishable(false);
    IWizardStep stagingStep = outerController.getWizardController().getStep(1);
    this.selectedType = this.datasourceTypeControllers.get(idx);
    if (selectedType instanceof PhysicalDatasourceController) {
      getModel().getModelInfo().validate();
      stagingStep.setDisabled(false);
    } else if (selectedType instanceof WizardRelationalDatasourceController) {
      getModel().validate();
      stagingStep.setDisabled(true);
    } else {
      // NOOP type selected
      stagingStep.setDisabled(false);
    }
  }
  
  public boolean stepDeactivatingForward(){
    super.stepDeactivatingForward();
    return selectedType.finishing();
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.models.IRelationalModelValidationListener#onRelationalModelInValid()
   */
  public void onRelationalModelInvalid() {
    if (selectedType instanceof WizardRelationalDatasourceController) {
      setValid(false);
      setFinishable(false);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.models.IRelationalModelValidationListener#onRelationalModelValid()
   */
  public void onRelationalModelValid() {
    if (selectedType instanceof WizardRelationalDatasourceController) {
      setValid(false); // no next button
      setFinishable(true);
    }
  }

  /**
   * @author wseyler
   * A noop place holder datasource for when the wizard first opens up and nothing is selected.
   */
  class NoopDatasourceController implements IDatasourceTypeController {

    /* (non-Javadoc)
     * @see org.pentaho.platform.dataaccess.datasource.wizard.controllers.IDatasourceTypeController#finishing()
     */
    public boolean finishing() {
      return false;
    }

    /* (non-Javadoc)
     * @see org.pentaho.platform.dataaccess.datasource.wizard.controllers.IDatasourceTypeController#supportsBusinessData(org.pentaho.platform.dataaccess.datasource.beans.BusinessData)
     */
    public boolean supportsBusinessData(DatasourceModel model) {
      return false;
    }
    
  }

  public void onCsvInValid() {
    if (selectedType instanceof PhysicalDatasourceController) {
      setValid(false);
    }
  }

  public void onCsvValid() {
    if (selectedType instanceof PhysicalDatasourceController) {
      setValid(true);
    }
  }

  public void onModelInfoInvalid() {
    // only care about the csv portion of the model info in this step
  }

  public void onModelInfoValid() {
    // only care about the csv portion of the model info in this step
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.models.IRelationalModelValidationListener#onRelationalModelInValid()
   */
  public void onRelationalModelInValid() {
    // TODO Auto-generated method stub
    
  }

}
