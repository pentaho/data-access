//= require "oop.js"
//= require "app.js"

pentaho.pda = pentaho.pda || {};

pentaho.pda.SOURCE_TYPE_OLAP = 'olap';
pentaho.pda.SOURCE_TYPE_MQL = 'mql';
pentaho.pda.SOURCE_TYPE_SQL = 'sql';
pentaho.pda.SOURCE_TYPE_ETL = 'etl';
pentaho.pda.SOURCE_TYPE_CDA = 'cda';

pentaho.pda.modelaccess = {};

/*
          @class pentaho.app.dataaccess
*/
pentaho.pda.app = function(){
	pentaho.app.call(this); //call parent object
	this.sources = [];  //cache of sources
	this.attempts = 0;
}

inheritPrototype(pentaho.pda.app, pentaho.app); //borrow the parent's methods

pentaho.pda.app.prototype.discoverSources = function(callback) {

	if (typeof callback !== 'function') {
		callback = false;
	}
	
	//should only be here if we don'thave sources already
	var md = this.moduleData, src={}, handler, that=this, sources = [];
	for (src in md) {
		var h = md[src].instance;
		if (h instanceof pentaho.pda.Handler) {
			h.getSources( //{property:'name', value:'Customers'},
				function(source){
					that.addSource(source);
					sources.push(source);
					if (callback) {
						callback(source);
					}
				}
			)
		} else {
			throw new Error(md[src].name + ' is not an instanceof pentaho.pda.Handler');
		}
	}

	return sources;
}

pentaho.pda.app.prototype.getSources = function(filter, callback) {

	var i,j,_sources = [], each;
	if (arguments.length == 1 && typeof filter == 'function' ) {
		callback = filter;
		filter = null;
	}

	if (this.sources.length > 0) {
//		console.log('using existing sources');
		if (filter == null ) {
//			console.log('no filter used');
			for (var i=0,j=this.sources.length;i<j;i++) {
				if (callback) {
					callback(this.sources[i]);
				}
			}
		} else {
//			console.log('using filter');
//			console.log(filter);
			_soruces = [];
			for (var i=0,j=this.sources.length;i<j;i++) {
				each = this.sources[i];
				try {
					if (each[filter.property] == filter.value) {
						_sources.push(each);
						if (callback) {
							callback(each);
						}
					}
				} catch(e) {
					//just move on to next
				}
			}
			this.sources = _sources;
		}
		if (this.sources.length == 1) {
			return this.sources[0];
		} else {
//			console.log('returning all sources');
			return this.sources;
		}
	} else { // length == 0
//		console.log('no sources');
		var that = this;
		this.discoverSources(
			function(source) {
				if (filter == null) {
					_sources.push(source);
					if (callback) {
						callback(source);
					}
				} else {
					try {
						if (source[filter.property] == filter.value) {
							_sources.push(source);
							if (callback) {
								callback(source);
							}
						} else {
							//console.log('no match for:' + each[filter.property] + ':' + filter.value);
						}
					} catch(e) {
						//just move on to next
//						console.log(e);
					}
				}
			}
		);
		if (_sources.length == 1) {
			return _sources[0];
		} else {
			return _sources;
		}
		
	}
	
}

pentaho.pda.app.prototype.addSource = function(source) {
	this.sources.push(source);
}

pentaho.pda.app.prototype.sortData = function( results, columnIdx, direction ) {

    if( columnIdx == -1 ) {
        // no sorting to apply
        return;
    }
    // create a new data set
    var sorted = { 
        "metadata" : results.metadata,
        "resultset" : []
    };
    
    // TODO support multi-level sort
    try {
        // TODO find a better way to do this without using globals
        pentaho.pda.app.prototype.sortedColumnIdx = columnIdx;
        pentaho.pda.app.prototype.sortDirection = direction;
        sorted.resultset = results.resultset.sort( pentaho.pda.app.prototype.compareRows );
    } catch (e) {
        alert(e.message);
    }
    return sorted;    

}

pentaho.pda.app.prototype.sortedColumnIdx = -1;
pentaho.pda.app.prototype.sortDirection = null;

