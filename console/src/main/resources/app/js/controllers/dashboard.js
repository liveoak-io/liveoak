loMod.controller('DashboardCtrl', function($scope, $routeParams, Current, breadcrumbs) {

    $scope.breadcrumbs = breadcrumbs;
    $scope.dataPeriods = [ "Hour", "Day", "Week", "Month" ];
    $scope.range = "Week";

    // FIXME: Fetch from rest. Mock data.
    $scope.data = {
        "Hour": {requests: 100, users: 7, notifications: 33, storage: 0.6},
        "Day": {requests: 150, users: 15, notifications: 44, storage: 1.5},
        "Week": {requests: 127, users: 13, notifications: 39, storage: 1.2},
        "Month": {requests: 172, users: 18, notifications: 51, storage: 1.8}
    };
});