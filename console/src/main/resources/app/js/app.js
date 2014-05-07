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
  'ngAnimate',
  'ui.bootstrap'
]);

loMod.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/', {
      template: '',
      controller: 'HomeCtrl',
      resolve: {
        loAppList : function(LoAppListLoader) {
          return new LoAppListLoader();
        }
      }
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
    .when('/applications/:appId/application-clients', {
      templateUrl : '/admin/console/partials/application-clients.html',
      controller : 'AppClientsCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        loRealmAppClients : function(LoRealmAppListLoader) {
          return new LoRealmAppListLoader();
        }
      }
    })
    .when('/applications/:appId/application-clients/:clientId', {
      templateUrl : '/admin/console/partials/application-client.html',
      controller : 'AppClientCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        loRealmAppClient: function(LoRealmApp, $route) {
          return LoRealmApp.get({appId: $route.current.params.clientId}).$promise.then(function (data) {
            return data;
          });
        },
        loClientRoles: function(LoRealmAppRoles, $route) {
          return LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function (data) {
            return data;
          });
        },
        loRealmRoles: function(LoRealmRolesLoader){
          return new LoRealmRolesLoader();
        },
        scopeMappings: function(LoRealmAppClientScopeMappingLoader){
          return new LoRealmAppClientScopeMappingLoader();
        }
      }
    })
    .when('/applications/:appId/create-client', {
      templateUrl : '/admin/console/partials/application-client.html',
      controller : 'AppClientCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        loRealmAppClient: function(LoRealmApp) {
          return new LoRealmApp();
        },
        loClientRoles: function(LoRealmAppRoles) {
          return new LoRealmAppRoles();
        },
        loRealmRoles: function(LoRealmRolesLoader){
          return new LoRealmRolesLoader();
        },
        scopeMappings: function(LoRealmAppClientScopeMapping){
          return new LoRealmAppClientScopeMapping();
        }
      }
    })
    .when('/applications/:appId/security', {
      templateUrl : '/admin/console/partials/security-list.html',
      controller : 'SecurityCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        }
      }
    })
    .when('/applications/:appId/create-security', {
      templateUrl : '/admin/console/partials/security-create.html',
      controller : 'SecurityCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        }
      }
    })
    .when('/applications/:appId/application-settings', {
      templateUrl : '/admin/console/partials/application-settings.html',
      controller : 'AppSettingsCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loRealmApp : function(LoRealmAppLoader) {
          return new LoRealmAppLoader();
        },
        loRealmAppRoles : function(LoRealmAppRolesLoader) {
          return new LoRealmAppRolesLoader();
        }
      }
    })
    .when('/applications/:appId/dashboard', {
      templateUrl : '/admin/console/partials/dashboard.html',
      controller : 'DashboardCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        loStorageList : function(LoStorageListLoader) {
          return new LoStorageListLoader();
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
      }
    })
    .when('/applications/:appId/next-steps', {
      templateUrl : '/admin/console/partials/next-steps.html',
      controller : 'NextStepsCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        loStorageList : function(LoStorageListLoader) {
          return new LoStorageListLoader();
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
    .when('/applications/:appId/storage/:storageId/browse', {
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
    .when('/error', {
      templateUrl : '/admin/console/partials/notfound.html'
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

loMod.factory('authInterceptor', function($q, LiveOak) {
  return {
    request: function (config) {
      var deferred = $q.defer();
      if (LiveOak.auth.token) {
        LiveOak.auth.updateToken(5).success(function() {
          config.headers = config.headers || {};
          config.headers.Authorization = 'Bearer ' + LiveOak.auth.token;

          deferred.resolve(config);
        }).error(function() {
          deferred.reject('Failed to refresh token');
        });
      }
      return deferred.promise;
    }
  };
});

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
  $httpProvider.interceptors.push('authInterceptor');
});
