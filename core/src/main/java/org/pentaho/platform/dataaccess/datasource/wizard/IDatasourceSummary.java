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



package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.Domain;

import java.io.Serializable;
import java.util.List;

/**
 * User: nbaker Date: 3/22/11
 */
public interface IDatasourceSummary extends Serializable {
  long getErrorCount();

  long getTotalRecords();

  List<String> getErrors();

  Domain getDomain();

  void setDomain( Domain domain );

  void setShowModeler( boolean b );

  boolean isShowModeler();
}