pentaho.pda.app.prototype.compareRows = function( row1, row2 ) {
    if( row1[pentaho.pda.app.prototype.sortedColumnIdx] == row2[pentaho.pda.app.prototype.sortedColumnIdx] ) {
        return 0;
    }
    if(pentaho.pda.app.prototype.sortDirection == pentaho.pda.Column.SORT_TYPES.ASCENDING ) {
        return ( row1[pentaho.pda.app.prototype.sortedColumnIdx] > row2[pentaho.pda.app.prototype.sortedColumnIdx] ) ? 1 : -1;
    } else {
        return ( row1[pentaho.pda.app.prototype.sortedColumnIdx] < row2[pentaho.pda.app.prototype.sortedColumnIdx] ) ? 1 : -1;
    } 
}

pentaho.pda.Handler = function(sandbox) {
	this.sandbox = sandbox;
	this.sources = [];
}

pentaho.pda.Handler.prototype.init = function init() {
}

/*
          @class pentaho.pda.model
*/
pentaho.pda.model = function(obj){
	this.id          = obj.id || 1;
	this.name        = obj.name || 'unknown';
	this.type        = obj.type || 'mql';
	this.description = obj.description || '';
    this.elements    = obj.elements || [];
    this.capabilities = obj.capabilities || {};
}

pentaho.pda.model.prototype.getNodeText = function( node, tag ) {
        for( var idx=0; idx<node.childNodes.length; idx++ ) {
            if(node.childNodes[idx].nodeName == tag) {
                return this.getText( node.childNodes[idx] );
            }
        }
        return null;
} //getNodeText
    
pentaho.pda.model.prototype.getNodeTextOfChild = function( node, tag1, tag2 ) {
        for( var idx=0; idx<node.childNodes.length; idx++ ) {
            if(node.childNodes[idx].nodeName == tag1) {
                return this.getNodeText( node.childNodes[idx], tag2 );
            }
        }
        return null;
} //getNodeTextOfChild

pentaho.pda.model.prototype.getText = function(node) {
        if(!node || !node.firstChild) return null;
        if(typeof(node.textContent) != "undefined") return node.textContent;
        return node.firstChild.nodeValue;
} //getText

pentaho.pda.model.prototype.hasCapability = function( capability ) {
        var value = this.capabilities[capability];
        if( typeof value == 'undefined' ) {
            return false;
        }
        return true;
}

pentaho.pda.model.prototype.getCapabilityValue = function( capability ) {
        return this.capabilities[capability];
    }

pentaho.pda.model.prototype.addCapability = function( capability, value ) {
        if( typeof value == 'undefined' ) {
          value = true;  
        }
        this.capabilities[capability] = value;
    }

pentaho.pda.model.prototype.getCapabilityNames = function() {
        var list = new Array();
        for( x in this.capabilities ) {
            list.push( x );
        }
        return list;
    }

pentaho.pda.model.prototype.addElement = function( element ) {
        this.elements.push( element );
    }

pentaho.pda.model.prototype.getAllElements = function() {
        return this.elements;
    }

pentaho.pda.model.prototype.getElementsOfType = function( type ) {
        return this.getElementsOfTypes( new Array( type ) );
    }

pentaho.pda.model.prototype.getElementsOfTypes = function( types ) {
        var elements = new Array();
        for( var idx=0; idx<this.elements.length; idx++ ) {
            for( var typeNo=0; typeNo<types.length; typeNo++ ) {
                if( this.elements[idx].elementType == types[typeNo] ) {
                    elements.push( this.elements[idx] );
                }
            }
        }
        return elements;
    }

pentaho.pda.model.prototype.getElementsOfDataType = function( type ) {
        return this.getElementsOfDataTypes( new Array( type ) );
    }

pentaho.pda.model.prototype.getElementsOfDataTypes = function( types ) {
        var elements = new Array();
        for( var idx=0; idx<this.elements.length; idx++ ) {
            for( var typeNo=0; typeNo<types.length; typeNo++ ) {
                if( this.elements[idx].dataType == types[typeNo] ) {
                    elements.push( this.elements[idx] );
                }
            }
        }
        return elements;
    }

pentaho.pda.model.prototype.getQueryElementsByFieldType = function( fieldType ) {
        var fieldTypes = new Array();
        fieldTypes.push( fieldType );
        return this.getColumnsByFieldTypes(fieldTypes, false);
    }
	
