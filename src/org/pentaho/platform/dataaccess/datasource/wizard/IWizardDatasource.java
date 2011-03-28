package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;

import java.util.List;

/**
 * User: nbaker
 * Date: 3/22/11
 */
public interface IWizardDatasource extends XulEventSource {

  /**
   * Localized name of the Datasource.
   * @return
   */
  String getName();

  /**
   * Return a list of steps to be added to the wizard when the datasource is activated.
   * @return
   */
  List<IWizardStep> getSteps();


  /**
   * @return a boolean that determines if the "Finish" button should be enabled.
   */
  public boolean isFinishable();


   /**
   * Flags a datasource as finishable. This needs to be fired as a propertyChangeEvent from the implementing Datasource
   * So the Main Controller will pickup the change.
   */
    public void setFinishable(boolean isFinishable);
    
  /**
   * Called when the Wizard is finished.
   * @param callback gets called with a summary of the results.
   */
  void onFinish(XulServiceCallback<IDatasourceSummary> callback);

  /**
   * Called when the datasource is becoming active (selected in the UI). At this time datasource steps will be
   * added to the IWizardController. Steps should be "cleared" when this method is called
   */
  void activating() throws XulException;

  /**
   * Step controllers should be initialized with bindings created at this time.
   */
  void init(XulDomContainer container) throws XulException;


  /**
   * Called when the datasource is deactivating (de-selected in the UI). All steps will be removed from the
   * IWizardController
   */
  void deactivating();

  String getId();

}
