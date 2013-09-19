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

var console_enabled = false;
  
function pentaho_client_onload() {

}

    function findUrlParam( name, url ) {
      var pos = url.indexOf( '?' );
      var params = url.substr( pos );
      pos = params.indexOf( '?'+name+'=' );
      if( pos == -1 ) {
        pos = params.indexOf( '&'+name+'=' );
      }
      if( pos != -1 ) {
        var tmp = params.substr( pos + name.length + 2 );
        if( tmp.indexOf( '&' ) != -1 ) {
          tmp = tmp.substr( 0, tmp.indexOf( '&' ) );
        }
        tmp = unescape( tmp );
        return tmp;
      }
      return null;
    }

var gCtrlr = new WaqrProxy();

function WaqrProxy() {

    this.wiz = new Wiz();
    this.repositoryBrowserController = new RepositoryBrowserControllerProxy();
    
    this.savePg0 = function() {
    }
    
}

function Wiz() {
    this.currPgNum = 0;
    this.previewTypeSelect = {};
}


function RepositoryBrowserControllerProxy() {

    this.callbackObject = null;

    this.remoteSave = function( myFilename, mySolution, myPath, myType, myOverwrite ) {
    
        this.callbackObject.saveState(myFilename, mySolution, myPath, myType, myOverwrite);
    
    }
}

function parseXML(sText){
    var xmlDoc;
    try { //Firefox, Mozilla, Opera, etc.
        parser=new DOMParser();
        xmlDoc=parser.parseFromString(sText,"text/xml");
        return xmlDoc;
    } catch(e){
        try { //Internet Explorer
            xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
            xmlDoc.async="false";
            xmlDoc.loadXML(sText);
            return xmlDoc;
        } catch(e) {
            alert(e.message);
            return false;
        }
    }
}
   
PentahoUserConsole = function() {

  this.console_enabled = window.parent != null && window.parent.mantle_initialized == true;
  
  this.toggleEditCallback = null;

  if( this.console_enabled ) {
    window.parent.registerContentCallback(this);
  }

  this.editContentToggled = function(selected) { 
            if( this.toggleEditCallback ) {
                this.toggleEditCallback(selected);
            }
            if( selected ) {
                this.enableEditButton();
                this.lowerEditButton();
            } else {
                this.enableEditButton();
                this.resetEditButton();
            }
  }

  this.enableEditButton = function() {
    // if possible, enable the 'Edit' toolbar button
    if( this.console_enabled && window.parent.enableContentEdit ){
        window.parent.enableContentEdit( true );
    }

  }

  this.disableEditButton = function() {
    // if possible, disable the 'Edit' toolbar button
    if( this.console_enabled && window.parent.enableContentEdit ){
        window.parent.enableContentEdit( false );
    }
  }

  this.lowerEditButton = function() {
    // if possible, lower/depress the 'Edit' toolbar button
    if( this.console_enabled && window.parent.setContentEditSelected ){
        window.parent.setContentEditSelected( true );
    }
  }

  this.resetEditButton = function() {
    // if possible, raise/reset the 'Edit' toolbar button
    if( this.console_enabled && window.parent.setContentEditSelected ){
        window.parent.setContentEditSelected( false );
    }
  }

this.enableSaveButtons = function() {
    // if possible, enable the 'Save' and 'Save As' toolbar buttons
    if( this.console_enabled && window.parent.enableAdhocSave ) {
        window.parent.enableAdhocSave( true );
    }
}

this.disableSaveButtons = function() {
    // if possible, disable the 'Save' and 'Save As' toolbar buttons
    if( this.console_enabled && window.parent.enableAdhocSave ) {
        window.parent.enableAdhocSave( false );
    }
}

this.refreshBrowsePanel = function() {
    // if possible refresh the solution browser panel
    if ( this.console_enabled && window.parent.mantle_refreshRepository ) {
        window.parent.mantle_refreshRepository();
    }
}

  
}

