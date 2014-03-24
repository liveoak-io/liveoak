'use strict';

// Declare app level module which depends on filters, and services
var loMod = angular.module('loApp', [
  'ngRoute',
  'loApp.filters',
  'loApp.services',
  'loApp.directives',
  'loApp.controllers',
  'services.breadcrumbs',
  'ngResource',
  'ngAnimate'
]);

loMod.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/', {
      redirectTo: '/applications/admin/dashboard' // FIXME hardcoded
    })
    .when('/applications', {
      templateUrl : '/admin/console/partials/applications.html',
      controller : 'AppListCtrl',
      resolve: {
        loAppList : function(LoAppListLoader) {
          return new LoAppListLoader();
        }
      }
    })
    .when('/applications/:appId', {
      redirectTo: '/applications/:appId/dashboard'
    })
    .when('/applications/dashboard', {
      redirectTo: '#/applications/admin/dashboard' // FIXME hardcoded
    })
    .when('/applications/:appId/dashboard', {
      templateUrl : '/admin/console/partials/dashboard.html',
      controller : 'DashboardCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        }
      }
    })
    .when('/applications/:appId/storage', {
      controller: 'StorageListCtrl',
      resolve: {
        loStorageList : function(LoStorageListLoader) {
          return new LoStorageListLoader();
        },
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        }
      },
      templateUrl: '/admin/console/partials/storage-list.html'
    })
    .when('/applications/:appId/create-storage', {
      controller: 'StorageCtrl',
      resolve: {
        loStorage : function() {
          return {'type':'mongo','servers':[{}],'credentials':[{'mechanism':'MONGODB-CR'}]};
        },
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        }
      },
      templateUrl: '/admin/console/partials/storage-create.html'
    })
    .when('/applications/:appId/storage/:storageId', {
      controller: 'StorageCtrl',
      resolve : {
        loStorage: function(LoStorageLoader) {
          return new LoStorageLoader();
        },
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        }
      },
      templateUrl: '/admin/console/partials/storage-create.html'
    })
    .when('/applications/:appId/push', {
      controller: 'PushCtrl',
      resolve : {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        loPush: function(LoPush, $route) {
          return LoPush.get({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              return {};
            }
          );
        }
      },
      templateUrl: '/admin/console/partials/push.html'
    })
    .when('/applications/:appId/storage/:storageId/list', {
      controller: 'StorageCollectionCtrl',
      resolve : {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        currentCollectionList: function(LoCollectionListLoader){
          return new LoCollectionListLoader();
        }
      },
      templateUrl: '/admin/console/partials/storage-collection.html'
    })
    .otherwise({
      templateUrl : '/admin/console/partials/notfound.html'
    });
}]);

angular.element(document).ready(function () {
  /* jshint ignore:start */
  var liveOak = new LiveOak({
    auth: {
      realm: 'liveoak-admin',
      clientId: 'console',
      onload: 'login-required'
    }
  });

  liveOak.auth.init('login-required').success(function () {
      loMod.factory('LiveOak', function () {
          return liveOak;
      });
      angular.bootstrap(document, ['loApp']);
  }).error(function() {
      window.location.reload();
  });
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