pentaho.pda.model.prototype.getColumnsByFieldType = function( fieldType ) {
        var fieldTypes = new Array();
        fieldTypes.push( fieldType );
        return this.getColumnsByFieldTypes(fieldTypes, true);
    }
    
pentaho.pda.model.prototype.getColumnsByFieldTypes = function( fieldTypes, allElements ) {
        var cols = this.getAllColumns();
        var columns = new Array();
        if( !fieldTypes ) {
            return cols;
        }
        for( var colNo=0; colNo<cols.length; colNo++ ) {
            for( var typeNo=0; typeNo<fieldTypes.length; typeNo++ ) {
                if( cols[colNo].elementType == fieldTypes[typeNo] && (cols[colNo].isQueryElement || allElements) ) {
                    columns.push( cols[colNo] );
                }
            }
        }
        return columns;
    }    
    
pentaho.pda.model.prototype.getQueryElementsByDataType = function( dataType ) {
        var dataTypes = new Array();
        dataTypes.push( dataType );
        return this.getColumnsByDataTypes(dataTypes, false);
    }
        
pentaho.pda.model.prototype.getColumnsByDataType = function( dataType ) {
        var dataTypes = new Array();
        dataTypes.push( dataType );
        return this.getColumnsByDataTypes(dataTypes, true);
    }
    
pentaho.pda.model.prototype.getColumnsByDataTypes = function( dataTypes, allElements ) {
        var cols = this.getAllColumns();
        var columns = new Array();
        if( !dataTypes ) {
            return cols;
        }
        for( var colNo=0; colNo<cols.length; colNo++ ) {
            for( var typeNo=0; typeNo<dataTypes.length; typeNo++ ) {
                if( cols[colNo].dataType == dataTypes[typeNo] && (cols[colNo].isQueryElement || allElements) ) {
                    columns.push( cols[colNo] );
                }
            }
        }
        return columns;
    }    
    
pentaho.pda.model.prototype.getColumnById = function( id ) {
        var cols = this.getAllColumns();
        for( var colNo=0; colNo<cols.length; colNo++ ) {
            if( cols[colNo].id == id ) {
                return cols[colNo];
            }
        }
        return null;
    }
    
pentaho.pda.model.prototype.sortColumnsByName = function( columns ) {
        return columns.sort( function( c1, c2 ) { return ( c1.name == c2.name ) ? 0 : ( c1.name > c2.name ) ? 1 : -1 } )
    }    
    
pentaho.pda.model.prototype.createListOptions = function (columnList ) {
        var options = new Array();
        for( var idx=0; idx<columnList.length; idx++ ) {
            var option = new Option( columnList[idx].name, columnList[idx].id );
            options.push( option );
        }
        return options;
    }
        
pentaho.pda.model.prototype.populateListFromResults = function( valuesList, results, textColumnNumber, valueColumnNumber ) {
        var hasValues = false;
        if( ''+valueColumnNumber != 'undefined' ) {
            if( valueColumnNumber >=0 && valueColumnNumber < results.columnNames.length ) {
                hasValues = true;
            }
        }
        for( var idx=0; idx<results.resultset.length; idx++ ) {
            var option;
            if( hasValues ) {
                option = new Option( results.resultset[idx][textColumnNumber], results.resultset[idx][valueColumnNumber] );
            } else {
                option = new Option( results.resultset[idx][textColumnNumber] );
            }
            valuesList.options[valuesList.options.length] = option;
        }
    }
        
        
pentaho.pda.dataelement = function() {
	this.dataType = pentaho.pda.Column.DATA_TYPES.UNKNOWN;
this.elementType = pentaho.pda.Column.ELEMENT_TYPES.UNKNOWN;
this.id = '';
this.name = '';
this.description = '';
this.defaultAggregation = pentaho.pda.Column.AGG_TYPES.NONE;
this.selectedAggregation = pentaho.pda.Column.AGG_TYPES.NONE;
this.availableAggregations = new Array();
    this.parent = null;
    this.children = new Array();
    this.isQueryElement = false;
    this.capabilities = {};
}

pentaho.pda.dataelement.prototype.addChild = function( child ) {
    this.children.push( child );
}

pentaho.pda.query = function(model) {
    this.model = model;
}

// constants

