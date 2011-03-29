package org.pentaho.platform.dataaccess.datasource.wizard.sources.query;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTOUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
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
  public QueryDatasource(DatasourceModel datasourceModel){
    this.datasourceModel = datasourceModel;
    datasourceService = new DatasourceServiceGwtImpl();
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
    String connectionName = datasourceModel.getSelectedRelationalConnection().getName();
       
    datasourceService.generateQueryDomain(name, query, connectionName, DatasourceDTOUtil.generateDTO(datasourceModel), callback);
  }

  @Override
  public void init(XulDomContainer container) throws XulException {
    queryStep = new QueryPhysicalStep(datasourceModel, this);
    container.addEventHandler(queryStep);
    queryStep.init();
  }


  @Override
  public String getId() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
