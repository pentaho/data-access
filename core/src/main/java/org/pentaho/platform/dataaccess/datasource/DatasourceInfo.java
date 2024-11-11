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


package org.pentaho.platform.dataaccess.datasource;


import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceInfo extends XulEventSourceAdapter implements IDatasourceInfo {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static final String MSG_PREFIX = "DatasourceInfo.DisplayType.";

  String name;

  String id;

  String type;

  String displayType;

  boolean editable;

  boolean removable;

  boolean importable;

  boolean exportable;

  boolean creatable;

  static transient DatasourceMessages messageBundle;


  public DatasourceInfo() {
    super();
    // TODO Auto-generated constructor stub
  }

  public DatasourceInfo( String name, String id, String type ) {
    super();
    this.name = name;
    this.id = id;
    this.type = type;
  }

  public DatasourceInfo( String name, String id, String type, boolean editable, boolean removable, boolean importable,
                         boolean exportable ) {
    super();
    this.name = name;
    this.id = id;
    this.type = type;
    this.editable = editable;
    this.removable = removable;
    this.importable = importable;
    this.exportable = exportable;
    this.creatable = true;
  }

  public DatasourceInfo( String name, String id, String type, boolean editable, boolean removable, boolean importable,
                         boolean exportable, boolean creatable ) {
    super();
    this.name = name;
    this.id = id;
    this.type = type;
    this.editable = editable;
    this.removable = removable;
    this.importable = importable;
    this.exportable = exportable;
    this.creatable = creatable;
  }

  @Bindable
  @Override
  public String getName() {
    return SafeHtmlUtils.htmlEscape( name );
  }

  @Bindable
  @Override
  public String getDisplayType() {
    if ( displayType != null ) {
      return displayType;
    } else {
      return getDisplayType( getType() );
    }
  }

  public static String getDisplayType( String type ) {
    if ( type == null ) {
      throw new IllegalArgumentException( getString( "DatasourceInfo.TYPE_NULL" ) );
    }

    String displayName = null;
    try {
      String key = MSG_PREFIX + type.replace( " ", "_" );
      displayName = getString( key );
      return displayName == null || displayName.equals( key ) ? type : displayName;
    } catch ( Exception e ) {
      // MessageHandler not initialized properly
      return type;
    }
  }

  public static String getDisplayType( String type, GwtDatasourceMessages messages ) {
    if ( type == null ) {
      throw new IllegalArgumentException( getString( "DatasourceInfo.TYPE_NULL" ) );
    }
    String displayName = null;
    try {
      String key = MSG_PREFIX + type.replace( " ", "_" );
      displayName = getString( key );
      return displayName == null || displayName.equals( key ) ? type : displayName;
    } catch ( Exception e ) {
      // messages not initialized properly
      return type;
    }
  }

  @Bindable
  @Override
  public String getId() {
    return id;
  }


  @Bindable
  @Override
  public String getType() {
    return type;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "DatasourceInfo [id=" + id + ", type=" + type + "]";
  }

  @Override
  public boolean isEditable() {
    return this.editable;
  }

  @Override
  public boolean isRemovable() {
    return this.removable;
  }

  @Override
  public boolean isImportable() {
    return this.importable;
  }

  @Override
  public boolean isExportable() {
    return this.exportable;
  }

  @Override
  public boolean isCreatable() {
    return this.creatable;
  }

  public static void setMessageBundle( DatasourceMessages bundle ) {
    messageBundle = bundle;
  }

  protected static String getString( String key ) {
    if ( messageBundle != null ) {
      return messageBundle.getString( key );
    } else {
      return MessageHandler.getString( key );
    }
  }

}
