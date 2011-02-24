  
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

	function parseResultSetXml(sSOAP) {
		var oXML  = parseXML(sSOAP);
		var rows = oXML.getElementsByTagName('DATA-ROW');        //initialize array of all DATA-ROW returned in SOAP
		var cols = oXML.getElementsByTagName('COLUMN-HDR-ITEM'); //initialize arry of all COLUMN-HDR-ITEM in SOAP
		var oResults = {};     //initialize emply object for each the JSON rows
		oResults.results = []; //add empty array to hold DATA-ROW contents in the results JS property
		for (var i=0; i<rows.length; i++) {
			row = oXML.getElementsByTagName('DATA-ROW')[i]; //get the row for this loop var i
			oResults.results[i] = {}; //initialize each row with empty objects
			for (var j=0; j<cols.length; j++) {
				//addign the object value for column header COLUMN-HDR-ITEM and ros DATA-ROW values
				oResults.results[i][oXML.getElementsByTagName('COLUMN-HDR-ITEM')[j].firstChild.nodeValue] = row.getElementsByTagName('DATA-ITEM')[j].firstChild.nodeValue;
			}
		}
		
		return JSON.stringify(oResults); //uses json.org json2.js library;
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
   
MetadataQueryDefinition = function() {
    this.domainId = null;
    this.domainName = null;
    this.modelId = null;
    this.modelName = null;
    this.query = null;
    this.id = null;
    this.cols = null;
    this.data = null;
    
    this.getState = function() {
        var state = {};
        state.domainId = this.domainId;
        state.domainName = this.domainName;
        state.modelId = this.modelId;
        state.modelName = this.modelName;
        state.query = this.query;
        return state;
    }
    
    this.fromState = function( state ) {
        this.domainId = state.domainId;
        this.domainName = state.domainName;
        this.modelId = state.modelId;
        this.modelName = state.modelName;
        this.query = state.query;
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

