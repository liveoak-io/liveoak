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
      // templateUrl : '/admin/console/partials/dashboard.html',
      // controller : 'DashboardCtrl'
      redirectTo: '/applications/My App/dashboard' // FIXME hardcoded
    })
    .when('/applications', {
      templateUrl : '/admin/console/partials/applications.html',
      controller : 'AppListCtrl'
    })
    .when('/applications/:appId', {
      redirectTo: '/applications/:appId/dashboard'
    })
    .when('/applications/dashboard', {
      redirectTo: '#/applications/My App/dashboard' // FIXME hardcoded
    })
    .when('/applications/:appId/dashboard', {
      templateUrl : '/admin/console/partials/dashboard.html',
      controller : 'DashboardCtrl'
    })
    .when('/applications/:appId/storage', {
      controller: 'StorageListCtrl',
      templateUrl: '/admin/console/partials/storage-list.html'
    })
    .when('/applications/:appId/storage/create', {
      controller: 'StorageCtrl',
      resolve: {
        'loStorage' : function(LoStorageLoader) {
          return new LoStorageLoader();
        }
      },
      templateUrl: '/admin/console/partials/storage-create.html'
    })
    .otherwise({
      templateUrl : '/admin/console/partials/notfound.html'
    });
}]);

angular.element(document).ready(function () {
  /* jshint ignore:start */
  var liveOak = new LiveOak({
    auth: {
      realm: 'keycloak-admin',
      clientId: 'console',
      clientSecret: 'password',
      onload: 'login-required',
      success: function () {
        loMod.factory('LiveOak', function () {
          return liveOak;
        });
        angular.bootstrap(document, ['loApp']);
      },
      error: function () {
        alert('authentication failed');
      }
    }
  });

  liveOak.auth.init();
  /* jshint ignore:end */
});

// -- Loading Interceptor ----------------------------

var resourceRequests = 0;
var loadingTimer = -1;

loMod.factory('spinnerInterceptor', function($q) {
  return function(promise) {
    return promise.then(function(response) {
      resourceRequests--;
      if (resourceRequests === 0) {
        if(loadingTimer !== -1) {
          window.clearTimeout(loadingTimer);
          loadingTimer = -1;
        }
        $('#loading').hide();
      }
      return response;
    }, function(response) {
      resourceRequests--;
      if (resourceRequests === 0) {
        if(loadingTimer !== -1) {
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
  var spinnerFunction = function(data) {
    if (resourceRequests === 0) {
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
