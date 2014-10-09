define(function() {
	var services = function($serviceProvider) {

		$serviceProvider("ManageDataSourceService", ["$http", function($http) {
			
			this.getJDBCDataSources = function(successCallback) {
				$http({method: 'GET', url: 'plugin/data-access/api/datasource/jdbc/connection'}).
				success(function(data, status, headers, config) {
					var sources = [];
					for (key in data) {
						var item = data[key];
						for (i in item) {
							sources.push({
								type: "JDBC",
								value: item[i]["$"]
							});
						}
					}
					successCallback(sources);
				}).
				error(function(data, status, headers, config) {
					alert("fail");
				});
			}

			this.getDSWDataSources = function(successCallback) {
				$http({method: 'GET', url: 'plugin/data-access/api/datasource/dsw/domain'}).
					success(function(data, status, headers, config) {
					    var sources = [];
						for (key in data) {
							sources.push({
								type: "Data Source Wizard",
								value: data[key]["$"]
							});
						}
						successCallback(sources);
					}).
					error(function(data, status, headers, config) {
						alert("fail");
					});
			}

			this.getAnalysisDataSources = function(successCallback) {
				$http({method: 'GET', url: 'plugin/data-access/api/datasource/analysis/catalog'}).
					success(function(data, status, headers, config) {
					    var sources = [];
						for (key in data) {
							var item = data[key];
							for (i in item) {
								sources.push({
									type: "Analysis",
									value: item[i]["$"]
								});
							}
						}
						successCallback(sources);
					}).
					error(function(data, status, headers, config) {
						alert("fail");
					});
			}

			this.getMetadataDataSources = function(successCallback) {
				$http({method: 'GET', url: 'plugin/data-access/api/datasource/metadata/domain'}).
					success(function(data, status, headers, config) {
					    var sources = [];
						for (key in data) {
							var item = data[key];
							for (i in item) {
								sources.push({
									type: "Metadata",
									value: item[i]["$"]
								});
							}
						}
						successCallback(sources);
					}).
					error(function(data, status, headers, config) {
						alert("fail");
					});
			}
		}]);
	}

	return services;
})