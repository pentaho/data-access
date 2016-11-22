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

/*
@module pentaho
@class xhr
@description Utility container for XHR events indepentdent of JS library
Currently uses pentaho-ajax.js to minimize dependencies, but could be any library
*/

var pentaho = pentaho || {};
pentaho.xhr = {
		/*
		@method execute
		@description call the appropriate XHR functions and delivers results
		*/
		execute: function(url, oConfig){
			var parms = "";
			for (parm in oConfig.data) {
				parms += "&" + parm +"="+oConfig.data[parm];
			}
			function func(response) {
				//console.log(response);
					var myxml = pentaho.xhr.parseXML(response);
					oConfig.complete(myxml);
			};
			//this shoudl be done by default then send if it cant convert
			//var response = pentahoGet(url, parms, func);
			var response = pentahoGet(url, parms);
			oConfig.complete(response);

		},
		/*
		@method SOAP2JS
		@description Utility function to convert data from pentaho ServiceAction SOAP to JS objects
		*/
		SOAP2JS: function(oXML) {
			//assumes we get a valid XML document
			//var oXML  = pentaho.xhr.parseXML(sSOAP);
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
			
			return oResults; //comment
		},
		/*
		@method parseXML
		@description Utility function to convert data from plain text/xml into XML document object
		*/
		parseXML: function (sText){
			var xmlDoc,parser;
			try { //Firefox, Mozilla, Opera, etc.
				  parser=new DOMParser();
				  xmlDoc=parser.parseFromString(sText,"text/xml");
				  return xmlDoc;
			}
			catch(e){
				try { //Internet Explorer
				  xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
				  xmlDoc.async="false";
				  xmlDoc.loadXML(sText);
				  return xmlDoc;
				} catch(e) {
				  	alert("parseXML Error" + e.message);
				  	return false;
				  }
			}
		} //end parseXML
		
	}  //end pentaho.xhr
