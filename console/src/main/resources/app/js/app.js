'use strict';

// Declare app level module which depends on filters, and services
var loMod = angular.module('loApp', [
  'ngRoute',
  'loApp.filters',
  'loApp.services',
  'loApp.directives',
  'loApp.controllers',
  'ngResource',
  'ui.bootstrap'
]);

loMod.config(function($compileProvider){
  $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|blob):/);
});

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
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmApp: function(LoRealmApp, $route) {
          return LoRealmApp.get({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              // Lazily create the application if not present
              return new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).
                $create({realmId: 'liveoak-apps'}).then(function(data){ return data;});
            }
          );
        },
        loRealmAppClient: function(LoRealmApp, $route) {
          return LoRealmApp.get({appId: $route.current.params.clientId}).$promise.then(function (data) {
            return data;
          });
        },
        loRealmAppRoles: function(LoRealmAppRoles, $route) {
          // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
          return LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              return [];
            }
          );
        },
        loRealmRoles: function(LoRealmRolesLoader){
          return new LoRealmRolesLoader();
        },
        scopeMappings: function(LoRealmAppClientScopeMapping, $route){
          // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
          return LoRealmAppClientScopeMapping.query({appId: $route.current.params.appId, clientId: $route.current.params.clientId}).$promise.then(function(data) {
              return data;
            },
            function() {
              return [];
            }
          );
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
        loRealmAppRoles: function(LoRealmApp, LoRealmAppRolesLoader, $route) {
          // FIXME: LIVEOAK-339 & 373 - Remove this once it's done properly on server-side
          return LoRealmApp.get({appId: $route.current.params.appId}).$promise.then(function() {
              return new LoRealmAppRolesLoader();
            },
            function() {
              // Lazily create the application if not present
              return new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).
                $create({realmId: 'liveoak-apps'}).then(function(){ return new LoRealmAppRolesLoader();});
            }
          );
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
      redirectTo: '/applications/:appId/security/policies'
    })
    .when('/applications/:appId/security/policies', {
      templateUrl : '/admin/console/partials/security-list.html',
      controller : 'SecurityListCtrl',
      resolve: {
        currentApp: function(LoAppLoader){
          return new LoAppLoader();
        },
        expApp: function(LoCollectionListLoader) {
          return new LoCollectionListLoader();
        },
        expAppResources : function(LoStorageListLoader) {
          return new LoStorageListLoader();
        }
      }
    })
    .when('/applications/:appId/security/roles', {
      templateUrl : '/admin/console/partials/security-roles.html',
      controller : 'SecurityRolesCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmApp : function(LoRealmApp, $route) {
          return LoRealmApp.get({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              // Lazily create the application if not present
              return new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'});
            }
          );
        }/*,
         loRealmAppRoles : function(LoRealmAppRolesLoader) {
         return new LoRealmAppRolesLoader();
         }*/
      }
    })
    // .when('/applications/:appId/create-security', {
    .when('/applications/:appId/security/policies/:storageId/:collectionId', {
      templateUrl : '/admin/console/partials/security-create.html',
      controller : 'SecurityCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loStorageList : function(LoSecurityCollectionsLoader) {
          return new LoSecurityCollectionsLoader();
        },
        loRealmAppRoles: function(LoRealmAppRolesLoader) {
          return new LoRealmAppRolesLoader();
        },
        uriPolicies: function(LoSecurity, $route) {
          return LoSecurity.get({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              return undefined;
            }
          );
        },
//        uriPolicies: function(LoSecurityLoader) {
//          return new LoSecurityLoader();
//        },
        aclPolicies: function(LoACL, $route) {
          return LoACL.get({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              return undefined;
            }
          );
        }
//        aclPolicies: function(LoACLLoader) {
//          return new LoACLLoader();
//        }
      }
    })
    .when('/applications/:appId/security/users', {
      templateUrl : '/admin/console/partials/security-users.html',
      controller : 'SecurityUsersCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        realmUsers: function(LoRealmUsers) {
          return LoRealmUsers.query();
        }
      }
    })
    .when('/applications/:appId/security/users/:userId', {
      templateUrl : '/admin/console/partials/security-user.html',
      controller : 'SecurityUsersAddCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        userProfile: function(LoRealmUserLoader) {
          return new LoRealmUserLoader();
        },
        userRoles: function(LoRealmUsers, $route) {
          return LoRealmUsers.getRoles({appId: $route.current.params.appId, userId: $route.current.params.userId}).$promise.then(function(data) {
              console.log(data);
              return data;
            },
            function() {
              return [];
            }
          );
        },
        appRoles: function(LoRealmAppRolesLoader) {
          return new LoRealmAppRolesLoader();
        }
      }
    })
    .when('/applications/:appId/security/add-user', {
      templateUrl : '/admin/console/partials/security-user.html',
      controller : 'SecurityUsersAddCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        userProfile: function(LoRealmUsers) {
          return new LoRealmUsers({enabled: true});
        },
        userRoles: function() {
          return [];
        },
        appRoles: function(LoRealmAppRolesLoader) {
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
    .when('/applications/:appId/storage/:storageId/browse/:collectionId', {
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

loMod.config(['$logProvider', function($logProvider) {
  $logProvider.debugEnabled(false);
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

  liveOak.auth.init({ onLoad: 'login-required' }).success(function () {
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
          window.location.reload();
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
