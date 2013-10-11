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
