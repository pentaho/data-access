package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * User: nbaker
 * Date: 3/21/11
 */
public class MessageHandler extends AbstractXulEventHandler {

  private XulDialog waitingDialog = null;
  private XulLabel waitingLabel = null;
  private XulDialog errorDialog;
  private XulDialog errorDetailsDialog;
  private DatasourceMessages messages;

  private XulDialog successDialog = null;

  private XulLabel successLabel = null;

  private XulDialog wizardDialog;
  
  private boolean showWizardDialog;

  public static final String MSG_OPENING_MODELER = "waiting.openingModeler";
  public static final String MSG_GENERAL_WAIT = "waiting.generalWaiting";
  public static final String MSG_STAGING_DATA = "physicalDatasourceDialog.STAGING_DATA"; //$NON-NLS-1$
  public static final String MSG_CREATING_DATA_SOURCE = "waiting.creatingDataSource"; //$NON-NLS-1$
  private static MessageHandler INSTANCE = new MessageHandler();

  private MessageHandler(){
  }

  @Override
  public String getName() {
    return "messageHandler";
  }

  public void init(){

    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorDetailsDialog = (XulDialog) document.getElementById("errorDetailsDialog"); //$NON-NLS-1$
      waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
      waitingLabel = (XulLabel) document.getElementById("waitingDialogLabel"); //$NON-NLS-1$

    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel"); //$NON-NLS-1$
  }


  public void showErrorDialog(String message) {
    showErrorDialog(messages.getString("error"), message);
  }
  
  public void showErrorDialog(String title, String message) {
	  showErrorDialog(title, message, false);
  }

  public void showErrorDialog(String title, String message, boolean showWizardDialog) {
	  this.showWizardDialog = showWizardDialog; 
	  XulDialog errorDialog = (XulDialog) document.getElementById("errorDialog");
	  errorDialog.setTitle(title);

	  XulLabel errorLabel = (XulLabel) document.getElementById("errorLabel");
	  errorLabel.setValue(message);

	  errorDialog.show();
	  
  }
  
  public void showErrorDetailsDialog(String title, String message, String detailMessage) {
	  XulDialog errorDialog = (XulDialog) document.getElementById("errorDetailsDialog");
	  errorDialog.setTitle(title);

	  XulLabel errorLabel = (XulLabel) document.getElementById("errorDetailsLabel");
	  errorLabel.setValue(message);

	  XulLabel detailMessageBox = (XulLabel) document.getElementById("error_dialog_details");
	  detailMessageBox.setValue(detailMessage);

	  errorDialog.show();  
  }  
  
  @Bindable
  public void closeSuccessDetailsDialog() {
    XulDialog detailedSuccessDialog = (XulDialog) document.getElementById("successDetailsDialog");
    detailedSuccessDialog.hide();
  }
  
  @Bindable
  public void showDetailedSuccessDialog(String message, String detailMessage) {
    XulDialog detailedSuccessDialog = (XulDialog) document.getElementById("successDetailsDialog");

    XulLabel successLabel = (XulLabel) document.getElementById("success_details_label");
    successLabel.setValue(message);
    
    XulTextbox detailMessageBox = (XulTextbox) document.getElementById("success_dialog_details");
    detailMessageBox.setValue(detailMessage);

    detailedSuccessDialog.show();
  }  

  @Bindable
  public void closeSuccessDialog() {
    successDialog.hide();
  }

  public void showSuccessDialog(String message) {
    successLabel.setValue(message);
    successDialog.show();
  }


  public void showWaitingDialog() {
    showWaitingDialog(messages.getString(MSG_CREATING_DATA_SOURCE));
  }
  public void showWaitingDialog(String msg) {
    waitingLabel.setValue(msg);
    waitingDialog.show();
  }
  public void closeWaitingDialog() {
    waitingDialog.hide();
  }
  

  @Bindable
  public void closeErrorDetailsDialog() {
    if (!errorDetailsDialog.isHidden()) {
      errorDetailsDialog.hide();
    }
   	wizardDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
    if(this.showWizardDialog) {
    	wizardDialog.show();
    }
  }


  public void setMessages(DatasourceMessages messages) {
    this.messages = messages;
  }

  public void setWizardDialog(XulDialog wizardDialog) {
    this.wizardDialog = wizardDialog;
  }

  public static MessageHandler getInstance() {
    return INSTANCE;
  }

  public static String getString(String key){
    return getInstance().messages.getString(key);
  }

  public static String getString(String key, String... params){
    return getInstance().messages.getString(key, params);
  }
}
