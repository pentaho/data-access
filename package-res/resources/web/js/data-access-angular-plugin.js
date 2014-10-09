/*
 * Defines the plugin for the angular views for data access
 */

var deps = [
	'mantle/puc-api/pucAngularPlugin', 
	"content/data-access-v2/resources/web/js/data-access-controllers",
	"content/data-access-v2/resources/web/js/data-access-services",
	"content/data-access-v2/resources/web/js/data-access-directives"
];

require(deps, function(PUCAngularPlugin, controllers, services, directives) {
	pen.startDataAccess = function() {
		// plugin.goto("/data-access/manage-data-sources");
		plugin.goto("/data-access/manage-data-sources");
	}

	// Routes
	var routes = function($routeProvider) {
		$routeProvider
			.when("/data-access/manage-data-sources", {
				templateUrl : "content/data-access-v2/resources/web/partials/manage-data-sources.html",
				controller : "ManageDataAccessController"		
			})
			.when("/data-access/new-data-source", {
				templateUrl : "content/data-access-v2/resources/web/partials/new-data-source.html",
				controller : "NewDataSourceController"
			})
			.when("/data-access/edit-data-source", {
				templateUrl : "content/data-access-v2/resources/web/partials/edit-data-source-connection.html",
				controller : "EditDataSourceController"
			});
	}

	var plugin = new PUCAngularPlugin({
		routerCallback : routes,
		controllerCallback : controllers,
		serviceCallback : services,
		directiveCallback : directives
	}).register();
})