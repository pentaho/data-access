
pentaho.pda.OlapHandler = function OlapHandler(sandbox) {
	pentaho.pda.Handler.call(this, sandbox);
    this.type = pentaho.pda.SOURCE_TYPE_OLAP;
    this.XMLA_SERVICE_URL = CONTEXT_PATH + 'Xmla';
    this.datasourceCache = [];
}

inheritPrototype(pentaho.pda.OlapHandler, pentaho.pda.Handler); //borrow the parent's methods

pentaho.pda.OlapHandler.prototype.getSources = function(filter, callback) {
    
    if (arguments.length == 1 && typeof filter == 'function' ) {
        callback = filter;
        filter = null;
    }
    this.xmla = new Xmla({
            async: false
    });
        
    if (this.datasourceCache.length > 0) {
		if (filter == null ) {
			for (var i=0,j=this.datasourceCache.length;i<j;i++) {
				callback(this.datasourceCache[i]);
			}
		} else {
			for (var i=0,j=this.datasourceCache.length;i<j;i++) {
				each = this.datasourceCache[i];
				try {
					if (each[filter.property] == filter.value) {
						callback(each);
					}
				} catch(e) {
					//just move on to next
				}
			}
		}
	} else {


        this.datasourceCache = [];
		var that = this;
//        try {

            // get the data sources
            var rowset1 = this.xmla.discoverDataSources({
                url: this.XMLA_SERVICE_URL
            });
            if (rowset1.hasMoreRows()) {
                this.datasourceCache = rowset1.fetchAllAsObject();
                for (var numDataSources = this.datasourceCache.length, i=0; i<numDataSources; i++){
                        
                    // get the catalogs for this data source
                    var datasource = this.datasourceCache[i];
                    var properties = {};
                    properties[Xmla.PROP_DATASOURCEINFO] = datasource[Xmla.PROP_DATASOURCEINFO];
                    var rowset2 = this.xmla.discoverDBCatalogs({
                        url: datasource["URL"]?datasource["URL"]:this.XMLA_SERVICE_URL,
                        properties: properties
                    });
                    if (rowset2.hasMoreRows()) {
                        var catalog;
                        while (catalog = rowset2.fetchAsObject()){
                        
                            // get the cubes for this catalog
                            var catalogName = catalog["CATALOG_NAME"];
                            var properties = {};
                            properties[Xmla.PROP_DATASOURCEINFO] = this.datasourceCache[i].DataSourceName;
                            properties[Xmla.PROP_CATALOG] = catalogName;

                            var restrictions = {};
                            restrictions["CATALOG_NAME"] = catalogName;

                            var rowset3 = this.xmla.discoverMDCubes({
                                url: this.XMLA_SERVICE_URL
                            ,   properties: properties
                            ,   restrictions: restrictions
                            });
                   
                            var loader, id, cubeName;
                            while (rowset3.hasMoreRows()){
                                var cubeName = rowset3.fieldVal("CUBE_NAME");
                                var id = this.datasourceCache[i].DataSourceName+'\t'+catalogName+'\t'+cubeName;
                                var datasource = new pentaho.pda.model.olap(
									{id:id,
									name:catalog["CATALOG_NAME"],
									type:pentaho.pda.SOURCE_TYPE_OLAP,
									description:''
									});
                                datasource.xmlaDatasource = this.datasourceCache[i].DataSourceName;
                                datasource.catalog = catalogName;
                                datasource.cubeName = cubeName;
								datasource.xmla = that.xmla;
								datasource.XMLA_SERVICE_URL = that.XMLA_SERVICE_URL
                                this.sources.push(datasource);
                                rowset3.next();
                            }
                        }                        
                    } 
                    else {
                        section_error.innerHTML = "No datasources found";
                    }
                }
                callback(datasource);
            }
        }
//        } catch (e) {
//            alert( e.message );
//        }
		return this.sources;
    } //discoverModels

pentaho.pda.model.olap = function(obj) {
	pentaho.pda.model.call(this, obj); //call parent object
	this.xmlaDatasource = '';
	this.catalog = '';
	this.cubeName = '';
	
}

