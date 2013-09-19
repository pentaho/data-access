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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

/**
 * User: nbaker
 * Date: Aug 13, 2010
 */
public class DatasourceDTOUtil {

  public static DatasourceDTO generateDTO(DatasourceModel model){
    DatasourceDTO dto = new DatasourceDTO();
    dto.setDatasourceName(model.getDatasourceName());
    dto.setCsvModelInfo(model.getModelInfo());
    dto.setDatasourceType(model.getDatasourceType());
    dto.setQuery(model.getQuery());
    if(model.getSelectedRelationalConnection() != null){
    	dto.setConnectionName(model.getSelectedRelationalConnection().getName());
    }
    return dto;
  }

  public static void populateModel(DatasourceDTO dto, DatasourceModel model){
    model.setDatasourceName(dto.getDatasourceName());
    model.setModelInfo(dto.getCsvModelInfo());
    model.setDatasourceType(dto.getDatasourceType());
    model.setQuery(dto.getQuery());
    model.setSelectedRelationalConnection(model.getGuiStateModel().getConnectionByName(dto.getConnectionName()));
  }

}
