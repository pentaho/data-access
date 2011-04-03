package org.pentaho.platform.dataaccess.datasource.wizard.sources.query;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.*;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.Collections;
import java.util.List;

/**
 * User: nbaker
 * Date: 3/26/11
 */
public class QueryDatasource extends AbstractXulEventHandler implements IWizardDatasource {
  private boolean finishable;
  private QueryPhysicalStep queryStep;
  private DatasourceModel datasourceModel;
  private IXulAsyncDatasourceService datasourceService;
  private IWizardModel wizardModel;

  public QueryDatasource(IXulAsyncDatasourceService datasourceService, DatasourceModel datasourceModel){
    this.datasourceModel = datasourceModel;
    this.datasourceService = datasourceService;
  }
  @Override
  public void activating() throws XulException {
    queryStep.activating();
  }

  @Override
  public void deactivating() {
    queryStep.deactivate();
  }

  @Override
  @Bindable
  public String getName() {
    return "SQL Query"; //TODO: i18n
  }

  @Override
  public List<IWizardStep> getSteps() {
    return Collections.singletonList((IWizardStep) queryStep);
  }

  @Override
  public void onFinish(XulServiceCallback<IDatasourceSummary> callback) {

    String name = datasourceModel.getDatasourceName().replace(".", "_").replace(" ", "_");
    String query = datasourceModel.getQuery();

       
    datasourceService.generateQueryDomain(name, query, getId(), datasourceModel.getSelectedRelationalConnection(), DatasourceDTOUtil.generateDTO(datasourceModel), callback);
  }

  @Override
  public void init(XulDomContainer container, IWizardModel wizardModel) throws XulException {
    this.wizardModel = wizardModel;
    queryStep = new QueryPhysicalStep(datasourceModel, this);
    container.addEventHandler(queryStep);
    queryStep.init(wizardModel);
  }


  @Override
  public String getId() {
    return "SQL-DS";  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  @Bindable
  public boolean isFinishable() {
    return finishable;
  }

  public void setFinishable(boolean finishable){
    boolean prevFinishable = this.finishable;
    this.finishable = finishable;
    firePropertyChange("finishable", prevFinishable, finishable);
  }

  @Override
  public void restoreSavedDatasource(Domain previousDomain, final XulServiceCallback<Void> callback) {

    String serializedDatasource = (String) previousDomain.getLogicalModels().get(0).getProperty("datasourceModel");

    datasourceService.deSerializeModelState(serializedDatasource, new XulServiceCallback<DatasourceDTO>() {
      public void success(DatasourceDTO datasourceDTO) {
        DatasourceDTO.populateModel(datasourceDTO, datasourceModel);
        datasourceModel.getGuiStateModel().setDirty(false);
        // initialize connections
        if (datasourceModel.getGuiStateModel().getConnections() == null
            || datasourceModel.getGuiStateModel().getConnections().size() <= 0) {
          queryStep.reloadConnections();
        }
        wizardModel.setEditing(true);

        callback.success(null);
      }

      public void error(String s, Throwable throwable) {
        MessageHandler.getInstance().showErrorDialog(MessageHandler.getString("ERROR"), MessageHandler.getString(
            "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", throwable.getLocalizedMessage()));
        
        callback.error(s, throwable);
      }
    });
  }

  @Override
  public void reset() {
    datasourceModel.clearModel();
  }
}
