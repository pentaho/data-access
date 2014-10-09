
define(function() {
	var directives = function($directiveProvider) {
		$directiveProvider("datasourceSettings", function() {
			return {
				restrict: 'AE',
				scope: true,
				templateUrl : "content/data-access-v2/resources/web/partials/manage-data-source-settings.html",
				link : function($scope) {
					if (!$scope.text) {
						$scope.text = {};
					}

					$scope.text.deleteErrorDialog = {};
					$scope.text.deleteErrorDialog.message = "Oops. Please select a data source you want to delete.";
					$scope.text.deleteErrorDialog.messageTitle = "Select a Data Source";

					$scope.toggleDialog = function() {
						var $menu = $("#manage-data-source-settings .menu");
						$menu.toggle();

						if ($menu.is(":visible")) {

							// Provide break to bind click event on document
							setTimeout(function() {
								$(document).one("click", function() {
									$menu.hide();
								});
							}, 1);
						}
					}

					$scope.edit = function() {
						if (true) {
							
						}

						$scope.goto('/data-access/edit-data-source');
					}

					$scope.delete = function() {

					}
				}
			}
		});

		$directiveProvider("messageDialog", function() {
			return {
				restrict: "AE",
				scope: {
					id: "@dialogId",
					messageTitle: "@",
					message: "@",
					showOk: "@",
					showCancel: "@",
					onCancelFunc: "&onCancel",
					onOkFunc: "&onOk"
				},
				templateUrl : "content/data-access-v2/resources/web/partials/message-dialog.html",
				link : function($scope, $element) {

					$scope.text = {};
					$scope.text.okButton = "OK";
					$scope.text.cancelButton = "Cancel";

					$scope.show = function() {
						$element.show();
					}

					$scope.onOk = function() {
						if ($scope.onOkFunc) {
							$scope.onOkFunc();
						}

						$("#"+$scope.id).hide();
					}

					$scope.onCancel = function() {
						if ($scope.onCancelFunc) {
							$scope.onCancelFunc();
						}

						$("#"+$scope.id).hide();
					}

				}
			}
		});

		$directiveProvider("draggable", ["$timeout", function($timeout) {
			return {
				restrict: "A",
				scope : {
					dragId: "@"
				},
				link: function($scope, $element) {
					$timeout(function() { // Force to be executed after DOM is processed
						var $dragEle = $scope.dragId ? $("#" + $scope.dragId) : $element;

						// Add drag behavior
						$dragEle.on("mousedown", function($event) {
							var $ele = $element;
							
							var startX = parseInt($ele.css("margin-left"));
							var startY = parseInt($ele.css("margin-top"));
							var startMouseX = $event.clientX;
							var startMouseY = $event.clientY;

							var move = function($event) {
								var diffX = ($event.clientX - startMouseX);
								var diffY = ($event.clientY - startMouseY);
								var left = startX + diffX;
								var top = startY + diffY;

								$ele.css("margin-left", left);
								$ele.css("margin-top", top);
							}
							$(document).on("mousemove", move);

							var stopMove = function() {
								$(document).off("mousemove", move);
								$(document).off("mouseup", stopMove);
							}
							$(document).on("mouseup", stopMove);
						});
					});
				}
			}
		}])
	}

	return directives;
})