pentaho.pda.AXIS_LOCATION_ACROSS = 'across';
pentaho.pda.AXIS_LOCATION_DOWN = 'down';
pentaho.pda.AXIS_LOCATION_SLICER = 'slicer';

pentaho.pda.SORT_DIRECTION_ASC = 'ASC';
pentaho.pda.SORT_DIRECTION_DESC = 'DESC';

pentaho.pda.MODEL = {};
pentaho.pda.CAPABILITIES = new Object();
pentaho.pda.CAPABILITIES.HAS_ACROSS_AXIS = "across-axis";
pentaho.pda.CAPABILITIES.IS_ACROSS_CUSTOM = "across-axis-customizable";
pentaho.pda.CAPABILITIES.HAS_DOWN_AXIS = "down-axis";
pentaho.pda.CAPABILITIES.IS_DOWN_CUSTOM = "down-axis-customizable";
pentaho.pda.CAPABILITIES.HAS_FILTERS = "filter-axis";
pentaho.pda.CAPABILITIES.IS_FILTER_CUSTOM = "filter-axis-customizable";
pentaho.pda.CAPABILITIES.CAN_SORT = "sortable";

pentaho.pda.Column = {};
pentaho.pda.Column.SORT_TYPES = new Object();
pentaho.pda.Column.SORT_TYPES.ASCENDING = "ASC";
pentaho.pda.Column.SORT_TYPES.DESCENDING = "DESC";

pentaho.pda.Column.OPERATOR_TYPES = new Object();
pentaho.pda.Column.OPERATOR_TYPES.OR = 'OR';
pentaho.pda.Column.OPERATOR_TYPES.OR_NOT = 'OR NOT';
pentaho.pda.Column.OPERATOR_TYPES.AND = 'AND';
pentaho.pda.Column.OPERATOR_TYPES.AND_NOT = 'AND NOT';

pentaho.pda.Column.CONDITION_TYPES = new Object();
pentaho.pda.Column.CONDITION_TYPES.LIKE = 'LIKE';
pentaho.pda.Column.CONDITION_TYPES.BEGINSWITH = 'BEGINS WITH';
pentaho.pda.Column.CONDITION_TYPES.ENDSWITH = 'ENDS WITH';
pentaho.pda.Column.CONDITION_TYPES.CONTAINS = 'CONTAINS';
pentaho.pda.Column.CONDITION_TYPES.NOT_CONTAINS = 'DOES NOT CONTAIN';
pentaho.pda.Column.CONDITION_TYPES.EQUAL = 'EQUAL';
pentaho.pda.Column.CONDITION_TYPES.LESS_THAN = '<';
pentaho.pda.Column.CONDITION_TYPES.LESS_THAN_OR_EQUAL = '<=';
pentaho.pda.Column.CONDITION_TYPES.MORE_THAN = '>';
pentaho.pda.Column.CONDITION_TYPES.MORE_THAN_OR_EQUAL = '>=';
pentaho.pda.Column.CONDITION_TYPES.IS_NULL = 'IS NULL';
pentaho.pda.Column.CONDITION_TYPES.NOT_NULL = 'IS NOT NULL';

pentaho.pda.Column.DATA_TYPES = new Object();
pentaho.pda.Column.DATA_TYPES.NUMERIC = "NUMERIC";
pentaho.pda.Column.DATA_TYPES.STRING = "STRING";
pentaho.pda.Column.DATA_TYPES.DATE = "DATE";
pentaho.pda.Column.DATA_TYPES.BOOLEAN = "BOOLEAN";
pentaho.pda.Column.DATA_TYPES.UNKNOWN = "UNKNOWN";
pentaho.pda.Column.DATA_TYPES.NONE = "NONE";

pentaho.pda.Column.ELEMENT_TYPES = new Object();
pentaho.pda.Column.ELEMENT_TYPES.CATEGORY = "CATEGORY";
pentaho.pda.Column.ELEMENT_TYPES.CUBE = "CUBE";
pentaho.pda.Column.ELEMENT_TYPES.DIMENSION = "DIMENSION";
pentaho.pda.Column.ELEMENT_TYPES.HIERARCHY = "HIERARCHY";
pentaho.pda.Column.ELEMENT_TYPES.LEVEL = "LEVEL";
pentaho.pda.Column.ELEMENT_TYPES.MEMBER = "MEMBER";
pentaho.pda.Column.ELEMENT_TYPES.FACT = "FACT";
pentaho.pda.Column.ELEMENT_TYPES.ATTRIBUTE = "ATTRIBUTE";
pentaho.pda.Column.ELEMENT_TYPES.KEY = "KEY";
pentaho.pda.Column.ELEMENT_TYPES.QUERY = "QUERY";
pentaho.pda.Column.ELEMENT_TYPES.UNKNOWN = "UNKNOWN";