inheritPrototype(pentaho.pda.model.olap, pentaho.pda.model); //borrow the parent's methods

	
pentaho.pda.model.olap.prototype.discoverModelDetail = function( forceLoad ) {

        // create a model object
        //var id = datasourceConfig.id;
        //var name = datasourceConfig.name;
        //var model = new pentaho.pda.model()

        //model.xmlaDatasource = datasourceConfig.xmlaDatasource;
        //model.catalog = datasourceConfig.catalog;
        //model.cubeName = datasourceConfig.cubeName;

        // populate the model with the dimensions

        //try {
            this.discoverDimensions( );
        //} catch ( e ) { alert(e.message) };

        this.addCapability(pentaho.pda.CAPABILITIES.HAS_ACROSS_AXIS);
        this.addCapability(pentaho.pda.CAPABILITIES.IS_ACROSS_CUSTOM);
        this.addCapability(pentaho.pda.CAPABILITIES.HAS_DOWN_AXIS);
        this.addCapability(pentaho.pda.CAPABILITIES.IS_DOWN_CUSTOM);
        this.addCapability(pentaho.pda.CAPABILITIES.HAS_FILTERS);
        this.addCapability(pentaho.pda.CAPABILITIES.IS_FILTER_CUSTOM);
        this.addCapability(pentaho.pda.CAPABILITIES.CAN_SORT);

    }

pentaho.pda.model.olap.prototype.discoverDimensions = function ( ){
        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;

        var rowset = this.xmla.discoverMDDimensions({
            url: this.XMLA_SERVICE_URL
        ,   properties: properties
        ,   restrictions: restrictions
        });

        while (rowset.hasMoreRows()){
            var dimensionName = rowset.fieldVal("DIMENSION_NAME");
            var dimensionUniqueName = rowset.fieldVal("DIMENSION_UNIQUE_NAME");
            var dimensionType = rowset.fieldVal("DIMENSION_TYPE");
            var dimension = new pentaho.pda.dataelement();
            dimension.dataType = pentaho.pda.Column.DATA_TYPES.NONE;
            dimension.elementType = pentaho.pda.Column.ELEMENT_TYPES.DIMENSION;
            dimension.id = dimensionUniqueName;
            dimension.name = dimensionName;
            dimension.isMeasures = dimensionType == '2';
            dimension.isTime = dimensionType == '1';
            this.addElement( dimension );
            
            // get the hierarchies for this dimension
            this.discoverHierarchies( dimension );
            
            rowset.next();
        }
    }
            
pentaho.pda.model.olap.prototype.discoverHierarchies = function(dimension ) {
        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;
        restrictions["DIMENSION_UNIQUE_NAME"] = dimension.id;

        var rowset = this.xmla.discoverMDHierarchies({
            url: this.XMLA_SERVICE_URL
        ,   properties: properties
        ,   restrictions: restrictions
        });
       
        while (rowset.hasMoreRows()){
            var hierarchyName = rowset.fieldVal("HIERARCHY_NAME");
            var hierarchyUniqueName = rowset.fieldVal("HIERARCHY_UNIQUE_NAME");
            var hierarchy = new pentaho.pda.dataelement();
            hierarchy.dataType = pentaho.pda.Column.DATA_TYPES.NONE;
            hierarchy.elementType = pentaho.pda.Column.ELEMENT_TYPES.HIERARCHY;
            hierarchy.id = hierarchyUniqueName;
            hierarchy.name = hierarchyName;
            hierarchy.parent = dimension;
            dimension.addChild( hierarchy );
            this.addElement( hierarchy );
            
            this.discoverLevels( dimension, hierarchy );
            
            // get the levels for this hierarchy
            rowset.next();
        }
    }

