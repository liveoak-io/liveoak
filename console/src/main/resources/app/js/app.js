'use strict';


// Declare app level module which depends on filters, and services
var loMod = angular.module('loApp', [
  'ngRoute',
  'loApp.filters',
  'loApp.services',
  'loApp.directives',
  'loApp.controllers',
  'services.breadcrumbs'
]);

loMod.config(['$routeProvider', function($routeProvider) {
  $routeProvider
      .when('/', {
          templateUrl : 'partials/dashboard.html',
          controller : 'DashboardCtrl'
      })
      .when('/applications/:appId', {
          redirectTo: '/applications/:appId/dashboard'
      })
      .when('/applications/:appId/dashboard', {
          templateUrl : 'partials/dashboard.html',
          controller : 'DashboardCtrl'
      })
      .otherwise({
          templateUrl : 'partials/notfound.html'
      });
}]);

angular.element(document).ready(function () {
    var liveOak = LiveOak({
        auth: {
            realm: 'LiveOak',
            clientId: 'console',
            clientSecret: 'password',
            onload: 'login-required',
            success: function () {
                loMod.factory('LiveOak', function () {
                    return liveOak;
                });
                angular.bootstrap(document, ["loApp"]);
            },
            error: function () {
                alert('authentication failed');
            }
        }
    });

    liveOak.auth.init();
});

// -- Loading Interceptor ----------------------------

var resourceRequests = 0;
var loadingTimer = -1;

loMod.factory('spinnerInterceptor', function($q, $window, $rootScope, $location) {
    return function(promise) {
        return promise.then(function(response) {
            resourceRequests--;
            if (resourceRequests == 0) {
                if(loadingTimer != -1) {
                    window.clearTimeout(loadingTimer);
                    loadingTimer = -1;
                }
                $('#loading').hide();
            }
            return response;
        }, function(response) {
            resourceRequests--;
            if (resourceRequests == 0) {
                if(loadingTimer != -1) {
                    window.clearTimeout(loadingTimer);
                    loadingTimer = -1;
                }
                $('#loading').hide();
            }

            return $q.reject(response);
        });
    };
});

loMod.config(function($httpProvider) {
    var spinnerFunction = function(data, headersGetter) {
        if (resourceRequests == 0) {
            loadingTimer = window.setTimeout(function() {
                $('#loading').show();
                loadingTimer = -1;
            }, 500);
        }
        resourceRequests++;
        return data;
    };
    $httpProvider.defaults.transformRequest.push(spinnerFunction);

    $httpProvider.responseInterceptors.push('spinnerInterceptor');

});
