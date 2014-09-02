/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;

public class PdiTransListener implements TransListener, Runnable {

  private StepInterface step;

  private Trans trans;

  private FileTransformStats transformStats;

  private boolean finished = false;

  private long rowsDone = 0;

  public PdiTransListener( Trans trans, StepInterface step, FileTransformStats transformStats ) {
    this.step = step;
    this.trans = trans;
    this.transformStats = transformStats;
  }

  public void cancel() {
    finished = true;
  }

  public boolean isFinished() {
    return finished;
  }

  public void run() {

    while ( !finished && !trans.isFinished() ) {
      try {
        rowsDone = step.getLinesOutput();
        transformStats.setTotalRecords( rowsDone );
        Thread.sleep( 250 );
      } catch ( InterruptedException e ) {
        // swallow this
      }
    }
    doFinish();
  }

  private void doFinish() {
    try {
      trans.cleanup();
    } catch ( Exception e ) {
      //Do Nothing
    }
    transformStats.setRowsFinished( true );
    transformStats.setTotalRecords( step.getLinesRead() );

    // there seems to be an issue with trans.getErrors() reporting 0 - figure it out on our own instead
    //    transformStats.setErrorCount(errorCount);
    transformStats.setErrorCount( step.getLinesRead() - step.getLinesWritten() );

    finished = true;
  }

  public void transFinished( Trans trans ) {
    doFinish();
  }

  public void transActive( Trans trans ) {

  }

  public void transIdle( Trans trans ) {

  }

  public void transStarted( Trans trans ) {

  }
}