pentaho.pda.model.olap.prototype.discoverLevels = function( dimension, hierarchy ) {

        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;
        restrictions["DIMENSION_UNIQUE_NAME"] = dimension.id;
        restrictions["HIERARCHY_UNIQUE_NAME"] = hierarchy.id;

        var rowset = this.xmla.discoverMDLevels({
            url: this.XMLA_SERVICE_URL
        ,   properties: properties
        ,   restrictions: restrictions
        });

        var parent = hierarchy

        while (rowset.hasMoreRows()){
            var levelName = rowset.fieldVal("LEVEL_CAPTION");
            var levelNumber = rowset.fieldVal("LEVEL_NUMBER");
            var level = new pentaho.pda.dataelement();
            level.dataType = pentaho.pda.Column.DATA_TYPES.STRING;
            level.elementType = pentaho.pda.Column.ELEMENT_TYPES.LEVEL;
            level.id = rowset.fieldVal("LEVEL_UNIQUE_NAME");
            level.name = levelName;
            level.number = levelNumber;
//            level.type = rowset.fieldVal("LEVEL_DBTYPE");
            level.parent = parent;
            hierarchy.addChild( level );
            this.addElement( level );

            if( dimension.isMeasures ) {
            // now get the measures
                this.discoverMeasures( dimension, hierarchy, level );
                level.isQueryElement = false;
            } else {
                level.isQueryElement = true;
            }

            parent = level;
            rowset.next();
        }
        
        
    }

pentaho.pda.model.olap.prototype.discoverMembers = function( dimension, hierarchy, level ) {

        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;
        restrictions["DIMENSION_UNIQUE_NAME"] = dimension.id;
        restrictions["HIERARCHY_UNIQUE_NAME"] = hierarchy.id;
        restrictions["LEVEL_NUMBER"] = level.id;

        var rowset = this.xmla.discoverMDMembers({
            url: this.XMLA_SERVICE_URL
        ,   properties: properties
        ,   restrictions: restrictions
        });

        while (rowset.hasMoreRows()){
            var memberName = rowset.fieldVal("MEMBER_NAME");
            var memberUniqueName = rowset.fieldVal("MEMBER_NAME");
            
            var measure = new pentaho.pda.dataelement();
            measure.dataType = pentaho.pda.Column.DATA_TYPES.NUMERIC;
            measure.elementType = pentaho.pda.Column.ELEMENT_TYPES.FACT;
            measure.id = memberUniqueName;
            measure.name = memberName;
            measure.parent = level;
            level.addChild( measure );
            this.addElement( measure );

            rowset.next();
        }
    }

pentaho.pda.model.olap.prototype.discoverMeasures = function( dimension, hierarchy, level ) {

        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;
//        restrictions["DIMENSION_UNIQUE_NAME"] = dimension.id;
//        restrictions["HIERARCHY_UNIQUE_NAME"] = hierarchy.id;
//        restrictions["LEVEL_NUMBER"] = level.id;
        var rowset = this.xmla.discoverMDMeasures({
            url: this.XMLA_SERVICE_URL
        ,   properties: properties
        ,   restrictions: restrictions
        });
        while (rowset.hasMoreRows()){
            var memberName = rowset.fieldVal("MEASURE_NAME");
            var memberUniqueName = rowset.fieldVal("MEASURE_UNIQUE_NAME");
            
            var measure = new pentaho.pda.dataelement();
            measure.dataType = pentaho.pda.Column.DATA_TYPES.NUMERIC;
            measure.elementType = pentaho.pda.Column.ELEMENT_TYPES.FACT;
            measure.id = memberUniqueName;
            measure.name = memberName;
            measure.parent = level;
            var agg = rowset.fieldVal("MEASURE_AGGREGATOR");
            
            switch(agg) {            
            case 1: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.SUM; break;
            case 2: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.COUNT; break;
            case 3: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.MIN; break;
            case 4: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.MAX; break;
            case 5: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.AVERAGE; break;
            case 6: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.VAR; break;
            case 7: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.STDDEV; break;
            case 8: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.CALC; break;
            case 9: measure.defaultAggregation = pentaho.pda.Column.AGG_TYPES.UNKNOWN; break;
            }
            
            measure.selectedAggregation = measure.defaultAggregation
            measure.availableAggregations = new Array(measure.defaultAggregation);
            level.addChild( measure );
            measure.isQueryElement = true;
            this.addElement( measure );

            rowset.next();
        }
    }
    
pentaho.pda.model.olap.prototype.getAllColumns = function() {
        var columns = new Array();

        for( var idx2 = 0; idx2<this.elements.length; idx2++ ) {
            if(this.elements[idx2].elementType == pentaho.pda.Column.ELEMENT_TYPES.LEVEL ||
               this.elements[idx2].elementType == pentaho.pda.Column.ELEMENT_TYPES.FACT
            ) {
                columns.push( this.elements[idx2] );
            }
        }
        return columns;
    }


    // create a new query