// values are from java.sql.Types
pentaho.pda.Column.JAVA_SQL_TYPES = new Object();
pentaho.pda.Column.JAVA_SQL_TYPES.NUMERIC = 2;
pentaho.pda.Column.JAVA_SQL_TYPES.VARCHAR = 12;
pentaho.pda.Column.JAVA_SQL_TYPES.BOOLEAN = 16;
pentaho.pda.Column.JAVA_SQL_TYPES.DATE = 91;

pentaho.pda.Column.AGG_TYPES = new Object();
pentaho.pda.Column.AGG_TYPES.SUM = 'SUM';
pentaho.pda.Column.AGG_TYPES.AVERAGE = 'AVERAGE';
pentaho.pda.Column.AGG_TYPES.MIN = 'MINIMUM';
pentaho.pda.Column.AGG_TYPES.MAX = 'MAXIMUM';
pentaho.pda.Column.AGG_TYPES.COUNT = 'COUNT';
pentaho.pda.Column.AGG_TYPES.COUNT_DISTINCT = 'COUNT_DISTINCT';
pentaho.pda.Column.AGG_TYPES.NONE = 'NONE';
pentaho.pda.Column.AGG_TYPES.VAR = 'VAR';
pentaho.pda.Column.AGG_TYPES.STDDEV = 'STDDEV';
pentaho.pda.Column.AGG_TYPES.CALC = 'CALC';
pentaho.pda.Column.AGG_TYPES.UNKNOWN = 'UNKNOWN';

pentaho.pda.Column.AGG_TYPE_MAP = new Object();
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.NONE] = 'none';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.SUM] = 'sum';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.AVERAGE] = 'average';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.MIN] = 'min';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.MAX] = 'max';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.COUNT] = 'item-count';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.COUNT_DISTINCT] = 'count-distinct';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.VAR] = 'var';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.STDDEV] = 'standard deviation';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.CALC] = 'calculated';
pentaho.pda.Column.AGG_TYPE_MAP[pentaho.pda.Column.AGG_TYPES.UNKNOWN] = 'unknown';

pentaho.pda.Column.TYPE_TO_JAVA_SQL_TYPE = new Object();
pentaho.pda.Column.TYPE_TO_JAVA_SQL_TYPE[ pentaho.pda.Column.DATA_TYPES.NUMERIC ] = pentaho.pda.Column.JAVA_SQL_TYPES.NUMERIC;
pentaho.pda.Column.TYPE_TO_JAVA_SQL_TYPE[ pentaho.pda.Column.DATA_TYPES.STRING ] = pentaho.pda.Column.JAVA_SQL_TYPES.VARCHAR;
pentaho.pda.Column.TYPE_TO_JAVA_SQL_TYPE[ pentaho.pda.Column.DATA_TYPES.DATE ] = pentaho.pda.Column.JAVA_SQL_TYPES.DATE;
pentaho.pda.Column.TYPE_TO_JAVA_SQL_TYPE[ pentaho.pda.Column.DATA_TYPES.BOOLEAN ] = pentaho.pda.Column.JAVA_SQL_TYPES.BOOLEAN;
pentaho.pda.Column.TYPE_TO_JAVA_SQL_TYPE[ pentaho.pda.Column.DATA_TYPES.UNKNOWN ] = pentaho.pda.Column.JAVA_SQL_TYPES.VARCHAR;	// assume unknown will be a string

