package org.pentaho.platform.dataaccess.datasource.wizard.sources.query;

import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.util.List;

/**
 * User: nbaker
 * Date: 3/26/11
 */
public class QueryDatasource extends AbstractXulEventHandler implements IWizardDatasource {
  private boolean finishable;

  @Override
  public void activating() throws XulException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public List<IWizardStep> getSteps() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void onFinish(XulServiceCallback<IDatasourceSummary> callback) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void init(XulDomContainer container) throws XulException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void deactivating() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getId() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isFinishable() {
    return finishable;
  }

  @Override
  public void setFinishable(boolean isFinishable) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
