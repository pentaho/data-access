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

package org.pentaho.platform.dataaccess.datasource.wizard.service;

/**
 * This class provides the structure for individual templates providing in the template listing service.
 * 
 * @author tkafalas
 * 
 */
public class DSWTemplateSummaryDto {
  String templateID;
  String displayName;

  public DSWTemplateSummaryDto() {

  }

  public DSWTemplateSummaryDto( String templateID, String displayName ) {
    this.templateID = templateID;
    this.displayName = displayName;
  }

  /**
   * @return the templateName
   */
  public String getTemplateID() {
    return templateID;
  }

  /**
   * @param templateName
   *          the templateName to set
   */
  public void setTemplateID( String templateID ) {
    this.templateID = templateID;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName
   *          the displayName to set
   */
  public void setDisplayName( String displayName ) {
    this.displayName = displayName;
  }

}
