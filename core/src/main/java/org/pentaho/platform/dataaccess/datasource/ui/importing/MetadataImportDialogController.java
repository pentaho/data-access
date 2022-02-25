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
 * Copyright (c) 2002-2022 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.ui.importing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.apache.http.HttpStatus;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.mantle.client.csrf.CsrfRequestBuilder;
import org.pentaho.mantle.client.csrf.CsrfUtil;
import org.pentaho.mantle.client.csrf.JsCsrfToken;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.tags.GwtConfirmBox;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;
import org.pentaho.ui.xul.gwt.tags.GwtPromptBox;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;
import org.pentaho.ui.xul.util.XulDialogCallback;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class MetadataImportDialogController extends AbstractXulDialogController<MetadataImportDialogModel>
  implements IImportPerspective, IOverwritableController {
  /**
   *
   */
  private static final String UPLOAD_URL = "plugin/data-access/api/datasource/metadata/uploadxmi"; //POST
  private static final String METADATA_IMPORT_XMI_URL = "plugin/data-access/api/datasource/metadata/import/uploaded";
    //POST
  private static final String METADATA_IMPORT_DSW_URL = "plugin/data-access/api/datasource/dsw/import/uploaded"; //POST
  private static final String METADATA_CHECK_URL = "plugin/data-access/api/datasource/metadata/iscontainsmodel"; //GET
  private static Integer FILE_UPLOAD_SUFFIX = 0;
  private BindingFactory bf;
  private XulButton acceptButton;
  private XulTree localizedBundlesTree;
  private XulTextbox domainIdText;
  private XulLabel metaFileLocation;
  private XulDialog importDialog;
  private ResourceBundle resBundle;
  private MetadataImportDialogModel importDialogModel;
  private XulLabel fileLabel;
  private FlowPanel mainFormPanel;
  private FlowPanel propertiesFileImportPanel;
  private XulVbox hiddenArea;
  private DatasourceMessages messages = null;
  private boolean overwrite;
  private boolean allowToHide = true;
  private static FormPanel.SubmitCompleteHandler submitHandler = null;
  private ImportCompleteCallback importCompleteCallback;

  // GWT controls
  private FormPanel formPanel;
  private FileUpload metadataFileUpload;
  private TextBox formDomainIdText;

  protected static final int OVERWRITE_EXISTING_SCHEMA = 8;
  private List<DialogListener> dialogCopyListeners = new ArrayList<DialogListener>();

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

  /**
   * The CSRF token field/parameter.
   * Its name and value are set to the expected values before each submit,
   * to match the obtained
   */
  private Hidden csrfTokenParameter;


  public void init() {
    try {
      resBundle = (ResourceBundle) super.getXulDomContainer().getResourceBundles().get( 0 );
      importDialogModel = new MetadataImportDialogModel();
      csrfTokenParameter = new Hidden( DISABLED_CSRF_TOKEN_PARAMETER );
      localizedBundlesTree = (XulTree) document.getElementById( "localizedBundlesTree" );
      domainIdText = (XulTextbox) document.getElementById( "domainIdText" );
      domainIdText.addPropertyChangeListener( new DomainIdChangeListener() );
      importDialog = (XulDialog) document.getElementById( "importDialog" );
      fileLabel = (XulLabel) document.getElementById( "fileLabel" );
      metaFileLocation = (XulLabel) document.getElementById( "uploadFileLabel" );
      acceptButton = (XulButton) document.getElementById( "importDialog_accept" );
      hiddenArea = (XulVbox) document.getElementById( "metadataImportCard" );
      acceptButton.setDisabled( true );

      bf.setBindingType( Binding.Type.ONE_WAY );
      Binding localizedBundlesBinding =
        bf.createBinding( importDialogModel, "localizedBundles", localizedBundlesTree, "elements" );
      localizedBundlesBinding.fireSourceChanged();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private void createWorkingForm() {
    if ( formPanel == null ) {
      formPanel = new FormPanel();
      formPanel.setMethod( FormPanel.METHOD_POST );
      formPanel.setEncoding( FormPanel.ENCODING_MULTIPART );
      formPanel.setAction( GWT.getHostPageBaseURL()+ UPLOAD_URL );
      formPanel.getElement().getStyle().setProperty( "position", "absolute" );
      formPanel.getElement().getStyle().setProperty( "visibility", "hidden" );
      formPanel.getElement().getStyle().setProperty( "overflow", "hidden" );
      formPanel.getElement().getStyle().setProperty( "clip", "rect(0px,0px,0px,0px)" );
      mainFormPanel = new FlowPanel();
      mainFormPanel.add( csrfTokenParameter );
      formPanel.add( mainFormPanel );
      propertiesFileImportPanel = new FlowPanel();
      mainFormPanel.add( propertiesFileImportPanel );

      formDomainIdText = new TextBox();
      formDomainIdText.setName( "domainId" );
      mainFormPanel.add( formDomainIdText );
      metadataFileUpload = new FileUpload();
      metadataFileUpload.setName( "metadataFile" );
      metadataFileUpload.getElement().setId( "metaFileUpload" );
      metadataFileUpload.addChangeHandler( new ChangeHandler() {
        @Override
        public void onChange( ChangeEvent event ) {
          String filename = ( (FileUpload) event.getSource() ).getFilename();
          if ( filename != null && filename.trim().length() > 0 ) {
            metaFileLocation.setValue( filename );
            importDialogModel.setUploadedFile( filename );
            acceptButton.setDisabled( !isValid() );
          } else {
            metaFileLocation
              .setValue( resBundle.getString( "importDialog.SELECT_METAFILE_LABEL", "browse for metadata file" ) );
            importDialogModel.setUploadedFile( null );
            acceptButton.setDisabled( !isValid() );
          }
        }
      } );

      mainFormPanel.add( metadataFileUpload );
      formPanel.addSubmitCompleteHandler( createSubmitCompleteHandler() );

      VerticalPanel vp = (VerticalPanel) hiddenArea.getManagedObject();
      vp.add( formPanel );
    }
  }

  private SubmitCompleteHandler createSubmitCompleteHandler() {
    return new SubmitCompleteHandler() {

      @Override
      public void onSubmitComplete( SubmitCompleteEvent event ) {

        final String jsonResponseText = new HTML( event.getResults() ).getText(); //delete all surrounded tags
        final JSONObject jsonResponse;
        JSONValue jsonVal = JSONParser.parseStrict( jsonResponseText );

        if ( jsonVal != null ) {
          jsonResponse = jsonVal.isObject();
        } else {
          jsonResponse = null;
        }
        if ( jsonResponse == null ) {
          onImportError( "wrong data from xmi file checker" );
          return;
        }

        String tempFileName = jsonResponse.get( "xmiFileName" ).isString().stringValue();
        RequestBuilder checkFileRequest = new RequestBuilder( RequestBuilder.GET,
          METADATA_CHECK_URL + "?tempFileName=" + URL.encode( tempFileName ) );

        checkFileRequest.setCallback( new RequestCallback() {
          @Override
          public void onResponseReceived( Request request, Response response ) {
            if ( response.getStatusCode() == HttpStatus.SC_OK ) {
              if ( Boolean.TRUE.toString().equalsIgnoreCase( response.getText() ) ) {
                promptImportMetadata( resBundle.getString( "importDialog.IMPORT_METADATA" ),
                  resBundle.getString( "importDialog.CONFIRMATION_LOAD_DSW" ),
                  resBundle
                    .getString( "importDialog.DIALOG_DSW_RADIO", "Data Source Wizard (Includes Analysis model)" ),
                  resBundle.getString( "importDialog.DIALOG_METADATA_RADIO", "Metadata model" ),
                  new AsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess( Boolean result ) {
                      new XmiImporterRequest( (Boolean) result ? METADATA_IMPORT_DSW_URL : METADATA_IMPORT_XMI_URL,
                        jsonResponse ).doImport( false );
                    }

                    @Override
                    public void onFailure( Throwable caught ) {
                      onImportError( caught.getMessage() );
                    }
                  } );
              } else if ( Boolean.FALSE.toString().equals( response.getText() ) ) {
                new XmiImporterRequest( METADATA_IMPORT_XMI_URL, jsonResponse ).doImport( false );
              } else {
                onImportError( "wrong data from xmi file checker" );
              }

            } else {
              onImportError( "[server data error] , wrong code: " + response.getStatusCode() );
            }
          }

          @Override
          public void onError( Request request, Throwable exception ) {
            onImportError( "[request error] " + exception.getMessage() );
          }
        } );
        try {
          checkFileRequest.send();
        } catch ( RequestException e ) {
          onImportError( e.getMessage() );
        }
      }
    };
  }

  public void setImportCompleteCallback( ImportCompleteCallback callback ) {
    this.importCompleteCallback = callback;
  }

  public interface ImportCompleteCallback {
    public void onImportSuccess();

    public void onImportCancel();
  }

  private class XmiImporterRequest implements RequestCallback {

    private JSONObject jsonFileList = null;
    private String url = null;

    public XmiImporterRequest( String url, JSONObject jsonFileList ) {
      this.jsonFileList = jsonFileList;
      this.url = url;
    }

    public void doImport( boolean overwrite ) {
      RequestBuilder requestBuilder = new CsrfRequestBuilder( RequestBuilder.POST,
              GWT.getHostPageBaseURL() + url );
      requestBuilder.setRequestData( "domainId=" + URL.encode( importDialogModel.getDomainId() )
        + "&jsonFileList=" + URL.encode( jsonFileList.toString() )
        + "&overwrite=" + Boolean.toString( overwrite ) );

      requestBuilder.setHeader( "Content-Type", "application/x-www-form-urlencoded" );
      requestBuilder.setCallback( this );
      try {
        requestBuilder.send();
      } catch ( RequestException e ) {
        onImportError( e.getMessage() );
      }
    }

    @Override
    public void onResponseReceived( Request request, Response response ) {
      if ( response.getStatusCode() == HttpStatus.SC_OK ) {
        //done
        onImportSuccess();
      } else if ( response.getStatusCode() == HttpStatus.SC_CONFLICT ) {
        //exists
        confirm( resBundle.getString( "Metadata.OVERWRITE_TITLE" ),
          messages.getString( "Metadata.OVERWRITE_EXISTING_SCHEMA" ),
          resBundle.getString( "importDialog.DIALOG_OK", "Ok" ),
          resBundle.getString( "importDialog.DIALOG_CANCEL", "Cancel" ),
          new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess( Boolean result ) {
              //one more attempt with overwrite=true
              if ( result ) {
                doImport( true );
              } else {
                allowToHide = true;
                hideDialog();
              }
            }

            @Override
            public void onFailure( Throwable caught ) {
              onImportError( caught.getMessage() );
            }
          } );
      } else {
        onImportError( "[server data error] , wrong code: " + response.getStatusCode() );
      }
    }

    @Override
    public void onError( Request request, Throwable exception ) {
      onImportError( "[request error] " + exception.getMessage() );
    }

  }

  public XulDialog getDialog() {
    return importDialog;
  }

  public MetadataImportDialogModel getDialogResult() {
    return importDialogModel;
  }

  public boolean isValid() {
    return importDialogModel.isValid();
  }

  @Bindable
  public void setMetadataFile() {
    jsClickUpload( metadataFileUpload.getElement().getId() );
  }

  @Bindable
  public void removeLocalizedBundle() {
    int[] selectedRows = localizedBundlesTree.getSelectedRows();
    if ( selectedRows.length == 1 ) {
      propertiesFileImportPanel.remove( selectedRows[ 0 ] );
      importDialogModel.removeLocalizedBundle( selectedRows[ 0 ] );
    }
  }

  @Bindable
  public void addLocalizedBundle() {
    final FileUpload localizedBundleUpload = new FileUpload();
    localizedBundleUpload.setName( "localeFiles" );
    localizedBundleUpload.getElement().setId( "propertyFileUpload" + FILE_UPLOAD_SUFFIX++ );
    localizedBundleUpload.addChangeHandler( new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        String fileName = ( (FileUpload) event.getSource() ).getFilename();
        if ( fileName == null || fileName.length() < 1 ) {  // Trying to detect a cancel
          propertiesFileImportPanel.remove( localizedBundleUpload );
        } else {
          importDialogModel.addLocalizedBundle( fileName, fileName );
        }
      }
    } );
    propertiesFileImportPanel.add( localizedBundleUpload );
    jsClickUpload( localizedBundleUpload.getElement().getId() );
  }

  native void jsClickUpload( String uploadElement ) /*-{
    $doc.getElementById(uploadElement).click();
  }-*/;

  private void reset() {
    metaFileLocation
      .setValue( resBundle.getString( "importDialog.SELECT_METAFILE_LABEL", "browse for metadata file" ) );
    importDialogModel.removeAllLocalizedBundles();
    importDialogModel.setUploadedFile( null );
    if ( formPanel != null && RootPanel.get().getWidgetIndex( formPanel ) != -1 ) {
      RootPanel.get().remove( formPanel );
    }
    acceptButton.setDisabled( true );
    domainIdText.setValue( "" );
    csrfTokenParameter.setValue( "" );
    overwrite = false;
    formPanel = null;
    importCompleteCallback = null;

    removeHiddenPanels();
  }


  public void concreteUploadCallback( String fileName, String uploadedFile ) {
    importDialogModel.addLocalizedBundle( fileName, uploadedFile );
  }

  public void genericUploadCallback( String uploadedFile ) {
    importDialogModel.setUploadedFile( uploadedFile );
    acceptButton.setDisabled( !isValid() );
  }

  private void onImportCancel() {
    if ( importCompleteCallback != null ) {
      importCompleteCallback.onImportCancel();
    }
  }

  private void onImportSuccess() {
    showMessagebox( resBundle.getString( "importDialog.IMPORT_METADATA" ),
      resBundle.getString( "importDialog.SUCCESS_METADATA_IMPORT" ) );
    super.hideDialog();
  }

  private void onImportError( String message ) {
    showMessagebox( resBundle.getString( "importDialog.IMPORT_METADATA" ),
      resBundle.getString( "importDialog.ERROR_IMPORTING_METADATA" ) + ": " + message );
    super.hideDialog();
  }

  public void showDialog() {
    reset();
    importDialog.setTitle( resBundle.getString( "importDialog.IMPORT_METADATA", "Import Metadata" ) );
    fileLabel.setValue( resBundle.getString( "importDialog.XMI_FILE", "XMI File" ) + ":" );
    super.showDialog();
    createWorkingForm();
    getDialog().center();
  }

  public void reShowDialog() {
    importDialog.setDisabled( false );
    allowToHide = true;
    for ( DialogListener l : dialogCopyListeners ) {
      super.addDialogListener( l );
    }
  }

  public void setBindingFactory( final BindingFactory bf ) {
    this.bf = bf;
  }

  public String getName() {
    return "metadataImportDialogController";
  }

  public FormPanel getFormPanel() {
    return formPanel;
  }

  class DomainIdChangeListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      formDomainIdText.setText( evt.getNewValue().toString() );
      importDialogModel.setDomainId( evt.getNewValue().toString() );
      acceptButton.setDisabled( !isValid() );
    }
  }

  public void buildAndSetParameters() {

    Hidden overwriteParam = new Hidden( "overwrite", String.valueOf( overwrite ) );
    mainFormPanel.add( overwriteParam );

  }

  public void removeHiddenPanels() {
    // Remove all previous hidden form parameters otherwise parameters
    // from a previous import would get included in current form submit
    for ( int i = 0; mainFormPanel != null && i < mainFormPanel.getWidgetCount(); i++ ) {
      if ( mainFormPanel.getWidget( i ).getClass().equals( Hidden.class )  && mainFormPanel.getWidget( i ) != csrfTokenParameter ) {
        mainFormPanel.remove( mainFormPanel.getWidget( i ) );
      }
    }
  }

  /**
   * Convert to $NLS$
   *
   * @param results
   * @return msg int PUBLISH_TO_SERVER_FAILED = 1; int PUBLISH_GENERAL_ERROR = 2; int PUBLISH_DATASOURCE_ERROR = 6; int
   * PUBLISH_USERNAME_PASSWORD_FAIL = 5; int PUBLISH_XMLA_CATALOG_EXISTS = 7; int PUBLISH_SCHEMA_EXISTS_ERROR = 8;
   */
  public String convertToNLSMessage( String results, String fileName ) {
    String msg = results;
    int code = new Integer( results ).intValue();
    String messageId;
    String[] parameters = new String[ 0 ];
    switch ( code ) {
      case 1: //PUBLISH_TO_SERVER_FAILED = 1;
        messageId = "Metadata.PUBLISH_TO_SERVER_FAILED";
        break;
      case 2: //PUBLISH_GENERAL_ERROR = 2;
        messageId = "Metadata.PUBLISH_GENERAL_ERROR";
        break;
      case 3: //PUBLISH_DATASOURCE_ERROR = 6;
        messageId = "Metadata.PUBLISH_DATASOURCE_ERROR";
        break;
      case 4: //PUBLISH_USERNAME_PASSWORD_FAIL = 5;
        messageId = "Metadata.PUBLISH_USERNAME_PASSWORD_FAIL";
        break;
      case 7: //PUBLISH_XMLA_CATALOG_EXISTS = 7;
        messageId = "Metadata.PUBLISH_XMLA_CATALOG_EXISTS";
        break;
      case 8: //PUBLISH_SCHEMA_EXISTS_ERROR
        messageId = "Metadata.OVERWRITE_EXISTING_SCHEMA";
        break;
      case 10: //PUBLISH_PROHIBITED_SYMBOLS_ERROR
        messageId = "Metadata.PUBLISH_PROHIBITED_SYMBOLS_ERROR";
        parameters = new String[] { NameUtils.reservedCharListForDisplay( ", " ) };
        break;
      default:
        messageId = "Metadata.ERROR";
        break;
    }
    msg = messages.getString( messageId, parameters );
    return msg + " Metadata File: " + fileName;
  }

  /**
   * Shows an informational dialog
   *
   * @param title   title of dialog
   * @param message message within dialog
   */
  private void showMessagebox( final String title, final String message ) {
    XulMessageBox messagebox = new GwtMessageBox() {
      @Override public void hide() {
        super.hide();
        if ( importCompleteCallback != null ) {
          importCompleteCallback.onImportSuccess();
        }
      }
    };

    messagebox.setTitle( title );
    messagebox.setMessage( message );
    messagebox.open();
  }

  /**
   * Shows a confirmation dialog
   *
   * @param title
   * @param message
   * @param okButtonLabel
   * @param cancelButtonLabel
   * @param onResulthandler
   */
  private void confirm( final String title, final String message, final String okButtonLabel,
                        final String cancelButtonLabel,
                        final AsyncCallback<Boolean> onResulthandler ) {
    XulConfirmBox confirm = new GwtConfirmBox() {
      @Override public Panel getDialogContents() {
        VerticalPanel vp = new VerticalPanel();
        Label lbl = new Label( this.getMessage() );
        vp.add( lbl );
        vp.setCellHorizontalAlignment( lbl, VerticalPanel.ALIGN_LEFT );
        vp.setCellVerticalAlignment( lbl, VerticalPanel.ALIGN_MIDDLE );
        return vp;
      }
    };
    confirm.setTitle( title );
    confirm.setMessage( message );
    confirm.setAcceptLabel( okButtonLabel );
    confirm.setCancelLabel( cancelButtonLabel );
    confirm.addDialogCallback( new XulDialogCallback<String>() {
      public void onClose( XulComponent component, Status status, String value ) {
        if ( onResulthandler != null ) {
          onResulthandler.onSuccess( status == XulDialogCallback.Status.ACCEPT );
        }

      }

      public void onError( XulComponent component, Throwable err ) {
        onResulthandler.onFailure( err );
        return;
      }
    } );
    confirm.open();
  }

  /**
   * Shows a promt dialog
   *
   * @param title
   * @param message
   * @param radioDSWLabel
   * @param radioMetaLabel
   * @param onResulthandler
   */
  private void promptImportMetadata( final String title, final String message, final String radioDSWLabel,
                                     final String radioMetaLabel,
                                     final AsyncCallback<Boolean> onResulthandler ) {
    final VerticalPanel panel = new VerticalPanel();
    panel.add( new Label( message ) ); //$NON-NLS-1$
    VerticalPanel vp = new VerticalPanel();
    HorizontalPanel hp = new HorizontalPanel();
    hp.getElement().getStyle().setMarginBottom( 10, Style.Unit.PX );
    hp.getElement().getStyle().setMarginTop( 10, Style.Unit.PX );
    final RadioButton dswRadio = new RadioButton( "importMetadata" );
    RadioButton metadataRadio = new RadioButton( "importMetadata" );
    dswRadio.setEnabled( true );
    dswRadio.setValue( true );
    hp.add( dswRadio );
    hp.add( new Label( radioDSWLabel ) );
    vp.add( hp );
    HorizontalPanel hp2 = new HorizontalPanel();
    hp2.add( metadataRadio );
    hp2.add( new Label( radioMetaLabel ) );
    vp.add( hp2 );
    panel.add( vp );
    XulPromptBox promptBox = new GwtPromptBox() {
      @Override
      public Panel getDialogContents() {
        return panel;
      }

      @Override
      public int open() {
        super.show();
        dswRadio.setFocus( true );
        return 0;
      }

      @Override
      public Panel getButtonPanel() {
        Panel button = super.getButtonPanel();
        return button;
      }
    };
    promptBox.setTitle( title );
    promptBox.setAcceptLabel( resBundle.getString( "importDialog.DIALOG_OK", "OK" ) );
    promptBox.setCancelLabel( resBundle.getString( "importDialog.DIALOG_Cancel", "Cancel" ) );
    promptBox.addDialogCallback( new XulDialogCallback<String>() {
      @Override
      public void onClose( XulComponent component, Status status, String value ) {
        if ( status == Status.CANCEL ) {
          onImportCancel();
          reShowDialog();
          return;
        }
        if ( onResulthandler != null ) {
          onResulthandler.onSuccess( dswRadio.getValue() );
        }
      }

      @Override
      public void onError( XulComponent xulComponent, Throwable throwable ) {
        onResulthandler.onFailure( throwable );
      }
    } );
    promptBox.setWidth( 460 );
    promptBox.setHeight( 140 );
    promptBox.open();
  }

  /**
   * pass localized messages from Entry point initialization
   *
   * @param datasourceMessages
   */
  public void setDatasourceMessages( DatasourceMessages datasourceMessages ) {
    this.messages = datasourceMessages;
  }

  /**
   * helper method for dialog display
   *
   * @return
   */
  public String getFileName() {
    return this.importDialogModel.getUploadedFile();
  }

  public void setOverwrite( boolean overwrite ) {
    this.overwrite = overwrite;

  }

  @Override public void addDialogListener( DialogListener<MetadataImportDialogModel> listener ) {
    super.addDialogListener( listener );
    this.dialogCopyListeners.add( listener );
  }

  @Override
  public void onDialogAccept() {
    importDialog.setDisabled( true );
    allowToHide = false;
    setupCsrfToken();
    super.onDialogAccept();
  }

  @Override
  public void hideDialog() {
    if ( allowToHide ) {
      super.hideDialog();
    }
  }

  /**
   * Obtains a CSRF token for the form's current URL and
   * fills it in the form's token parameter hidden field.
   */
  private void setupCsrfToken() {
    assert formPanel != null;

    JsCsrfToken token = CsrfUtil.getCsrfTokenSync( formPanel.getAction() );
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