pentaho.pda.model.olap.prototype.createQuery = function() {
    var query = new pentaho.pda.query.olap(this);
    return query;
}

    // submit a new query
pentaho.pda.model.olap.prototype.submitQuery = function( query, rowLimit ) {

            var results = {
                metadata:[],
                resultset:[]
            }
            
        if(query.state.measures.length == 0 && query.state.rowSelections.length == 0 && query.state.columnSelections.length == 0 ) {
            // we can't construct a query for this
            return results;
        }
        
        // find a measure to use
        // TODO use the default measure if available
        var measures = query.state.measures;
        var measureAdded = false;
        if(measures.length == 0) {
            var column = this.getColumnsByFieldType([pentaho.pda.Column.ELEMENT_TYPES.FACT])[0]
            var selection = query.createSelection();
            selection.column = column;
            measures = [selection];
            measureAdded = true;
        }
        
        // create the MDX statement for this
        var mdx = "select ";
        var mdxFrag = new Array();

        for(var idx=0; idx<query.state.columnSelections.length; idx++) {
            mdxFrag.push( this.getSelectionMdx(query.state.columnSelections[idx],query.state.conditions) );
        }
        mdxFrag.push( this.getMeasuresMdx(measures) );
        if( mdxFrag.length == 1 ) {
            mdx += mdxFrag[0];
        } else {
            // we need a crossjoin
            mdx += 'Crossjoin(';
            for(var idx=0; idx<mdxFrag.length; idx++) {
                if(idx > 0) {
                    mdx += ', ';
                }
                mdx += mdxFrag[idx];
            }
            mdx += ')';
            
        }
        
        mdx += " ON COLUMNS, ";
        
        var mdxFrag = new Array();
        for(var idx=0; idx<query.state.rowSelections.length; idx++) {
            mdxFrag.push( this.getSelectionMdx(query.state.rowSelections[idx],query.state.conditions) );
        }
        
        if( mdxFrag.length == 1 ) {
            mdx += mdxFrag[0];
        } else {
            // we need a crossjoin
            mdx += 'Crossjoin(';
            for(var idx=0; idx<mdxFrag.length; idx++) {
                if(idx > 0) {
                    mdx += ', ';
                }
                mdx += mdxFrag[idx];
            }
            mdx += ')';
            
        }
        
        mdx += " ON ROWS from ["+this.cubeName+"]";

        query.state.mdx = mdx;
//alert(mdx);

        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;
        try {
            var rowset = this.xmla.executeTabular({
                url: this.XMLA_SERVICE_URL,
                statement: mdx,
                async: false,
                properties: properties
            ,   restrictions: restrictions
            });


            var fields = rowset.getFields();
            var fieldCount = (measureAdded) ? fields.length-1 : fields.length;

            for( var idx=0; idx<fieldCount; idx++ ) {
                var id = this.cleanupFieldName(fields[idx].name);
                // TODO how to get the column id e.g. '[Markets].[Territory]' instead of '[Markets].[Territory].[MEMBER_CAPTION]'
                var md = {
                    colIndex: 0,
                    colName : id,
                    colType : this.getDataTypeFromXsd( fields[idx].type )
//                    colLabel : ?
                };
                results.metadata.push(md);
            }
            
            while (rowset.hasMoreRows()){
                var row = new Array();
                for( var idx=0; idx<fieldCount; idx++ ) {
                   row.push(rowset.fieldVal(idx));
                }       
                results.resultset.push(row);
                rowset.next();
            }
            return results;
        } catch (e) {
            alert(e.message);
        }
        return null;
    }

pentaho.pda.model.olap.prototype.getMeasuresMdx = function(measures) {
        var mdx = '';
        if( measures.length > 1 ) {
            mdx += '{';
        }
        for(var idx=0; idx<measures.length; idx++) {
            if(idx > 0) {
                mdx += ', ';
            } 
            mdx += measures[idx].column.id;
        }
        if( measures.length > 1 ) {
            mdx += '}';
        }
        return mdx;
    }
    
