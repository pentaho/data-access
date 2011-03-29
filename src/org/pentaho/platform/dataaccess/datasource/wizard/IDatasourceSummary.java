package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.Domain;

import java.io.Serializable;
import java.util.List;

/**
 * User: nbaker
 * Date: 3/22/11
 */
public interface IDatasourceSummary extends Serializable {
  long getErrorCount();
  long getTotalRecords();
  List<String> getErrors();
  Domain getDomain();
  void setDomain(Domain domain);

  void setShowModeler(boolean b);

  boolean isShowModeler();
}