pentaho.pda.Column.JAVA_SQL_TYPE_TO_TYPE = new Object();
pentaho.pda.Column.JAVA_SQL_TYPE_TO_TYPE[ pentaho.pda.Column.JAVA_SQL_TYPES.NUMERIC ] = pentaho.pda.Column.DATA_TYPES.NUMERIC;
pentaho.pda.Column.JAVA_SQL_TYPE_TO_TYPE[ pentaho.pda.Column.JAVA_SQL_TYPES.VARCHAR ] = pentaho.pda.Column.DATA_TYPES.STRING;
pentaho.pda.Column.JAVA_SQL_TYPE_TO_TYPE[ pentaho.pda.Column.JAVA_SQL_TYPES.DATE ] = pentaho.pda.Column.DATA_TYPES.DATE;
pentaho.pda.Column.JAVA_SQL_TYPE_TO_TYPE[ pentaho.pda.Column.JAVA_SQL_TYPES.BOOLEAN ] = pentaho.pda.Column.DATA_TYPES.BOOLEAN;

pentaho.pda.Column.COMPARATOR = new Object();
pentaho.pda.Column.COMPARATOR.STRING = [ 
	[Messages.getString( "EXACTLY_MATCHES" ), pentaho.pda.Column.CONDITION_TYPES.EQUAL],
	[Messages.getString( "CONTAINS" ), pentaho.pda.Column.CONDITION_TYPES.CONTAINS],
	[Messages.getString( "ENDS_WITH" ), pentaho.pda.Column.CONDITION_TYPES.ENDSWITH],
	[Messages.getString( "BEGINS_WITH" ), pentaho.pda.Column.CONDITION_TYPES.BEGINSWITH],
  [Messages.getString( "DOES_NOT_CONTAIN" ), pentaho.pda.Column.CONDITION_TYPES.NOT_CONTAINS],
  [Messages.getString( "IS_NULL" ), pentaho.pda.Column.CONDITION_TYPES.IS_NULL],
  [Messages.getString( "IS_NOT_NULL" ), pentaho.pda.Column.CONDITION_TYPES.NOT_NULL]];
pentaho.pda.Column.COMPARATOR.NUMERIC = [
  [Messages.getString( "EQUALS" ), pentaho.pda.Column.CONDITION_TYPES.EQUAL],
//  ["<>", ],
  [Messages.getString( "MORE_THAN_OR_EQUAL" ), pentaho.pda.Column.CONDITION_TYPES.MORE_THAN_OR_EQUAL],
  [Messages.getString( "LESS_THAN_OR_EQUAL" ), pentaho.pda.Column.CONDITION_TYPES.LESS_THAN_OR_EQUAL],
  [Messages.getString( "MORE_THAN" ), pentaho.pda.Column.CONDITION_TYPES.MORE_THAN],
  [Messages.getString( "LESS_THAN" ), pentaho.pda.Column.CONDITION_TYPES.LESS_THAN],
  [Messages.getString( "IS_NULL" ), pentaho.pda.Column.CONDITION_TYPES.IS_NULL],
  [Messages.getString( "IS_NOT_NULL" ), pentaho.pda.Column.CONDITION_TYPES.NOT_NULL]];
pentaho.pda.Column.COMPARATOR.BOOLEAN = [
  ["=", pentaho.pda.Column.CONDITION_TYPES.EQUAL],
//  ["<>", ],
  [Messages.getString( "IS_NULL" ), pentaho.pda.Column.CONDITION_TYPES.IS_NULL],
  [Messages.getString( "IS_NOT_NULL" ), pentaho.pda.Column.CONDITION_TYPES.NOT_NULL]];
pentaho.pda.Column.COMPARATOR.DATE = [ 
	[Messages.getString( "ON" ), pentaho.pda.Column.CONDITION_TYPES.EQUAL],
//	[Messages.getString( "NOT_ON" ),
	[Messages.getString( "ON_OR_AFTER" ), pentaho.pda.Column.CONDITION_TYPES.MORE_THAN_OR_EQUAL],
	[Messages.getString( "ON_OR_BEFORE" ), pentaho.pda.Column.CONDITION_TYPES.LESS_THAN_OR_EQUAL],
	[Messages.getString( "AFTER" ), pentaho.pda.Column.CONDITION_TYPES.MORE_THAN],
  [Messages.getString( "BEFORE" ), pentaho.pda.Column.CONDITION_TYPES.LESS_THAN],
  [Messages.getString( "IS_NULL" ), pentaho.pda.Column.CONDITION_TYPES.IS_NULL],
  [Messages.getString( "IS_NOT_NULL" ), pentaho.pda.Column.CONDITION_TYPES.NOT_NULL]];

