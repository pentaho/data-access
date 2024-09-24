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

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.util.DialogController;

public interface IDatasourceEditor extends DialogController<Domain> {
  public void showEditDialog( final String domainId, final String modelId );
}
