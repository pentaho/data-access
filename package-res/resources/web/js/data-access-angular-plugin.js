/*
 * Defines the plugin for the angular views for data access
 */

pen.require(['mantle/puc-api/pucAngularApi'], function(PentahoPluginHandler) {
	pen.startDataAccess = function() {
		plugin.goNext("/data-access");
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
	var controllers = function($controllerProvider) {

		$controllerProvider("DataAccessController", ["$scope", 
			function($scope) {
				$scope.title="Data-access";
			}]);

		$controllerProvider("DataAccessController1", ["$scope", 
			function($scope) {
				$scope.title="Page1";
			}]);
	}	
	
	// TODO: retrieve data to populate the page
	var service = null;
	
	var plugin = new PentahoPluginHandler.Plugin({
		routerCallback : routes,
		controllerCallback : controllers
	}).register();
})