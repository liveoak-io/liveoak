'use strict';

/* Directives */
var module = angular.module('loApp.directives', []);

module.directive('loNavbar', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'templates/lo-navbar.html',
    }
});

module.directive('loNavigation', function () {
    return {
        scope: {
            loCurrent: '@',
            loApps: '=',
            loApp: '='
        },
        restrict: 'E',
        replace: true,
        templateUrl: 'templates/lo-navigation.html',
    }
});

module.directive('appVersion', ['version', function(version) {
    return function(scope, elm, attrs) {
        elm.text(version);
    };
}]);