//Comparators with no right-hand parameters (is null, etc).
pentaho.pda.Column.SINGLE_COMPARATORS = {};
pentaho.pda.Column.SINGLE_COMPARATORS[pentaho.pda.Column.CONDITION_TYPES.IS_NULL] = {};
pentaho.pda.Column.SINGLE_COMPARATORS[pentaho.pda.Column.CONDITION_TYPES.NOT_NULL] = {};
  
pentaho.pda.Column.COMPARATOR_MAP = new Object();
pentaho.pda.Column.COMPARATOR_MAP[ pentaho.pda.Column.DATA_TYPES.NUMERIC ] = pentaho.pda.Column.COMPARATOR.NUMERIC;
pentaho.pda.Column.COMPARATOR_MAP[ pentaho.pda.Column.DATA_TYPES.STRING ] = pentaho.pda.Column.COMPARATOR.STRING;
pentaho.pda.Column.COMPARATOR_MAP[ pentaho.pda.Column.DATA_TYPES.DATE ] = pentaho.pda.Column.COMPARATOR.DATE;
pentaho.pda.Column.COMPARATOR_MAP[ pentaho.pda.Column.DATA_TYPES.BOOLEAN ] = pentaho.pda.Column.COMPARATOR.BOOLEAN;
pentaho.pda.Column.COMPARATOR_MAP[ pentaho.pda.Column.DATA_TYPES.UNKNOWN ] = pentaho.pda.Column.COMPARATOR.STRING;

/*static*/pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE = new Object();
pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.NUMERIC ] = "default";
pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.STRING ] = "";
pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.DATE ] = "default";
pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.BOOLEAN ] = "";
pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.UNKNOWN ] = "";

pentaho.pda.Column.ALIGNMENT_TYPES = new Object();
pentaho.pda.Column.ALIGNMENT_TYPES.DEFAULT = "not-set";
pentaho.pda.Column.ALIGNMENT_TYPES.LEFT = "left";
pentaho.pda.Column.ALIGNMENT_TYPES.RIGHT = "right";

/*static*/pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE = new Object();
pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.NUMERIC ] = pentaho.pda.Column.ALIGNMENT_TYPES.DEFAULT;
pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.STRING ] = pentaho.pda.Column.ALIGNMENT_TYPES.DEFAULT;
pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.DATE ] = pentaho.pda.Column.ALIGNMENT_TYPES.DEFAULT;
pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.BOOLEAN ] = pentaho.pda.Column.ALIGNMENT_TYPES.DEFAULT;
pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE[ pentaho.pda.Column.DATA_TYPES.UNKNOWN ] = pentaho.pda.Column.ALIGNMENT_TYPES.DEFAULT;

/**
 * @param columnType String, one of the properties of the BVItem.TYPE Object.
 */
pentaho.pda.Column.getDefaultFormat = function( columnType )
{
	return pentaho.pda.Column.DEFAULT_FORMAT_BY_TYPE[ columnType ];
}
pentaho.pda.Column.getDefaultAlignment = function( columnType )
{
	return pentaho.pda.Column.DEFAULT_ALIGNMENT_BY_TYPE[ columnType ];
}


// TODO move this stuff to a helper object

pentaho.pda.model.prototype.getColumnFromList = function( list ) {
        if( list ) {
            var itemId = list.value;
            return this.getColumnById( itemId );
        }
        return null;
    }

pentaho.pda.model.prototype.populateListControl = function( list, fieldTypes, sorted, selectedItem ) {

        var cols = this.getColumnsByFieldTypes(fieldTypes);
        if( sorted == true ) {
            cols = this.sortColumnsByName( cols );
        }
        var ops = this.createListOptions( cols );
        var options = list.options;
        var selectedId = null;
        if( selectedItem ) {
            if( selectedItem.id ) {
                selectedId = selectedItem.id;
            } else {
                selectedId = selectedItem;
            }
        }
        var selectedIdx = -1;
        for( colNo = 0; colNo < ops.length; colNo++ ) {
            options[options.length] = ops[colNo];
            if( ops[colNo].value == selectedId ) {
                selectedIdx = colNo;
            }
        }
        list.selectedIndex = selectedIdx;

    }
    

