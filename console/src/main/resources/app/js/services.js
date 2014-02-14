'use strict';

/* Services */
loMod.factory('Current', function() {
    var appName = "My App"

    return {
        setApp: function(app){
            appName = app;
        },
        name: appName
    }
})

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('loApp.services', []).
  value('version', '0.1');

angular.module('services.breadcrumbs', []);
angular.module('services.breadcrumbs').factory('breadcrumbs', ['$rootScope', '$location', function($rootScope, $location){

    var breadcrumbs = [];
    var breadcrumbsService = {};

    //we want to update breadcrumbs only when a route is actually changed
    //as $location.path() will get updated immediately (even if route change fails!)
    $rootScope.$on('$routeChangeSuccess', function(event, current){

        var pathElements = $location.path().split('/'), result = [], i;
        var breadcrumbPath = function (index) {
            return '/' + (pathElements.slice(0, index + 1)).join('/');
        };

        pathElements.shift();
        for (i = 0; i < pathElements.length; i++) {
            result.push({name: pathElements[i], path: breadcrumbPath(i)});
        }

        breadcrumbs = result;
    });

    breadcrumbsService.getAll = function() {
        return breadcrumbs;
    };

    breadcrumbsService.getFirst = function() {
        return breadcrumbs[0] || {};
    };

    return breadcrumbsService;
}]);