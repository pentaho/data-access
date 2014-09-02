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

package org.pentaho.platform.dataaccess.metadata.model;

import java.io.Serializable;

/**
 * Represents a Metadata Column object
 */
public interface IColumn extends Serializable {

  /**
   * Returns the data type of the column
   *
   * @return
   */
  public String getType();

  /**
   * Returns the id of the column
   *
   * @return
   */
  public String getId();

  /**
   * Returns the name of the column
   *
   * @return
   */
  public String getName();

  /**
   * Returns the category of the column
   *
   * @return
   */
  public String getCategory();

  /**
   * Returns the default aggregation type of the column. Can be NONE.
   *
   * @return
   */
  public String getDefaultAggType();

  /**
   * Returns an array of the availble aggregation types. Can be empty.
   *
   * @return
   */
  public String[] getAggTypes();

  /**
   * Returns the currently selected aggregation type. Can be NONE.
   *
   * @return
   */
  public String getSelectedAggType();

  /**
   * Returns the type of this column, e.g. DIMENSION. Can be UNKNOWN.
   *
   * @return
   */
  public String getFieldType();

  /**
   * Returns the horizontal alignment of the column - RIGHT, LEFT, CENTERED.
   *
   * @return
   */
  public String getHorizontalAlignment();

  /**
   * Returns the format mask for this column, e.g. "#,###.00" or "MMM dd, yyyy"
   *
   * @return
   */
  public String getFormatMask();
}
