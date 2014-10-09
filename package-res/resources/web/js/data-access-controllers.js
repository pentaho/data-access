
var deps = [
	"common-ui/util/PentahoSpinner", 
	"common-ui/util/spin.min"
]

define(deps, function(spinner, Spinner) {
	var runningSpinner;

	var showWaiting = function ($container) {
      var config = spinner.getLargeConfig();
      config.color = "#BBB";
      runningSpinner = new Spinner(config);
      var s = runningSpinner.spin();
      $container.append(s.el);
      // $(".data-access-dialog .glasspane").show();
    }

    var stopWaiting = function() {
		runningSpinner.stop();
		// $(".data-access-dialog .glasspane").fadeOut();
    }

	var controllers = function($controllerProvider) {

		// Manage Data Access Controller
		$controllerProvider("ManageDataAccessController", ["$scope", "$filter", "ManageDataSourceService",
			function($scope, $filter, service) {
				$scope.text = {};
				$scope.text.title = "Manage Data Source";
				$scope.text.nextButton = "Next";
				$scope.text.prevButton = "Close";
				$scope.text.search = "Search";
				$scope.text.type = "Type";
				$scope.text.name = "Data Source";
				$scope.text.newDataSource = "New Data Source";
				
				$scope.text.edit = "Edit...",
				$scope.text.delete = "Delete",
				$scope.text.export = "Export...",
				$scope.text.importAnalysis = "Import Analysis...",
				$scope.text.importMetadata = "Import Metadata...",
				$scope.text.newConnection = "New Connection..."

				$scope.data = [];
				$scope.typeReverse = 'false';
				$scope.nameReverse = 'false';

				$scope.arrowButtonClass = function(reverse) {
					return reverse ? "pentaho-upbutton" : "pentaho-downbutton";	
				}

				$scope.select = function($event) {
					$("#manage-data-sources-data .data").removeClass("selected");
					$($event.currentTarget).addClass("selected");
				}

				$scope.resize = function($event) {
					var $ele = $($event.target);

					var $parent = $ele.parent(".header-cell");
					var $parentDataChildren = $(".data .content-cell:nth-child(" + ($parent.index()+1) + ")");

					var $sibling = $parent.prev();
					var $siblingDataChildren = $(".data .content-cell:nth-child(" + ($sibling.index()+1) + ")");

					var startLeft = $ele.position().left;
					var startMouse = $event.clientX;
					var startSiblingWidth = $sibling.width();
					var startSiblingDataChildWidth = $siblingDataChildren.width();
					var startParentWidth = $parent.width();
					var startParentDataChildrenWidth = $parentDataChildren.width();

					var resize = function($event) {
						var diff = ($event.clientX - startMouse);
						var left = startLeft + diff;
						var siblingWidth = startSiblingWidth + diff;
						var parentWidth = startParentWidth - 1 - diff;
						var siblingDataChildWidth = startSiblingDataChildWidth + diff;
						var parentDataChildrenWidth = startParentDataChildrenWidth - 1 - diff;

						var minWidth = 30;
						if (siblingWidth <= minWidth || parentWidth <= minWidth) {
							return;
						}

						$ele.position().left = left;
						
						$sibling.width(siblingWidth);
						$siblingDataChildren.width(siblingDataChildWidth);
						
						$parent.width(parentWidth);
						$parentDataChildren.width(parentDataChildrenWidth);
					}
					$(document).on("mousemove", resize);

					var stopResize = function() {
						$(document).off("mousemove", resize);
						$(document).off("mouseup", stopResize);
					}
					$(document).on("mouseup", stopResize);
				}

				// Used for sorting
				$scope.predicate = "type";

				// Add data to the scope
				var dataAdded = 0;
				var addData = function (data) {
					for (i in data) {
						$scope.data.push(data[i]);
					}

					if (++dataAdded == 4) {
						stopWaiting();
					}
				}

				
				showWaiting($("#manage-data-sources-data"));
				setTimeout(function() {
					
					// Service Calls
					service.getJDBCDataSources(addData);
					service.getDSWDataSources(addData);
					service.getAnalysisDataSources(addData);
					service.getMetadataDataSources(addData);	
				}, 500);
				
			}]);

		$controllerProvider("NewDataSourceController", ["$scope", 
			function($scope) {
				$scope.title="Page1";
			}]);
	}

	return controllers;
})