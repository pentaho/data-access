package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.DummyDatasource;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.*;

/**
 * User: nbaker
 * Date: 3/30/11
 */
public class WizardModel extends XulEventSourceAdapter implements IWizardModel {
  private LinkedHashSet<IWizardDatasource> datasources = new LinkedHashSet<IWizardDatasource>();
  private String datasourceName;
  private boolean editing;
  private List<Class<? extends IWizardDatasource>> ignoredDatasources = new ArrayList<Class<? extends IWizardDatasource>>();
  private IWizardDatasource selectedDatasource;

  public WizardModel(){
    addDatasource(new DummyDatasource());
  }

  @Override
  @Bindable
  public String getDatasourceName() {
    return datasourceName;
  }

  @Override
  @Bindable
    public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  @Override
  @Bindable
  public Set getDatasources() {
    return datasources;
  }

  @Override
  public void addDatasource(IWizardDatasource datasource){
    // due to initialization order, datasources may be added after a call to remove them by type (cleaning out built-ins)
    if(ignoredDatasources.contains(datasource.getClass())){
      return;
    }
    boolean reallyAdded = this.datasources.add(datasource);
    if(reallyAdded)
      firePropertyChange("datasources", null, datasources);
    if(selectedDatasource == null){
      setSelectedDatasource(datasources.iterator().next());
    }
  }

  @Bindable
  public void setSelectedDatasource(IWizardDatasource datasource) {
    IWizardDatasource prevSelection = selectedDatasource;
    selectedDatasource = datasource;
    firePropertyChange("selectedDatasource", prevSelection, selectedDatasource);
  }

  @Bindable
  public IWizardDatasource getSelectedDatasource(){
    return selectedDatasource;
  }

  @Override
  public void removeDatasourceByType(Class<? extends IWizardDatasource> datasource){
    ignoredDatasources.add(datasource);
    IWizardDatasource matchedSource = null;
    for(IWizardDatasource source : datasources){
      if(source.getClass().equals(datasource)){
        matchedSource = source;
        break;
      }
    }
    if(matchedSource != null){
      datasources.remove(matchedSource);
    }
  }

  @Override
  public boolean isEditing() {
    return editing;
  }
  @Override
  public void setEditing(boolean editing){
    this.editing = editing;
    firePropertyChange("editing", !this.editing, this.editing);
  }

  @Override
  public void reset() {
    this.setDatasourceName("");
    this.setSelectedDatasource(datasources.iterator().next());
    for(IWizardDatasource source : datasources){
      source.reset();
    }
  }
}
