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
* Copyright (c) 2021 Hitachi Vantara. All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.mantle.client.csrf.CsrfUtil;
import org.pentaho.mantle.client.csrf.JsCsrfToken;

public class UploadFileEntryPoint implements EntryPoint {

  /**
   * The name of the CSRF token field to use when CSRF protection is disabled.
   * <p>
   * An arbitrary name, yet different from the name it can have when CSRF protection enabled.
   * This avoids not having to dynamically adding and removing the field from the form depending
   * on whether CSRF protection is enabled or not.
   * <p>
   * When CSRF protection is enabled,
   * the actual name of the field is set before each submit.
   */
  private static final String DISABLED_CSRF_TOKEN_PARAMETER = "csrf_token_disabled";

  private FormPanel uploadForm;

  /**
   * The CSRF token field/parameter.
   * Its name and value are set to the expected values before each submit,
   * to match the obtained {@link JsCsrfToken}.
   * <p>
   * The Tomcat's context must have the `allowCasualMultipartParsing` attribute set
   * so that the `CsrfGateFilter` is able to transparently read this parameter
   * in a multi-part encoding form, as is the case of `form`.
   */
  private Hidden csrfTokenParameter;

  public void onModuleLoad() {
    // Create a FormPanel and point it at a service.
    uploadForm = new FormPanel();
    uploadForm.setAction( GWT.getModuleBaseURL() + "/UploadService" );

    // Because we're going to add a FileUpload widget, we'll need to set the
    // form to use the POST method, and multipart MIME encoding.
    uploadForm.setEncoding( FormPanel.ENCODING_MULTIPART );
    uploadForm.setMethod( FormPanel.METHOD_POST );

    // Create a panel to hold all of the form widgets.
    VerticalPanel panel = new VerticalPanel();
    uploadForm.setWidget( panel );

    // Create a TextBox, giving it a name so that it will be submitted.
    final TextBox tb = new TextBox();
    tb.setName( "textBoxFormElement" );
    panel.add( tb );

    // Create a FileUpload widget.
    FileUpload upload = new FileUpload();
    upload.setName( "uploadFormElement" );
    panel.add( upload );

    csrfTokenParameter = new Hidden( DISABLED_CSRF_TOKEN_PARAMETER );
    panel.add( csrfTokenParameter );

    // Add a 'Upload' button.
    Button uploadSubmitButton = new Button( "Upload" );
    panel.add( uploadSubmitButton );

    uploadSubmitButton.addClickListener( new ClickListener() {
      public void onClick( Widget sender ) {

        setupCsrfToken();

        uploadForm.submit();
      }
    } );

    uploadForm.addFormHandler( new FormHandler() {
      public void onSubmit( FormSubmitEvent event ) {
      }

      public void onSubmitComplete( FormSubmitCompleteEvent event ) {
        Window.alert( event.getResults() );
      }
    } );

    RootPanel.get().add( uploadForm );
  }

  /**
   * Obtains a CSRF token for the form's current URL and
   * fills it in the form's token parameter hidden field.
   */
  private void setupCsrfToken() {
    assert uploadForm != null;

    JsCsrfToken token = CsrfUtil.getCsrfTokenSync( uploadForm.getAction() );
    if ( token != null ) {
      csrfTokenParameter.setName( token.getParameter() );
      csrfTokenParameter.setValue( token.getToken() );
    } else {
      // Reset the field.
      csrfTokenParameter.setName( DISABLED_CSRF_TOKEN_PARAMETER );
      csrfTokenParameter.setValue( "" );
    }
  }
}
