package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class ColumnInfoCollection extends AbstractModelList<ColumnInfo> {

  private int selectedCount = 0;

  private PropertyChangeListener selectedListener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent evt) {
      if(evt.getPropertyName().equals("include") || evt.getPropertyName().equals("children")){
        int count = 0;
        for(ColumnInfo ci : getChildren()) {
          if (ci.isInclude()) {
            count++;
          }
        }
        setSelectedCount(count);
      }
    }
  };

  private void setSelectedCount(int count) {
    int prev = selectedCount;
    selectedCount = count;
    firePropertyChange("selectedCount", prev, count);
  }

  @Bindable
  public int getSelectedCount() {
    return selectedCount;
  }

  public ColumnInfoCollection(){

  }

  public void onAdd(ColumnInfo child) {
    child.addPropertyChangeListener(selectedListener);
  }

  public void onRemove(ColumnInfo child) {
    child.removePropertyChangeListener(selectedListener);
  }
}
