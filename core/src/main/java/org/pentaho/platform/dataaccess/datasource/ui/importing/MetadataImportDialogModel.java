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


package org.pentaho.platform.dataaccess.datasource.ui.importing;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MetadataImportDialogModel extends XulEventSourceAdapter {

  private List<LocalizedBundleDialogModel> localizedBundles;
  private String uploadedFile;
  private String domainId;

  public MetadataImportDialogModel() {
    localizedBundles = new ArrayList<LocalizedBundleDialogModel>();
  }

  public String getDomainId() {
    return domainId;
  }

  public void setDomainId( String domainId ) {
    this.domainId = domainId;
  }

  public void addLocalizedBundle( String fileName, String uploadedFile ) {
    localizedBundles.add( new LocalizedBundleDialogModel( fileName, uploadedFile ) );
    this.firePropertyChange( "localizedBundles", null, localizedBundles );
  }

  public void removeLocalizedBundle( int paramIndex ) {
    localizedBundles.remove( paramIndex );
    this.firePropertyChange( "localizedBundles", null, localizedBundles );
  }

  public void removeAllLocalizedBundles() {
    localizedBundles.clear();
    this.firePropertyChange( "localizedBundles", null, localizedBundles );
  }

  public String getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile( String uploadedFile ) {
    this.uploadedFile = uploadedFile;
  }

  @Bindable
  public List<LocalizedBundleDialogModel> getLocalizedBundles() {
    return localizedBundles;
  }

  @Bindable
  public void setLocalizedBundles( List<LocalizedBundleDialogModel> value ) {
    List<LocalizedBundleDialogModel> previousValue = localizedBundles;
    this.localizedBundles = value;
    this.firePropertyChange( "localizedBundles", previousValue, value );
  }

  public String getLocalizedBundleEntries() {
    String result = "";
    for ( LocalizedBundleDialogModel currentParameter : localizedBundles ) {
      result = result + currentParameter.getFileName() + "=" + currentParameter.getUploadedFile() + ";";
    }
    result = result.substring( 0, result.length() - 1 );
    return result;
  }

  public boolean isValid() {
    return uploadedFile != null && !StringUtils.isEmpty( domainId );
  }
}
