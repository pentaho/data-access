package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulExpandPanel;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.tags.GwtRadioGroup;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * User: nbaker
 * Date: 3/22/11
 */
public class SummaryDialogController extends AbstractXulEventHandler {

  private XulDialog wizardDialog;

  private GwtRadioGroup modelerDecision;

  private XulExpandPanel errorLogExpander;

  private XulDialog summaryDialog;
  private XulLabel summaryDialogRowsLoaded;
  private XulVbox showModelerCheckboxHider;
  private XulLabel summaryDialogDetails;
  private IDatasourceSummary summary;
  private XulServiceCallback<IDatasourceSummary> callback;
  private BindingFactory bf;

  @Override
  public String getName() {
    return "summaryDialog";
  }

  public void init(){
    wizardDialog = (XulDialog) document.getElementById("main_wizard_window");
    modelerDecision = (GwtRadioGroup) document.getElementById("modelerDecision");
    errorLogExpander = (XulExpandPanel) document.getElementById("errorLogExpander");
    showModelerCheckboxHider = (XulVbox) document.getElementById("showModelerCheckboxHider");

    summaryDialog = (XulDialog) document.getElementById("summaryDialog");
    summaryDialogRowsLoaded = (XulLabel) document.getElementById("summaryDialogRowsLoaded");
    summaryDialogDetails = (XulLabel) document.getElementById("summaryDialogDetails");
  }

  public void showSummaryDialog(IDatasourceSummary stats, XulServiceCallback<IDatasourceSummary> callback){
    summary = stats;
    this.callback = callback;

    wizardDialog.hide();

    errorLogExpander.setExpanded(false);
    modelerDecision.setValue("DEFAULT");

    // only show csv related stuff if it is a csv data source (it will have stats)
    if (stats != null && stats.getErrorCount() > 0) {
      long errors = stats.getErrorCount();
      long total = stats.getTotalRecords() > 0 ? stats.getTotalRecords() : errors;

      long successRows = total > errors ? total - errors : 0;

      summaryDialogRowsLoaded.setValue(MessageHandler.getInstance().messages.getString("summaryDialog.rowsLoaded", String.valueOf(successRows), String.valueOf(total)));
      String lf = "\n";
      if (errors > 0) {
        StringBuilder detailMsg = new StringBuilder();
        for (String error : stats.getErrors()) {
          detailMsg.append(error);
          detailMsg.append(lf);
        }
        summaryDialogDetails.setValue(detailMsg.toString());
        errorLogExpander.setVisible(true);
      } else {
        summaryDialogDetails.setValue("");
        errorLogExpander.setVisible(false);
      }

    } else {
      summaryDialogRowsLoaded.setValue(MessageHandler.getInstance().messages.getString("summaryDialog.generalSuccess"));
      errorLogExpander.setVisible(false);
    }

    //TODO: handle modeler checkbox visibility
    //showModelerCheckboxHider.setVisible(!datasourceModel.getGuiStateModel().isEditing());
    showModelerCheckboxHider.setVisible(true);
    summaryDialog.show();
  }

  @Bindable
  public void closeSummaryDialog() {
    summaryDialog.hide();
    boolean editModeler = modelerDecision.getValue() != null && modelerDecision.getValue().equals("EDIT");
    if (editModeler) {
      MessageHandler.getInstance().showWaitingDialog(MessageHandler.getInstance().messages.getString(MessageHandler.MSG_OPENING_MODELER));
      summary.setShowModeler(true);
    } else {
      MessageHandler.getInstance().showWaitingDialog(MessageHandler.getInstance().messages.getString(MessageHandler.MSG_GENERAL_WAIT));
      summary.setShowModeler(false);
    }

    errorLogExpander.setExpanded(false);
    this.callback.success(summary);
  }

  public void setBindingFactory(BindingFactory bindingFactory) {
    bf = bindingFactory;
  }
}
