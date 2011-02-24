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
 * Created June, 2010
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.List;

import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtWaitingDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;


public class PhysicalDatasourceController extends AbstractXulEventHandler implements IDatasourceTypeController {

  private static final String INVALID_IMAGE = "images/invalid.png"; //$NON-NLS-1$

  private static final String CHECK_MARK_IMAGE = "images/check_mark.png"; //$NON-NLS-1$

  private XulDialog errorDialog = null;
  
  private XulLabel errorLabel = null;

  private XulDialog successDialog = null;
  
  private XulLabel successLabel = null;
  
  private org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel datasourceModel;
  
  private DatasourceMessages messages ;

  private XulLabel csvTextPreview = null;
  
  private ICsvDatasourceServiceAsync csvModelService;

  public PhysicalDatasourceController() {
  }

  @Bindable
  public void init() {
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel"); //$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel"); //$NON-NLS-1$
    csvTextPreview = (XulLabel) document.getElementById("csvTextPreview"); //$NON-NLS-1$
  }
  
  public String getName() {
    return "physicalDatasourceController"; //$NON-NLS-1$
  }

  @Bindable
  public void toggleIncludeColumnCheck(String includeToggleButtonId) {
    XulButton toggle = (XulButton) document.getElementById(includeToggleButtonId);
    if (toggle.getImage().endsWith(CHECK_MARK_IMAGE)) {
      toggle.setImage(INVALID_IMAGE);
    } else {
      toggle.setImage(CHECK_MARK_IMAGE);
    }
  }

  @Bindable
  public ModelInfo getModelInfo() {
    return datasourceModel.getModelInfo();
  }

  @Bindable
  public void setModelInfo(ModelInfo modelInfo) {
    datasourceModel.setModelInfo(modelInfo);
  }

  // TODO: REmove once CSVService is seperated out
  public static String getDatasourceURL(){
	String moduleUrl = GWT.getModuleBaseURL();
    if (moduleUrl.indexOf("content") > -1) {//$NON-NLS-1$
        //we are running the client in the context of a BI Server plugin, so 
        //point the request to the GWT rpc proxy servlet
        String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));//$NON-NLS-1$
        //NOTE: the dispatch URL ("connectionService") must match the bean id for 
        //this service object in your plugin.xml.  "gwtrpc" is the servlet 
        //that handles plugin gwt rpc requests in the BI Server.
        return baseUrl + "gwtrpc/CsvDatasourceService";//$NON-NLS-1$
      }
      //we are running this client in hosted mode, so point to the servlet 
      //defined in war/WEB-INF/web.xml
      return moduleUrl + "CsvDatasourceService";//$NON-NLS-1$
  }

  public void initializeBusinessData(BusinessData businessData) {
    datasourceModel.setDatasourceType(DatasourceType.NONE);

    SqlPhysicalModel model = (SqlPhysicalModel) businessData.getDomain().getPhysicalModels().get(0);
    String queryStr = model.getPhysicalTables().get(0).getTargetTable();
    //    datasourceModel.setDatasourceType(DatasourceType.SQL);
    datasourceModel.setDatasourceName(businessData.getDomain().getId());
    datasourceModel.setQuery(queryStr);
    for (IConnection conn : datasourceModel.getGuiStateModel().getConnections()) {
      if (model.getDatasource().getDatabaseName().equals(conn.getName())) {
        datasourceModel.setSelectedRelationalConnection(conn);
        break;
      }
    }
    datasourceModel.getGuiStateModel().setRelationalData(null);
    //columnFormatTreeCol.setEditable(true);
    datasourceModel.getGuiStateModel().setRelationalData(businessData.getData());

  }

  public boolean supportsBusinessData(BusinessData businessData) {
    return (businessData.getDomain().getPhysicalModels().get(0) instanceof SqlPhysicalModel);
  }

  public void setDatasourceMessages( DatasourceMessages datasourceMessages ) {
    this.messages = datasourceMessages;
  }

  public void setWaitingDialog( GwtWaitingDialog waitingDialog ) {
//    this.waitingDialog = waitingDialog;
  }

  public void setDatasourceModel( DatasourceModel datasourceModel ) {
    this.datasourceModel = datasourceModel;
  }

  @Bindable
  public void closeErrorDialog() {
    errorDialog.hide();
  }
  public void showErrorDialog(String message) {
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    successDialog.hide();
  }
  public void showSuccessDialog(String message) {
    successLabel.setValue(message);
    successDialog.show();
  }

  public boolean finishing() {
    syncModelInfo();
    return true;
  }

  public void syncModelInfo() {
  	String filename = datasourceModel.getModelInfo().getFileInfo().getFileName();
  	String tmpFilename  = datasourceModel.getModelInfo().getFileInfo().getTmpFilename();
  	
  	if((filename == null) || (filename != null && !tmpFilename.startsWith(filename))) { //creating a brand new ds || editing a ds having uploaded a new file
  		filename = tmpFilename; 
  	} else if(filename != null && tmpFilename.startsWith(filename)) { // editing a ds without uploading a new file
  		datasourceModel.getModelInfo().getFileInfo().setTmpFilename(filename);
  	} 
  
	if (filename != null) {    
    getModelService().getEncoding(filename, new AsyncCallback<String>()  {
    	public void onSuccess(String encoding) {
    		datasourceModel.getModelInfo().getFileInfo().setEncodingFromServer(encoding);
    		      try {
    		        // go get the file contents of the selected file
    		        getModelService().getPreviewRows(datasourceModel.getModelInfo().getFileInfo().getTmpFilename(),
    		            datasourceModel.getModelInfo().getFileInfo().getHeaderRows() > 0,
    		            10, encoding,
    		            new AsyncCallback<List<String>>()  {
	    		          public void onSuccess(List<String> lines) {
	    		            try {
	    		              datasourceModel.getModelInfo().getFileInfo().setContents(lines);
	
	    		              refreshPreview();
	    		            } catch (Exception e) {
	    		              GWT.log("Had an issue refreshing the data preview", e); //$NON-NLS-1$
	    		            }
	    		          }
	    		          public void onFailure(Throwable th) {
	    		            showErrorDialog(th.getMessage());
	    		          }
    		        });

    		      } catch (Exception e) {
    		        showErrorDialog(e.getMessage());
    		      }
    	}
    	public void onFailure(Throwable th) {
    		 GWT.log("Had an issue getting the encoding type", th); //$NON-NLS-1$
    	}
    });
    
    } else {
	      try {
	        refreshPreview();
	      } catch (Exception e) {
	        GWT.log("Had an issue refreshing the data preview", e);             //$NON-NLS-1$
	      }
	    }    		

    
  }

  private ICsvDatasourceServiceAsync getModelService() {
    if (csvModelService == null) {
      csvModelService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) csvModelService;
      endpoint.setServiceEntryPoint(getDatasourceURL());
    }
    return csvModelService;
  }
  
  @Bindable
  public void refreshPreview() throws Exception {
    csvTextPreview.setValue(""); //$NON-NLS-1$
    CsvFileInfo fileInfo = datasourceModel.getModelInfo().getFileInfo();
    csvTextPreview.setValue(fileInfo.formatSampleContents());
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
