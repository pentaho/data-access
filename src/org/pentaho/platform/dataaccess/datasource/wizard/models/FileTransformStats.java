/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2009-2010 Pentaho Corporation.  All rights reserved.
 *
 * Created Sep, 2010
 * @author jdixon
 */
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;
import java.util.ArrayList;

public class FileTransformStats implements Serializable {

  private static final long serialVersionUID = 4741799513666255014L;

  private boolean rowsFinished = false;
  private boolean rowsStarted = false;
  private long rowsDone = 0;
  private long rowsRejected = 0;
  private int indexCount = 0;
  private boolean indexStarted = false;
  private boolean indexFinished = false;
  private int indexDone = 0;
  private int errors = 0;
  private transient DataRow[] rows = null;
  
  private ArrayList<String> csvInputErrors = new ArrayList<String>();
  private ArrayList<String> tableOutputErrors = new ArrayList<String>();
  private long csvInputErrorCount = 0;
  private long tableOutputErrorCount = 0;

  public FileTransformStats() {
  }
  
  public boolean isRowsFinished() {
    return rowsFinished;
  }
  public void setRowsFinished(boolean rowsFinished) {
    this.rowsFinished = rowsFinished;
  }
  public boolean isRowsStarted() {
    return rowsStarted;
  }
  public void setRowsStarted(boolean rowsStarted) {
    this.rowsStarted = rowsStarted;
  }
  public long getRowsDone() {
    return rowsDone;
  }
  public void setRowsDone(long rowsDone) {
    this.rowsDone = rowsDone;
  }
  public long getRowsRejected() {
    return rowsRejected;
  }
  public void setRowsRejected(long rowsRejected) {
    this.rowsRejected = rowsRejected;
  }  
  public int getIndexCount() {
    return indexCount;
  }
  public void setIndexCount(int indexCount) {
    this.indexCount = indexCount;
  }
  public boolean isIndexStarted() {
    return indexStarted;
  }
  public void setIndexStarted(boolean indexStarted) {
    this.indexStarted = indexStarted;
  }
  public boolean isIndexFinished() {
    return indexFinished;
  }
  public void setIndexFinished(boolean indexFinished) {
    this.indexFinished = indexFinished;
  }
  public int getIndexDone() {
    return indexDone;
  }
  public void setIndexDone(int indexDone) {
    this.indexDone = indexDone;
  }

  public int getErrors() {
    return errors;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }
  public ArrayList<String> getCsvInputErrors() {
    return csvInputErrors;
  }
  
  public void setCsvInputErrors(ArrayList<String> csvInputErrors) {
    this.csvInputErrors = csvInputErrors;
  }
  
  public ArrayList<String> getTableOutputErrors() {
    return tableOutputErrors;
  }
  
  public void setTableOutputErrors(ArrayList<String> tableOutputErrors) {
    this.tableOutputErrors = tableOutputErrors;
  }  

  public long getCsvInputErrorCount() {
    return csvInputErrorCount;
  }

  public void setCsvInputErrorCount(long csvInputErrorCount) {
    this.csvInputErrorCount = csvInputErrorCount;
  }

  public long getTableOutputErrorCount() {
    return tableOutputErrorCount;
  }

  public void setTableOutputErrorCount(long tableOutputErrorCount) {
    this.tableOutputErrorCount = tableOutputErrorCount;
  }

  public DataRow[] getDataRows() {
    return rows;
  }
  
  public void setDataRows( DataRow[] rows ) {
    this.rows = rows;
  }

}
