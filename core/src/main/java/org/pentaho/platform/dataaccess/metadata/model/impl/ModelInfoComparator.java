/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.dataaccess.metadata.model.impl;

import java.util.Comparator;

/**
 * compares two model info objects so that they can be sorted by name
 *
 * @author jamesdixon
 */
public class ModelInfoComparator implements Comparator<ModelInfo> {

  @Override
  public int compare( ModelInfo model1, ModelInfo model2 ) {
    return model1.getModelName().compareTo( model2.getModelName() );
  }

}