pentaho.pda.model.olap.prototype.getSelectionMdx = function(selection, conditions) {

        // see if we have member selections
        var doneMembers = false;
        var needBrace = false;
        var mdxfrag = '';
        for(var c=0; c<conditions.length; c++ ) {
            if(conditions[c].column.id == selection.column.id ) {
                var values = conditions[c].value;
                for(var v=0; v<values.length; v++) {
                    if(doneMembers) {
                        mdxfrag += ', ';
                    needBrace = true;
                    }
                    mdxfrag += selection.column.id;
                    mdxfrag += '.[';
                    mdxfrag += values[v];
                    mdxfrag += ']';
                    doneMembers = true;
                }
            }
        }
        if(!doneMembers) {
            // do all the members
            mdxfrag += selection.column.id;
            mdxfrag += '.Members';
        }
        
        if( needBrace ) {
           return '{'+mdxfrag+'}';
        } else {
            return mdxfrag; 
        }
    }

pentaho.pda.model.olap.prototype.cleanupFieldName = function(name) {
        name = name.replace( /_x005b_/g , '[' );
        name = name.replace( /_x005d_/g , ']' );
        return name;
    }

pentaho.pda.model.olap.prototype.getDataTypeFromXsd = function( type ) {

        switch (type){
            case "xsd:boolean": return pentaho.pda.Column.DATA_TYPES.BOOLEAN;
            case "xsd:decimal": 
            case "xsd:double": 
            case "xsd:float":
            case "xsd:int":
            case "xsd:integer":
            case "xsd:nonPositiveInteger":
            case "xsd:negativeInteger":
            case "xsd:nonNegativeInteger":
            case "xsd:positiveInteger":
            case "xsd:short":
            case "xsd:byte":
            case "xsd:long":
            case "xsd:unsignedLong":
            case "xsd:unsignedInt":
            case "xsd:unsignedShort":
            case "xsd:unsignedByte": return pentaho.pda.Column.DATA_TYPES.NUMERIC;
            case "xsd:string": return pentaho.pda.Column.DATA_TYPES.STRING;
            case "xsd:dateTime": return pentaho.pda.Column.DATA_TYPES.DATE;
            case "Restrictions": return pentaho.pda.Column.DATA_TYPES.UNKNOWN;
            default: return pentaho.pda.Column.DATA_TYPES.UNKNOWN;
        }
    }
    

pentaho.pda.model.olap.prototype.getAllValuesForColumn = function( column ) {
  
        // find a measure to use
        var facts = this.getColumnsByFieldType([pentaho.pda.Column.ELEMENT_TYPES.FACT]);

        // find the dimension root of the column
        var root = column;
        while(root.parent != null) {
            root = root.parent;
        }

        // create the MDX statement for this
        var mdx = 
          "select "+facts[0].id+" ON COLUMNS, "+
          "Order({"+column.id+".Members},"+root.id+".CurrentMember.OrderKey, BASC) ON ROWS "+
          "from ["+this.cubeName+"]";

//alert(mdx);

        var properties = {};
        properties[Xmla.PROP_DATASOURCEINFO] = this.xmlaDatasource;
        properties[Xmla.PROP_CATALOG] = this.catalog;

        var restrictions = {};
        restrictions["CATALOG_NAME"] = this.catalog;
        restrictions["CUBE_NAME"] = this.cubeName;
        try {
/*
            var rowset = this.xmla.executeMultiDimensional({
                url: this.XMLA_SERVICE_URL,
                statement: mdx,
                async: false,
                properties: properties
            ,   restrictions: restrictions
            });
*/
            var rowset = this.xmla.executeTabular({
                url: this.XMLA_SERVICE_URL,
                statement: mdx,
                async: false,
                properties: properties
            ,   restrictions: restrictions
            });
            var results = {
                metadata:[],
                resultset:[]
            }

            var fields = rowset.getFields();
            var targetCol = fields.length-2; // the field with the members is the second to last one
            var fieldDef = fields[targetCol];
            var md = {
                colIndex: 0,
                colName : column.id,
                colType : this.getDataTypeFromXsd( fieldDef.type ),
                colLabel : column.name
            }

            while (rowset.hasMoreRows()){
                var row = new Array();
                row.push(rowset.fieldVal(targetCol));
                results.resultset.push(row);
                rowset.next();
            }

            return results;
        } catch (e) {
            alert(e.message);
        }
        return null;
    }

