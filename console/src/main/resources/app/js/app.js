'use strict';

// Declare app level module which depends on filters, and services
var loMod = angular.module('loApp', [
  'ngRoute',
  'loApp.filters',
  'loApp.services',
  'loApp.directives',
  'loApp.controllers',
  'ngResource',
  'ngCookies',
  'patternfly.autofocus',
  'patternfly.notification',
  'patternfly.select',
  'patternfly.validation',
  'ui.bootstrap',
  'ui.codemirror'
]);

var liveOak = new LiveOak({
  auth: {
    realm: 'liveoak-admin',
    clientId: 'console',
    onload: 'login-required'
  }
});

loMod.factory('LiveOak', function () {
  return liveOak;
});

liveOak.auth.init({ onLoad: 'login-required' }).success(function () {
  angular.element(document).ready(function() {
    angular.bootstrap(document, ['loApp']);
  });
}).error(function() {
  window.alert('Error: Unable to initialize LiveOak client.');
});

loMod.config(function($compileProvider){
  $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|blob):/);
});

loMod.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/', {
      template: '',
      controller: 'HomeCtrl',
      resolve: {
        loAppList : function(loLiveLoader, LoLiveAppList, $filter, $location, $q) {
          var livePromise = loLiveLoader(LoLiveAppList.getList);
          livePromise.then(function(loAppList){
            var filtered = $filter('filter')(loAppList.members, {'visible': true});
            if (filtered.length === 1) {
              $location.url('/applications/' + filtered[0].id);
            }
            else {
              $location.url('/applications');
            }
          }, function(){
            $location.url('/error');
          });

          // Returning a promise which would never be resolved, so that the page would not render.
          // The page will be redirected before rendering based on the application list loaded above.
          return $q.defer().promise;
        }
      }
    })
    .when('/applications', {
      templateUrl : '/admin/console/partials/applications.html',
      controller : 'AppListCtrl',
      resolve: {
        loAppList : function(loLiveLoader, LoLiveAppList) {
          return loLiveLoader(LoLiveAppList.getList);
        },
        examplesList : function(LoAppExamples) {
          return LoAppExamples.query().$promise;
        }
      }
    })
    .when('/example-applications', {
      templateUrl : '/admin/console/partials/applications-example.html',
      controller : 'ExampleListCtrl',
      resolve: {
        examplesList : function(LoAppExamples) {
          return LoAppExamples.query().$promise;
        },
        loAppList : function(loLiveLoader, LoLiveAppList) {
          return loLiveLoader(LoLiveAppList.getList);
        }
      }
    })
    .when('/applications/:appId/business-logic', {
      templateUrl : '/admin/console/partials/business-logic-list.html',
      controller : 'BusinessLogicListCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        triggeredScripts: function(LoBusinessLogicScripts, $route) {
          return LoBusinessLogicScripts.get({appId: $route.current.params.appId, type:'resource-triggered-scripts'});
        }
      }
    })
    .when('/applications/:appId/create-logic', {
      templateUrl : '/admin/console/partials/business-logic-create.html',
      controller : 'BusinessLogicDetailsCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        currentScript: function(LoBusinessLogicScripts) {
          return new LoBusinessLogicScripts();
        },
        hasResource: function(LoBusinessLogicScripts, $route) {
          return LoBusinessLogicScripts.getResource({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              // Lazily create the resource if not present
              var data = { name: 'scripts', type:'scripts', config: { 'script-directory' : '${application.dir}/scripts' } };
              LoBusinessLogicScripts.createResource({appId: $route.current.params.appId}, data);
              return false;
            }
          );
        }
      }
    })
    .when('/applications/:appId/business-logic/:scriptId', {
      templateUrl : '/admin/console/partials/business-logic-create.html',
      controller : 'BusinessLogicDetailsCtrl',
      resolve: {
        currentApp: function (LoAppLoader) {
          return new LoAppLoader();
        },
        currentScript: function (LoBusinessLogicScripts, $route) {
          return LoBusinessLogicScripts.get({appId: $route.current.params.appId, type: 'resource-triggered-scripts', scriptId: $route.current.params.scriptId}).$promise;
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
        loClients: function(LoClient, $route){
          return LoClient.getList({appId: $route.current.params.appId});
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
        loClient: function(LoClient, LoRealmApp, $route) {
          return LoRealmApp.get({appId: $route.current.params.clientId}).$promise.then(function (data) {
            return LoClient.get({appId: $route.current.params.appId, clientId: data.id}).$promise.then(function(data){
              return data;
            });
          });
        },
        loClients: function(LoClient, $route){
          return LoClient.getList({appId: $route.current.params.appId});
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
        loClient: function(LoClient) {
          return new LoClient();
        },
        loClients: function(LoClient, $route){
          return LoClient.getList({appId: $route.current.params.appId});
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

    .when('/applications/:appId/security/secure-collections', {
      templateUrl : '/admin/console/partials/security-create-collections.html',
      controller : 'NoSecurityCtrl',
      resolve: {
        currentApp: function($route) {
          // FIXME: name may be different from id
          return {id: $route.current.params.appId, name: $route.current.params.appId};
        },
        noCollections: function(LoSecurityCollections, $route, $filter, $location) {
          return new LoSecurityCollections.get({appId: $route.current.params.appId}).$promise.then(function(data) {
            var storageList = $filter('filter')(data.members, {'type': 'database'});
            for(var i = 0; i < storageList.length; i++) {
              if(storageList[i].members && storageList[i].members.length > 0) {
                $location.path('/applications/' + $route.current.params.appId + '/security/policies/storage/' + storageList[i].id  + '/' + storageList[i].members[0].id);
              }
            }
            return true;
          });
        }
      }
    })
    .when('/applications/:appId/security/secure-storage', {
      templateUrl : '/admin/console/partials/security-create-storage.html',
      controller : 'NoSecurityCtrl',
      resolve: {
        currentApp: function($route) {
          // FIXME: name may be different from id
          return {id: $route.current.params.appId, name: $route.current.params.appId};
        },
        noStorage: function(LoSecurityCollections, $route, $filter, $location) {
          return new LoSecurityCollections.get({appId: $route.current.params.appId}).$promise.then(function(data) {
            var storageList = $filter('filter')(data.members, {'type': 'database'});
            if(storageList.length > 0) {
              $location.path('/applications/' + $route.current.params.appId + '/security/policies/storage/' + storageList[0].id);
            }
            return true;
          });
        }
      }
    })
    .when('/applications/:appId/security/policies/storage/:storageId/:collectionId', {
      templateUrl : '/admin/console/partials/security-create-collections.html',
      controller : 'SecurityCollectionsCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loStorageList : function(LoSecurityCollectionsLoader) {
          return new LoSecurityCollectionsLoader();
        },
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmAppRoles: function(LoRealmAppRoles, LoRealmApp, $route) {
          return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
              function() {
                $route.reload();
              });
            }
          );
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
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        /*appRoles: function(LoRealmAppRolesLoader) {
          return new LoRealmAppRolesLoader();
        }*/
        appRoles: function(LoRealmAppRoles, LoRealmApp, $route) {
          return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(
            function(data) {
              return data;
            },
            function() {
              new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
                function() {
                  return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(
                    function(data) {
                      return data;
                    });
                });
            });
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
    .when('/applications/:appId/security/policies/storage/:storageId', {
      templateUrl : '/admin/console/partials/security-create-storage.html',
      controller : 'SecurityStorageCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loStorageList : function(LoSecurityCollectionsLoader) {
          return new LoSecurityCollectionsLoader();
        },
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmAppRoles: function(LoRealmAppRoles, LoRealmApp, $route) {
          return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
                function() {
                  $route.reload();
                });
            }
          );
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
    .when('/applications/:appId/security/secure-push', {
      templateUrl : '/admin/console/partials/security-create-push.html',
      controller : 'SecurityPushCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loAppExpanded : function(LoSecurityCollectionsLoader) {
          return new LoSecurityCollectionsLoader();
        },
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmAppRoles: function(LoRealmAppRoles, LoRealmApp, $route) {
          return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
                function() {
                  return undefined;
                });
            }
          );
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
    .when('/applications/:appId/security/secure-clients', {
      templateUrl : '/admin/console/partials/security-create-clients.html',
      controller : 'SecurityClientsCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loAppExpanded : function(LoSecurityCollectionsLoader) {
          return new LoSecurityCollectionsLoader();
        },
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmAppRoles: function(LoRealmAppRoles, LoRealmApp, $route) {
          return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
                function() {
                  return undefined;
                });
            }
          );
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
    .when('/applications/:appId/security/secure-logic', {
      templateUrl : '/admin/console/partials/security-create-logic.html',
      controller : 'SecurityBusinessLogicCtrl',
      resolve: {
        currentApp: function(LoAppLoader) {
          return new LoAppLoader();
        },
        loAppExpanded : function(LoSecurityCollectionsLoader) {
          return new LoSecurityCollectionsLoader();
        },
        // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
        loRealmAppRoles: function(LoRealmAppRoles, LoRealmApp, $route) {
          return new LoRealmAppRoles.query({appId: $route.current.params.appId}).$promise.then(function(data) {
              return data;
            },
            function() {
              new LoRealmApp({'name': $route.current.params.appId, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
                function() {
                  return undefined;
                });
            }
          );
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
    .when('/error', {
      controller: 'ErrorCtrl',
      template: '<div></div>'
    })
    .otherwise({
      templateUrl : '/admin/console/partials/notfound.html'
    });
}]);

loMod.config(['$logProvider', function($logProvider) {
  $logProvider.debugEnabled(false);
}]);

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
