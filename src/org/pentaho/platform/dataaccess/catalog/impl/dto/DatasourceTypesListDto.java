package org.pentaho.platform.dataaccess.catalog.impl.dto;

/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.catalog.impl.DatasourceType;

/**
 * This class provides the list structure of DatasourceTypes for use in the DatasourceCatalog services.
 *
 * @author wseyler
 */
@XmlRootElement( name = "datasourceList" )
public class DatasourceTypesListDto {

  public List<DatasourceType> datasourceTypes;

  DatasourceTypesListDto() {
  }

  public DatasourceTypesListDto( List<IDatasourceType> datasourceTypes ) {
    this.datasourceTypes = new ArrayList<DatasourceType>();
    for ( IDatasourceType iDatasourceType : datasourceTypes ) {
      this.datasourceTypes.add( (DatasourceType) iDatasourceType );
    }
  }
}
