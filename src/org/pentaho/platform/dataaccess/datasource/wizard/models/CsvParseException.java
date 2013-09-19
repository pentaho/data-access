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

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: Aug 11, 2010
 * Time: 3:50:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CsvParseException extends Exception implements Serializable {
  private static final long serialVersionUID = 1L;
  private int lineNumber = 0;
  private String offendingLine = "";
  private static final String I18N_MESSAGE_KEY = "CsvParseException.0001_FAILED_TO_PARSE_CSV";

  public CsvParseException() {
    this(0,null);
  }

  public CsvParseException(int lineNumber, String offendingLine) {
    super(I18N_MESSAGE_KEY);
    this.lineNumber = lineNumber;
    this.offendingLine = offendingLine;
  }

  public CsvParseException(int lineNumber, String offendingLine, Throwable cause) {
    super(I18N_MESSAGE_KEY, cause);
    this.lineNumber = lineNumber;
    this.offendingLine = offendingLine;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getOffendingLine() {
    return offendingLine;
  }

}
