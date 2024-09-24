/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.sources.csv;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;

import java.io.Serializable;
import java.util.ArrayList;

public class FileTransformStats implements Serializable, IDatasourceSummary {

  private static final long serialVersionUID = 4741799513666255014L;

  private boolean rowsFinished = false;
  private boolean rowsStarted = false;
  private long totalRecords = 0;
  private long rowsRejected = 0;
  private int indexCount = 0;
  private boolean indexStarted = false;
  private boolean indexFinished = false;
  private int indexDone = 0;
  private ArrayList<String> errors = new ArrayList<String>();
  private transient DataRow[] rows = null;
  private long errorCount = 0;
  private Domain domain;
  private boolean showModeler;

  public FileTransformStats() {
  }

  public boolean isRowsFinished() {
    return rowsFinished;
  }

  public void setRowsFinished( boolean rowsFinished ) {
    this.rowsFinished = rowsFinished;
  }

  public boolean isRowsStarted() {
    return rowsStarted;
  }

  public void setRowsStarted( boolean rowsStarted ) {
    this.rowsStarted = rowsStarted;
  }

  public long getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords( long totalRecords ) {
    this.totalRecords = totalRecords;
  }

  public long getRowsRejected() {
    return rowsRejected;
  }

  public void setRowsRejected( long rowsRejected ) {
    this.rowsRejected = rowsRejected;
  }

  public int getIndexCount() {
    return indexCount;
  }

  public void setIndexCount( int indexCount ) {
    this.indexCount = indexCount;
  }

  public boolean isIndexStarted() {
    return indexStarted;
  }

  public void setIndexStarted( boolean indexStarted ) {
    this.indexStarted = indexStarted;
  }

  public boolean isIndexFinished() {
    return indexFinished;
  }

  public void setIndexFinished( boolean indexFinished ) {
    this.indexFinished = indexFinished;
  }

  public int getIndexDone() {
    return indexDone;
  }

  public void setIndexDone( int indexDone ) {
    this.indexDone = indexDone;
  }

  public void setErrors( ArrayList<String> errors ) {
    this.errors = errors;
  }

  public ArrayList<String> getErrors() {
    return errors;
  }

  public long getErrorCount() {
    if ( errorCount > 0 ) {
      return errorCount;
    }
    if ( errors != null ) {
      return errors.size();
    } else {
      return 0;
    }
  }

  public void setErrorCount( long errorCount ) {
    this.errorCount = errorCount;
  }

  public DataRow[] getDataRows() {
    return rows;
  }

  public void setDataRows( DataRow[] rows ) {
    this.rows = rows;
  }

  @Override
  public Domain getDomain() {
    return domain;
  }

  public void setDomain( Domain domain ) {
    this.domain = domain;
  }

  @Override
  public void setShowModeler( boolean b ) {
    this.showModeler = b;
  }

  public boolean isShowModeler() {
    return showModeler;
  }

}
