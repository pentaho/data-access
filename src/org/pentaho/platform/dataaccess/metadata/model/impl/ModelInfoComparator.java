package org.pentaho.platform.dataaccess.metadata.model.impl;

import java.util.Comparator;

/**
 * compares two model info objects so that they can be sorted by name
 * @author jamesdixon
 *
 */
public class ModelInfoComparator implements Comparator {

  @Override
  public int compare(Object obj1, Object obj2) {
    ModelInfo model1 = (ModelInfo) obj1;
    ModelInfo model2 = (ModelInfo) obj2;
    return model1.getModelName().compareTo(model2.getModelName());
  }

}
