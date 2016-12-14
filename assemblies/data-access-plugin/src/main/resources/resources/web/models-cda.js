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

pentaho.pda.CDAHandler = function CDAHandler(sandbox) {
	pentaho.pda.Handler.call(this, sandbox);
    this.type = pentaho.pda.SOURCE_TYPE_CDA;
}

inheritPrototype(pentaho.pda.CDAHandler, pentaho.pda.Handler); //borrow the parent's methods

pentaho.pda.CDAHandler.prototype.discoverModels = function(  ) {
	var that = this;
	pentaho.cda.discoverDescriptors(
		function(files){
			var i=0,j=0,datasource, descriptor;
			for (i=0,j=files.length;i<j;i++) {
				descriptor = new pentaho.cda.Descriptor({name:files[i].name, path:files[i].path});
				datasource = new pentaho.pda.model.cda(
					{id:files[i].name,
					name:files[i].name,
					type:pentaho.pda.SOURCE_TYPE_CDA,
					description:''
					});
				datasource.descriptor = descriptor;
				that.modelList.push(datasource);
			}
		}
	)
	return this.sources;		
}

pentaho.pda.model.cda = function(obj) {
	pentaho.pda.model.call(this, obj); //call parent object
	this.path = '';
}

inheritPrototype(pentaho.pda.model.cda, pentaho.pda.model); //borrow the parent's methods

	
pentaho.pda.model.cda.prototype.discoverModelDetail = function( forceLoad ) {

	var that =this
	this.descriptor.discoverQueries(function(queries){
		var i=0,j=0,datasource;
		for (i=0,j=queries.length;i<j;i++) {
            var query = new pentaho.pda.dataelement();
            query.dataType = pentaho.pda.Column.DATA_TYPES.NONE;
            query.elementType = pentaho.pda.Column.ELEMENT_TYPES.QUERY;
            query.id         = queries[i].id;
            query.name       = queries[i].name;
            query.query_type = queries[i].type;
            query.isMeasures = false;
            query.isTime = false;
            that.addElement( query );

			that.addCapability(pentaho.pda.CAPABILITIES.HAS_ACROSS_AXIS);
			that.addCapability(pentaho.pda.CAPABILITIES.IS_ACROSS_CUSTOM);
			that.addCapability(pentaho.pda.CAPABILITIES.HAS_FILTERS);
			that.addCapability(pentaho.pda.CAPABILITIES.IS_FILTER_CUSTOM);
			that.addCapability(pentaho.pda.CAPABILITIES.CAN_SORT);
		};
	});
}