pentaho.pda.model.olap.prototype.searchColumn = function( column, searchStr ) {

        var all = this.getAllValuesForColumn(column);
        
        // now filter out the ones that don'y match
        var results = {
            metadata: all.metadata,
            resultset: []
        }
        
        for(var idx=0; idx<all.resultset.length; idx++) {
            if(all.resultset[idx][0].indexOf(searchStr) != -1) {
                results.resultset.push(all.resultset[idx]);
            }
        }
        return results;
    }


/* ******************************************
                        pentaho.pda.query.mql
   ******************************************						
*/
pentaho.pda.query.olap = function(model) {
	pentaho.pda.query.call(this, model); //call parent object
	
    this.state = {
        "mdx" : "mdx",
        columnSelections : [],
        rowSelections : [],
        measures : [],
        conditions : []
    };
    
}

inheritPrototype(pentaho.pda.query.olap, pentaho.pda.query); //borrow the parent's methods

pentaho.pda.query.olap.prototype.prepare = function( ) {
    this.state.mdx = this.getMdx(this);
    }

pentaho.pda.query.olap.prototype.getQueryStr = function() {
    return this.state.mdx;
}

pentaho.pda.query.olap.prototype.createSelection = function() {
        var selection = {
            column: null,
            selection:null,
        };
        return selection;
    }

pentaho.pda.query.olap.prototype.createCondition = function() {
        var condition = {
            "column" : null,
            "operator" : null,
            "value" : null,
            "combinationType" : pentaho.pda.Column.OPERATOR_TYPES.AND
        }
        return condition;
    }

pentaho.pda.query.olap.prototype.addConditionById = function(columnId, operator, value, combinationType) {
        var column = this.model.getColumnById( columnId );
        if(column != null) {
            var condition = this.createCondition();
            condition.column = column;
            condition.operator = operator;
            if(typeof value == 'object' && value.length) {
                condition.value = value;
            } else {
                condition.value = [ value ];
            }
            condition.combinationType = combinationType;
            this.addCondition( condition );
            return condition;
        }
        return null;
    }

pentaho.pda.query.olap.prototype.addCondition = function( condition ) {
        this.state.conditions.push( condition );
    }

pentaho.pda.query.olap.prototype.addSelectionById = function( columnId, location ) {
        var column = this.model.getColumnById( columnId );
        if(column != null) {
        
            var selection = this.createSelection();
            selection.column = column;
            if(column.elementType == pentaho.pda.Column.ELEMENT_TYPES.FACT) {
                // its a measure
                this.state.measures.push(selection);
            }

            if(column.elementType == pentaho.pda.Column.ELEMENT_TYPES.LEVEL) {
                // its a level
                if( location == pentaho.pda.AXIS_LOCATION_ACROSS ) {
                    this.state.columnSelections.push(selection);
                } 
                else if( location == pentaho.pda.AXIS_LOCATION_DOWN ) {
                    this.state.rowSelections.push(selection);
                }
            }
        
            return column;
        }
        return null;
    }

    // submit a new query
