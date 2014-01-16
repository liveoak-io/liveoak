angular.module('loApp.controllers').controller('GlobalCtrl', function($scope, LiveOak) {
    $scope.auth = LiveOak.auth;
    $scope.username = LiveOak.auth.username;
    $scope.isAdmin = LiveOak.auth.hasResourceRole("admin");
    $scope.authenticated = LiveOak.auth.authenticated;

    $scope.userLabel = function() {
        var role = "";
        if ($scope.auth.hasResourceRole("admin")) {
            role = "admin";
        } else if ($scope.auth.hasResourceRole("user")) {
            role = "user";
        }
        return $scope.auth.username + " (" + role + ")";
    }

});

angular.module('loApp.controllers').controller('AppDropdownCtrl', function($scope, LiveOak) {

    // FIXME: Get with REST
    $scope.applications = ["Hardcoded One", "Hardcoded Two"];
    $scope.showNav = function() {
        var show = $scope.applications.length > 0;
        return LiveOak.auth.authenticated && show;
    }

});