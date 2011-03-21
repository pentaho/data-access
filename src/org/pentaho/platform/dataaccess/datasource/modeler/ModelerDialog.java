package org.pentaho.platform.dataaccess.datasource.modeler;

import com.google.gwt.user.client.Window;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.gwt.GwtModelerMessages;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.propforms.*;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

/**
 * User: nbaker
 * Date: Aug 10, 2010
 */
public class ModelerDialog extends AbstractXulDialogController<Domain> implements IXulLoaderCallback {

  private XulDomContainer container;
  private DialogListener<Domain> listener;
  private IModelerServiceAsync service = new GwtModelerServiceImpl();

  private ModelerWorkspace model = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
  private ModelerController controller;
  private IModelerMessages messages;
  private XulDialog errorDialog;
  private XulDialog waitDialog;

  private EmbeddedWizard wizard;
  private ModelerDialog modeler;
  private IXulAsyncConnectionService connectionService;
  private IXulAsyncDatasourceService datasourceService;
  private AsyncConstructorListener constructorListener;
  private static ModelerDialog instance;

  private ModelerDialog(final AsyncConstructorListener<ModelerDialog> constructorListener){
    this(null, constructorListener);
  }

  private ModelerDialog(EmbeddedWizard wizard, final AsyncConstructorListener<ModelerDialog> constructorListener){
    this.wizard = wizard;
    this.constructorListener = constructorListener;
    AsyncXulLoader.loadXulFromUrl("modeler.xul", "modeler", this);
  }


  public static ModelerDialog getInstance(final AsyncConstructorListener<ModelerDialog> constructorListener){
    if(instance != null){
      constructorListener.asyncConstructorDone(instance);
      return instance;
    }
    instance = new ModelerDialog(constructorListener);
    return instance;
  }

  public static ModelerDialog getInstance(EmbeddedWizard wizard, final AsyncConstructorListener<ModelerDialog> constructorListener){
    if(instance != null){
      constructorListener.asyncConstructorDone(instance);
      return instance;
    }
    instance = new ModelerDialog(wizard, constructorListener);
    return instance;
  }



  @Override
  protected XulDialog getDialog() {
    return (XulDialog) container.getDocumentRoot().getElementById("modeler_dialog");
  }

  @Override
  protected Domain getDialogResult() {
    return null;
  }

  @Bindable
  public void onAccept(){
    enableWaitCursor(true);
    try {
      model.getWorkspaceHelper().populateDomain(model);
    } catch (ModelerException e) {
      e.printStackTrace();
      showErrorDialog(messages.getString("ModelEditor.ERROR"),
          messages.getString("ModelEditor.ERROR_0001_SAVING_MODELS"));
    }
    service.serializeModels(model.getDomain(), model.getModelName(), new XulServiceCallback<String>(){
      public void success(String retVal) {
        enableWaitCursor(false);
        hideDialog();
        model.getDomain().setId(retVal);
        ModelerDialog.this.listener.onDialogAccept(model.getDomain());
      }

      public void error(String message, Throwable error) {
        enableWaitCursor(false);
        showErrorDialog(messages.getString("ModelEditor.ERROR"),
          messages.getString("ModelEditor.ERROR_0001_SAVING_MODELS"));
      }
    });
  }


  protected void showErrorDialog(String title, String message) {
    errorDialog = (XulDialog) container.getDocumentRoot().getElementById("errorDialog");
    XulLabel errorLabel = (XulLabel) container.getDocumentRoot().getElementById("errorLabel");
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void onLoad(){
  }

  public void xulLoaded(GwtXulRunner gwtXulRunner) {
    container = gwtXulRunner.getXulDomContainers().get(0);
    container.addEventHandler(this);

    BogoPojo bogo = new BogoPojo();
    service.gwtWorkaround(bogo, new XulServiceCallback<BogoPojo>(){
      public void success(BogoPojo retVal) {

      }

      public void error(String message, Throwable error) {

      }
    });

    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    if(wizard == null){
      wizard = new EmbeddedWizard(datasourceService, connectionService, null, false);
    }

    messages = new GwtModelerMessages((ResourceBundle) container.getResourceBundles().get(0));
    try{
      ModelerMessagesHolder.setMessages(messages);
    } catch(Exception ignored){
      // Messages may have been set earlier, ignore.
    }

    GwtModelerWorkspaceHelper workspacehelper = new GwtModelerWorkspaceHelper();

    controller = new ModelerController(model);

    controller.setWorkspaceHelper(workspacehelper);
//    controller.setMessages(messages);
    BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());
    controller.setBindingFactory(bf);
    container.addEventHandler(controller);
    try{
      controller.init();
    } catch(ModelerException e){
      e.printStackTrace();
    }

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(model.getModel(), "valid", "modeler_dialog_accept", "disabled", new BindingConvertor<Boolean, Boolean>(){
      @Override
      public Boolean sourceToTarget(Boolean value) {
        return !value;
      }

      @Override
      public Boolean targetToSource(Boolean value) {
        return !value;
      }
    });

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    AbstractModelerNodeForm propController = new MeasuresPropertiesForm(workspacehelper.getLocale());
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new DimensionPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new LevelsPropertiesForm(workspacehelper.getLocale());
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();


    propController = new HierarchyPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new MainModelerNodePropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();


    propController = new GenericPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    ColResolverController colController = new ColResolverController();
    container.addEventHandler(colController);
    controller.setColResolver(colController);
    colController.init();

    waitDialog = (XulDialog) document.getElementById("waitingDialog");
    this.constructorListener.asyncConstructorDone(this);
  }

  public void overlayLoaded() {
  }

  public void overlayRemoved() {
  }

  public void showDialog(Domain domain) {
    enableWaitCursor(true);
    model.setDomain(domain);
    controller.resetPropertyForm();
    showDialog();
    enableWaitCursor(false);
  }

  public void showDialog(String domainId, String modelId) {
    enableWaitCursor(true);
    service.loadDomain(domainId, new XulServiceCallback<Domain>(){
      public void success(Domain retVal) {
        model.setDomain(retVal);
        controller.resetPropertyForm();
        enableWaitCursor(false);
        showDialog();
      }

      public void error(String message, Throwable error) {
        enableWaitCursor(false);
        showErrorDialog(messages.getString("ModelEditor.ERROR"),
          messages.getString("ModelEditor.ERROR_0002_LOADING_DOMAIN"));
      }
    });
  }

  private void enableWaitCursor(final boolean enable) {
    if (enable) {
      waitDialog.show();
    } else {
      waitDialog.hide();
    }
  }

  @Override
  public String getName() {
    return "modelerDialogController";
  }

  @Bindable
  public void closeErrorDialog(){
    errorDialog.hide();
  }

  @Bindable
  public void onCancel(){
    hideDialog();
  }

  @Bindable
  public void onEditSource() {
    showEditSourceDialog();
  }

  public void showEditSourceDialog() {
    wizard.showEditDialog(model.getDomain(), new XulServiceCallback<Domain>() {
      public void success(Domain domain) {
        try {
          model.refresh(domain);
        } catch (ModelerException e) {
          showErrorDialog("Error", e.getMessage());
        }
      }

      public void error(String s, Throwable throwable) {
        showErrorDialog("Error", s);
        throwable.printStackTrace();
      }
    });
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener) {
//    checkInitialized();
    super.addDialogListener(listener);
    this.listener = listener;
    listener.onDialogReady();
  }


}
