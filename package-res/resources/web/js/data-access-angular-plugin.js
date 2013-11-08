/*
 * Defines the plugin for the angular views for data access
 */

pen.require(['mantle/puc-api/pucAngularApi'], function(PentahoPluginHandler) {
	pen.startDataAccess = function() {
		PentahoPluginHandler.goNext("/data-access");
	}

	// Routes
	var routes = [{
		url : "/data-access",
		templateUrl : "content/data-access-v2/resources/web/partials/data-access.html",
		controller : "DataAccessController"
	}, {
		url : "/data-access-page1",
		templateUrl : "content/data-access-v2/resources/web/partials/data-access-page1.html",
		controller : "DataAccessController1"
	}, {
		url : "/data-access-page2",
		templateUrl : "content/data-access-v2/resources/web/partials/data-access-page2.html",
		controller : "DataAccessController2"
	}];
	
	// Controllers
	var controllers = [{
		name : "DataAccessController",
		def : ["$scope", function($scope) {
			$scope.title="Data-access";
		}]
	}, {
		name : "DataAccessController1",
		def : ["$scope", function($scope) {
			$scope.title="Page1";
		}]
	}, {
		name : "DataAccessController2",
		def : ["$scope", function($scope) {
		}]
	}];
	
	// TODO: retrieve data to populate the page
	var service = null;
	
	var plugin = new PentahoPluginHandler.PUCAngularPlugin(routes, controllers, service);
	plugin.register();
})