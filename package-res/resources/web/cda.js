var pentaho = pentaho || {};
pentaho.cda = {
	descriptors: [],
	/*
	@description class function to discover the cube(s) on the Pentaho BI Server
	@parameters - configuration object with options and callback function
	@return - returns array of descriptors found on BI server via callback function
	*/
	discoverDescriptors: function(func){
		//If we already have some descriptors in the class variable, return early with those values
		if (pentaho.cda.descriptors.length > 0) {
			//console.log("Using Existing descriptors");
			func(pentaho.cda.descriptors);
		}
		else {
				//call BI server for all CDA files.
				pentaho.xhr.execute("/pentaho/content/cda/getCdaList", {
					async: true,
					dataType: "json",
					type: "GET",
					complete: function(response){
						var fileList = eval('(' + response + ')' );
						var fileCount = fileList.resultset.length;
						var result;
						if ( fileCount > 0) {
							for (var i=0;i<fileCount;i++) {
								result = fileList.resultset[i];
								pentaho.cda.descriptors[i] = {
									name: result[0],
									path: result[1]
								};
							}
						} 
						if (typeof func == 'function'){
							func(pentaho.cda.descriptors);
						} else {
							throw new Error("Unrecognized callback function to pentaho.cda.discoverDescriptors");
						}
						//call success function if there is one
					}, // end on complete
					error: function(e){
						throw new Error("unable to get pentaho descriptors");
					} // end on error
				}); // end xhr.execute
			}  //end else length was zero
		} // end discoverDescriptors
	
};

pentaho.cda.Descriptor = function(json){
	this.name        = json.name || 'Unknown';
	this.path        = json.path || '';
	this.datasources = [];
	this.queries     = [];
	//console.log(this.path);
}

pentaho.cda.Descriptor.prototype = {
	addDataSource: function(connection){
		for (var i=0,j=this.datasources.length;i<j;i++){
			if (this.datasources[i] == connection) {
				return;
			}
		}
		this.datasources.push(connection);
	}
	,addQuery: function(query){
		for (var i=0,j=this.queries.length;i<j;i++){
			if (this.queries[i] == query) {
				//console.log('found something');
				return;
			}
		}
		this.dataaccesses.push(access);
	}
	,toXML: function() {
		//var xmlDoc = document.implementation.createDocument(namespaceURL, 'CDADescriptor', null);
		
		var file = '<?xml version=\"1.0\" encoding=\"utf-8\"?>';
		file += '<CDADescriptor><DataSources>';
		for (var i=0,j=this.datasources.length; i<j; i++) {
			file += this.datasources[i].toXML();
		}
		file += '</DataSources>';
		//console.log(this.dataaccesses.length);
		var myda;
		for (i=0,j=this.dataaccesses.length; i<j; i++) {
			myda =this.dataaccesses[i];
			//console.log(myda);
			file += myda.toXML();
		}
		
		file += '</CDADescriptor>';
		return file;
	}
	,save: function(path) {
		this.path = path;
		$.post("content/cda/writeCdaFile",{path:path,data:this.toXML()},
			function(data){
				//console.log(data);
			});
	}
	,discoverQueries: function(func) {
    		var that = this;
		
		if (that.queries.length == 0) {
			pentaho.xhr.execute("/pentaho/content/cda/listQueries", {
				async: true,
				dataType: "json",
				type: "GET",
				data: {
					path: that.path,
					outputType:'json'
				},
				complete: function(data){
					var queryList = eval('(' + data + ')' ),
					rs=queryList.resultset, query, loc;
					for (query in rs){
						loc = rs[query];
						that.queries.push(new pentaho.cda.Query({id:loc[0], name:loc[1]||loc[0], type:loc[2]}, that));
					};
					func(that.queries);
				}
			});
		} else {
			func(that.queries);
		}
	}
	//,load: function(){load using getCdaFile}
}

pentaho.cda.Connection = function(json){
	this.id   = json.id || 1;
	this.type = json.type || 'metadata.metadata';
}

pentaho.cda.MQLConnection = function(json) {
	pentaho.cda.Connection.call(this, json);
	this.type   = 'metadata.metadata';
	this.domain = json.domain;
	this.xmi    = 'metadata.xmi';
}

inheritPrototype(pentaho.cda.MQLConnection, pentaho.cda.Connection);

pentaho.cda.MQLConnection.prototype.toXML = function() {
		return '<Connection id=\"' + this.id + "\" type=\"" + this.type + '\">'+
		'<DomainId>'+ this.domain + '</DomainId>' +
		'<XmiFile>' + this.xmi    + '</XmiFile>'  +
		'</Connection>';
}

pentaho.cda.DataAccess = function(json){
	this.id   = json.id || 1;
	this.type = json.type || 'mql';
	this.name = json.name || 'Unknown';
	this.query = '';
	this.access = 'public';
	this.cache  = true;
	this.cacheDuration = 1;
	this.columns = [];
	this.parameters = [];
}

pentaho.cda.DataAccess.prototype = {
	toXML: function(){
		var str = '';
		str = "<DataAccess id=\""+this.id+"\" connection=\""+this.connection.id+"\" type=\""+this.type+"\" access=\""+this.access+"\">"+
			"<Name>"+ this.name +"</Name>"+
			"<Query>" + this.query + "</Query>"+
			"</DataAccess>";
		//console.log(str);
		return str;
		
	}, // end toXML
	setConnection: function(conn) {
		if (conn instanceof pentaho.cda.Connection) {
			this.connection = conn;
		} else {
			this.connection = {};
		}
	}
} //end pentaho.cda.DataAccess.prototype

pentaho.cda.Query = function(query, file){
	this.file = file;
	this.id   = query.id   || 1;
	this.name = query.name || '';
	this.type = query.type || 'mql';
/*
	if (query.connection) {
		this.connection = query.connection;
	} else {
		this.connection = new pentaho.cda.Connection({});
	}
	this.columns    = [];
	this.outputs    = [];
	this.rawquery      = '';
	*/
	
	this.parameters = [];
	if (query.parameters == null) {
		try {
			this.discoverParameters();
		} catch(e) {
			//do nothing
		}
	}

}

pentaho.cda.Query.prototype = {
	discoverParameters: function(func){
		var that = this;

		if (this.parameters.length == 0) {
			$.getJSON("/pentaho/content/cda/listParameters", {path:that.file.path, dataAccessId:this.id},
			function(data){
				for (var i=0,j=data.resultset.length;i<j;i++){
					var rs = data.resultset[i];
					//that.parameters[i] = new pentaho.cda.Parameter({id:rs[0], name:rs[1], type:rs[2]});
					that.parameters[i] = {id:rs[0], name:rs[1], type:rs[2]};
				};
				if (typeof func == 'function') {
					func(that.parameters);
				}
			});

		} else {
			func(this.parameters);
		}
	}
	,execute: function(func){
	$.getJSON("/pentaho/content/cda/doQuery", {path:this.file.path, dataAccessId:this.id},
		function(data){
			//console.log(columns);
			if (typeof func == 'function'){
				func(data);
			}
		});
	},
	/*
	@method addColumn
	@description Utility method to add a Column to the query
	*/
	addColumn: function(column){//we are passed a Column object
		this.columns.push(column);
	},
	/*
	@method removeColumn
	@description Utility method to remove a Column from the query
	*/
	removeColumn: function(index){
		this.column.splice(index,1);
	}

} // end pentaho.cda.Query.prototype
