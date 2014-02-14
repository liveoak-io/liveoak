loMod.controller('GlobalCtrl', function($scope, LiveOak, Current) {
    $scope.auth = LiveOak.auth;
    $scope.username = LiveOak.auth.username;
    $scope.isAdmin = LiveOak.auth.hasResourceRole("admin");
    $scope.authenticated = LiveOak.auth.authenticated;

    $scope.curApp = Current;

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

angular.module('loApp.controllers').controller('AppDropdownCtrl', function($scope, LiveOak, Current) {

    // FIXME: Get with REST
    $scope.applications = ["My App", "Other App"];
    $scope.curApp = Current;
    $scope.showNav = function() {
        var show = $scope.applications.length > 0;
        return LiveOak.auth.authenticated && show;
    }

});