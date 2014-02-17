'use strict';

/* Directives */
var loDirectives = angular.module('loApp.directives', []);

loDirectives.directive('loNavbar', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'templates/lo-navbar.html',
    }
});

loDirectives.directive('loNavigation', function () {
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

loDirectives.directive('appVersion', ['version', function(version) {
    return function(scope, elm, attrs) {
        elm.text(version);
    };
}]);
