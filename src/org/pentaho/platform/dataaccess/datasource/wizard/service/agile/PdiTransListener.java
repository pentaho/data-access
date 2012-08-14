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

    while( !finished && !trans.isFinished() ) {
      try {
        rowsDone = step.getLinesOutput();
        transformStats.setTotalRecords(rowsDone);
        Thread.sleep(250);
      } catch (InterruptedException e) {
        // swallow this
      }
    }
    doFinish();
  }

  private void doFinish() {
    int errorCount = trans.getErrors();
    try {
      trans.cleanup();
    } catch (Exception e) {
    }
    transformStats.setRowsFinished(true);
    transformStats.setTotalRecords(step.getLinesRead());

    // there seems to be an issue with trans.getErrors() reporting 0 - figure it out on our own instead
//    transformStats.setErrorCount(errorCount);
    transformStats.setErrorCount(step.getLinesRead() - step.getLinesWritten());

    finished = true;
  }
  
  public void transFinished(Trans trans) {
    doFinish();
  }

  public void transActive(Trans trans) {

  }

  public void transIdle(Trans trans) {

  }

  public void transStarted(Trans trans) {

  }
}