pentaho.pda.query.olap.prototype.getMdx = function( ) {

        if(this.state.measures.length == 0 && this.state.rowSelections.length == 0 && this.state.columnSelections.length == 0 ) {
            // we can't construct a query for this
            return '';
        }
        
        // find a measure to use
        // TODO use the default measure if available
        var measures = this.state.measures;
        var measureAdded = false;
        if(measures.length == 0) {
            var column = this.model.getColumnsByFieldType([pentaho.pda.Column.ELEMENT_TYPES.FACT])[0]
            var selection = this.createSelection();
            selection.column = column;
            measures = [selection];
            measureAdded = true;
        }
 
        // create the MDX statement for this
        var mdx = "select ";
        var mdxFrag = new Array();

        for(var idx=0; idx<this.state.columnSelections.length; idx++) {
            mdxFrag.push( this.getSelectionMdx(this.state.columnSelections[idx],this.state.conditions) );
        }
        mdxFrag.push( this.getMeasuresMdx(measures) );
        if( mdxFrag.length == 1 ) {
            mdx += mdxFrag[0];
        } else {
            // we need a crossjoin
            mdx += 'Crossjoin(';
            for(var idx=0; idx<mdxFrag.length; idx++) {
                if(idx > 0) {
                    mdx += ', ';
                }
                mdx += mdxFrag[idx];
            }
            mdx += ')';
            
        }
        
        mdx += " ON COLUMNS, ";
        
        var mdxFrag = new Array();
        for(var idx=0; idx<this.state.rowSelections.length; idx++) {
            mdxFrag.push( this.getSelectionMdx(this.state.rowSelections[idx],this.state.conditions) );
        }
        
        if( mdxFrag.length == 1 ) {
            mdx += mdxFrag[0];
        } else {
            // we need a crossjoin
            mdx += 'Crossjoin(';
            for(var idx=0; idx<mdxFrag.length; idx++) {
                if(idx > 0) {
                    mdx += ', ';
                }
                mdx += mdxFrag[idx];
            }
            mdx += ')';
            
        }
        
        mdx += " ON ROWS from ["+this.model.cubeName+"]";

        mdx += this.getWhereMdx();
//alert(mdx);
        return mdx;
    }

pentaho.pda.query.olap.prototype.getWhereMdx = function() {
        var mdxfrag = ' where (';
        
        var found = false;
        
        for(var idx=0; idx<this.state.conditions.length; idx++) {
            if(this.state.conditions[idx].column.elementType == pentaho.pda.Column.ELEMENT_TYPES.FACT) {
                if(idx > 0) {
                    mdxfrag += ' '+ this.state.conditions[idx].combinationType +' ';
                } 
                mdxfrag += this.state.conditions[idx].column.id;
                mdxfrag += ' '+this.state.conditions[idx].operator+' ';
                mdxfrag += this.state.conditions[idx].value[0];
                found = true;
            } else {
                // add any conditions that do not have a selection
                var hasSelection = false;
                for( var s=0; s<this.state.rowSelections.length; s++ ) {
                    if( this.state.rowSelections[s].column.id == this.state.conditions[idx].column.id) {
                        hasSelection = true;
                        break;
                    }
                }
                for( var s=0; s<this.state.columnSelections.length; s++ ) {
                    if( this.state.columnSelections[s].column.id == this.state.conditions[idx].column.id) {
                        hasSelection = true;
                        break;
                    }
                }
                if( !hasSelection ) {
                    mdxfrag += this.state.conditions[idx].column.id;
                    mdxfrag += '.[';
                    mdxfrag += this.state.conditions[idx].value[0];
                    mdxfrag += ']';
                    found = true;
                }
            }
        }        
        
        mdxfrag += ')'
        if (found) {
            return mdxfrag;
        } else {
            return '';
        }
    }
    
pentaho.pda.query.olap.prototype.getMeasuresMdx = function(measures) {
        var mdx = '';
        if( measures.length > 1 ) {
            mdx += '{';
        }
        for(var idx=0; idx<measures.length; idx++) {
            if(idx > 0) {
                mdx += ', ';
            } 
            mdx += measures[idx].column.id;
        }
        if( measures.length > 1 ) {
            mdx += '}';
        }
        return mdx;
    }
    
pentaho.pda.query.olap.prototype.getSelectionMdx = function(selection, conditions) {

        // see if we have member selections
        var doneMembers = false;
        var needBrace = false;
        var mdxfrag = '';
        for(var c=0; c<conditions.length; c++ ) {
            if(conditions[c].column.id == selection.column.id ) {
                var values = conditions[c].value;
                for(var v=0; v<values.length; v++) {
                    if(doneMembers) {
                        mdxfrag += ', ';
                    needBrace = true;
                    }
                    mdxfrag += selection.column.id;
                    mdxfrag += '.[';
                    mdxfrag += values[v];
                    mdxfrag += ']';
                    doneMembers = true;
                }
            }
        }
        if(!doneMembers) {
            // do all the members
            mdxfrag += selection.column.id;
            mdxfrag += '.Members';
        }
        
        if( needBrace ) {
           return '{'+mdxfrag+'}';
        } else {
            return mdxfrag; 
        }
    }
