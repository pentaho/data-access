package org.pentaho.platform.dataaccess.datasource.wizard.sources.query;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;

import java.util.Collections;
import java.util.List;

/**
 * User: nbaker
 * Date: 3/28/11
 */
public class QueryDatasourceSummary implements IDatasourceSummary {

  private Domain domain;
  private boolean showModeler;
  private boolean reportingOnly ;

  public QueryDatasourceSummary(){
    
  }
  @Override
  public Domain getDomain() {
    return domain;
  }

  @Override
  public long getErrorCount() {
    return 0;
  }

  @Override
  public long getTotalRecords() {
    return 0;
  }

  @Override
  public List<String> getErrors() {
    return Collections.emptyList();
  }

  @Override
  public void setDomain(Domain domain) {
    this.domain = domain;
  }

  @Override
  public void setShowModeler(boolean b) {
    showModeler = b;
  }

  @Override
  public boolean isShowModeler() {
    return showModeler;
  }
}
