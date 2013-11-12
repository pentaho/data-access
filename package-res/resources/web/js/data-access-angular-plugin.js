/*
 * Defines the plugin for the angular views for data access
 */

pen.require(['mantle/puc-api/pucAngularApi'], function(PentahoPluginHandler) {
	pen.startDataAccess = function() {
		PentahoPluginHandler.goNext("/data-access");
	}

	// Routes
	var routes = function($routeProvider) {
		$routeProvider
			.when("/data-access", {
				templateUrl : "content/data-access-v2/resources/web/partials/data-access.html",
				controller : "DataAccessController"		
			})
			.when("/data-access-page1", {
				templateUrl : "content/data-access-v2/resources/web/partials/data-access-page1.html",
				controller : "DataAccessController1"
			})
			.when("/data-access-page2", {
				templateUrl : "content/data-access-v2/resources/web/partials/data-access-page2.html"
			});
	}
	
	// Controllers
	var controllers = function($controller) {

		$controller("DataAccessController", ["$scope", 
			function($scope) {
				$scope.title="Data-access";
			}]);

		$controller("DataAccessController1", ["$scope", 
			function($scope) {
				$scope.title="Page1";
			}]);
	}	
	
	// TODO: retrieve data to populate the page
	var service = null;
	
	var plugin = new PentahoPluginHandler.PUCAngularPlugin(routes, controllers, service);
	plugin.register();
})