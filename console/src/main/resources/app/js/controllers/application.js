'use strict';

var loMod = angular.module('loApp.controllers.application', []);

loMod.controller('AppListCtrl', function($scope, $routeParams, $location, $modal, $filter, Notifications, loAppList, LoApp, LoStorage, LoPush, LoRealmApp) {

  $scope.applications = [];

  $scope.createdId = $routeParams.created;

  var increaseStorages = function(app) {
    return function (resources) {
      if (resources._members) {
        for (var j = 0; j < resources._members.length; j++) {
          if (resources._members[j].hasOwnProperty('MongoClientOptions')) {
            app.mongoStorages++;
          }
        }
      }
    };
  };

  var filtered = $filter('filter')(loAppList._members, {'visible': true});
  for (var i = 0; i < filtered.length; i++) {
    var app = {
      id: filtered[i].id,
      name: filtered[i].name,
      visible: filtered[i].visible
    };
    app.storage = LoStorage.getList({appId: app.id});

    app.mongoStorages = 0;
    app.storage.$promise.then(increaseStorages(app));
    app.push = LoPush.get({appId: app.id});

    $scope.applications.push(app);
  }

  // Delete Application
  $scope.modalApplicationDelete = function(appId) {
    $scope.deleteAppId = appId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/application/application-delete.html',
      controller: DeleteApplicationModalCtrl,
      scope: $scope
    });
  };

  var DeleteApplicationModalCtrl = function ($scope, $modalInstance, $log, LoApp) {

    $scope.applicationDelete = function (appId) {
      $log.debug('Deleting application: ' + appId);
      LoApp.delete({appId: appId},
        // success
        function(/*value, responseHeaders*/) {
          Notifications.success('Application "' + appId + '" deleted successfully.');
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete application "' + appId + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

  // New Application Wizard

  $scope.setupStep = 1; // current step
  $scope.setupSteps = 3; // total steps
  $scope.setupType = 'basic'; // setup type: basic or diy

  $scope.storagePath = '';

  $scope.nextStep = function() {
    $scope.setupStep++;
  };

  $scope.prevStep = function() {
    $scope.setupStep--;
  };

  $scope.stepValid = function() {
    // Step 1: Require name
    switch($scope.setupStep) {
      case 1:
        return $scope.appModel.id;
      case 2:
        return $scope.storagePath;
      case 3:
        return !$scope.pushModel ||
          ($scope.pushModel &&
            ($scope.pushModel.upsURL && $scope.pushModel.applicationId && $scope.pushModel.masterSecret) ||
            (!$scope.pushModel.upsURL && !$scope.pushModel.applicationId && !$scope.pushModel.masterSecret));
      default:
        return false;
    }
  };

  $scope.appModel = {
  };

  var redirectOnNewAppSuccess = function() {
    //$location.search('created', $scope.appModel.id).path('applications');
    $location.path('applications/' + $scope.appModel.id + '/next-steps');
  };

  $scope.create = function() {
    var data = {
      id: $scope.appModel.id,
      name: $scope.appModel.id,
      type: 'application',
      config: {
      }
    };

    new LoRealmApp({name: $scope.appModel.id}).$create({realmId: 'liveoak-apps'},
      function(/*realmApp*/) {
        LoApp.create(data,
          // success
          function(/*value, responseHeaders*/) {
            new LoApp({type:'aggregating-filesystem',config:{directory:'${io.liveoak.js.dir}'}}).$addResource({appId: $scope.appModel.id, resourceId: 'client'});
            if($scope.setupType === 'basic') {
              var storageData = {
                id: $scope.storagePath,
                type: 'mongo',
                config: {
                  db: $scope.appModel.id,
                  servers: [{host: 'localhost', port: 27017}],
                  credentials: []
                }
              };

              LoStorage.create({appId: $scope.appModel.id}, storageData,
                // success
                function(/*value, responseHeaders*/) {
                  if($scope.pushModel && $scope.pushModel.upsURL) {
                    var pushData = {
                      type: 'ups',
                      config: {
                        upsURL: $scope.pushModel.upsURL,
                        applicationId: $scope.pushModel.applicationId,
                        masterSecret: $scope.pushModel.masterSecret
                      }
                    };

                    LoPush.update({appId: $scope.appModel.id}, pushData,
                      // success
                      function (/*value, responseHeaders*/) {
                        Notifications.success('The application ' + data.name + ' has been created with storage and push configured.');
                        redirectOnNewAppSuccess();
                      },
                      // error
                      function (httpResponse) {
                        Notifications.httpError('The application ' + data.name + ' has been created with storage but failed to configure push.', httpResponse);
                      }
                    );
                  }
                  else {
                    Notifications.success('The application ' + data.name + ' has been created with storage configured.');
                    redirectOnNewAppSuccess();
                  }
                },
                // error
                function(httpResponse) {
                  Notifications.httpError('The application ' + data.name + ' has been created but failed to configure storage.', httpResponse);
                  // TODO: Rollback ?
                });
            }
            else {
              Notifications.success('The application ' + data.name + ' has been created.');
              redirectOnNewAppSuccess();
            }
          },
          // error
          function(httpResponse) {
            Notifications.httpError('The application ' + data.name + ' could not be created.', httpResponse);
          });
      },
      function(httpResponse) {
        Notifications.httpError('The application ' + data.name + ' could not be created.', httpResponse);
      }
    );

  };

});

loMod.controller('AppSettingsCtrl', function($scope, $rootScope, $log, $route, $modal, currentApp, LoRealmApp, LoRealmAppRoles, loRealmApp, loRealmAppRoles, Notifications) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Settings',      'href':'#/applications/' + currentApp.id + '/application-settings'}
  ];

  var settingsBackup = {};
  var resetEnv = function() {
    $scope.settings = {
      name: currentApp.name,
      roles: loRealmAppRoles,
      defaultRoles: angular.copy(loRealmApp.defaultRoles) || [],
      deletedRoles: [],
      newRoles: []
    };
    settingsBackup = angular.copy($scope.settings);
  };

  resetEnv();

  $scope.$watch('settings', function() {
    $scope.changed = !angular.equals($scope.settings, settingsBackup);
  }, true);

  $scope.clear = function() {
    $scope.settings = angular.copy(settingsBackup);
    $scope.changed = false;
  };

  $scope.toggleDefaultRole = function(roleName) {
    var idx = $scope.settings.defaultRoles.indexOf(roleName);
    if (idx > -1) {
      $scope.settings.defaultRoles.splice(idx, 1);
    }
    else {
      $scope.settings.defaultRoles.push(roleName);
    }
  };

  $scope.toggleDeletedRole = function(roleName) {
    var idx = $scope.settings.deletedRoles.indexOf(roleName);
    if (idx > -1) {
      $scope.settings.deletedRoles.splice(idx, 1);
    }
    else {
      $scope.settings.deletedRoles.push(roleName);
    }
  };

  var AddRoleModalCtrl = function ($scope, $modalInstance, Notifications, LoRealmAppRoles, roles) {

    $scope.newRole = new LoRealmAppRoles();

    $scope.addRole = function () {
      for(var i = 0; i < roles.length; i++) {
        if(roles[i].name === $scope.newRole.name) {
          Notifications.error('The role with name "' + $scope.newRole.name + '" already exists.');
          return;
        }
      }

      $modalInstance.close($scope.newRole);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

  $scope.modalAddRole = function() {
    var modalAddRole = $modal.open({
      templateUrl: '/admin/console/templates/modal/application/role-add.html',
      controller: AddRoleModalCtrl,
      scope: $scope,
      resolve: {
        roles: function () {
          return $scope.settings.roles.concat($scope.settings.newRoles);
        }
      }
    });

    modalAddRole.result.then(
      function(newRole) { // modal completion
        $scope.settings.newRoles.push(newRole);
      }
    );
  };

  $scope.pendingTasks = -1;

  $scope.$watch('pendingTasks', function (newVal, oldVal/*, scope*/) {
    if(oldVal === 1 && newVal === 0) {
      $route.reload();
    }
  });

  $scope.save = function() {
    // check newly added and simultaneously deleted roles..
    // we are going backwards so splice won't affect the index we are working at
    var idx = $scope.settings.newRoles.length;
    while (idx--) {
      var deletedIdx = $scope.settings.deletedRoles.indexOf($scope.settings.newRoles[idx]);
      if (deletedIdx !== -1) {
        $scope.settings.deletedRoles.splice(deletedIdx, 1);
        $scope.settings.newRoles.splice(idx, 1);
      }
    }

    var defaultRolesChanged = !angular.equals(loRealmApp.defaultRoles, $scope.settings.defaultRoles);
    $scope.pendingTasks = $scope.settings.deletedRoles.length + $scope.settings.newRoles.length + defaultRolesChanged;

    var deleteRoleSuccessCallback = function(value) {
      $scope.pendingTasks--;
      Notifications.success('The application role "' +  value.name + '" was deleted.');
    };

    var deleteRoleFailureCallback = function(httpResponse) {
      $scope.pendingTasks--;
      Notifications.error('Unable to delete the application role "' +  httpResponse.config.data.name + '".', httpResponse);
    };

    for (var drIdx = 0; drIdx < $scope.settings.deletedRoles.length; drIdx++) {
      $scope.settings.deletedRoles[drIdx].$delete({realmId: 'liveoak-apps', appId: currentApp.id, roleName: $scope.settings.deletedRoles[drIdx].name},
        deleteRoleSuccessCallback, deleteRoleFailureCallback
      );
    }

    var addRoleSuccessCallback = function(value) {
      $scope.pendingTasks--;
      Notifications.success('The application role "' + value.name + '" has been created.');
    };

    var addRoleFailureCallback = function(httpResponse) {
      $scope.pendingTasks--;
      Notifications.error('Unable to create the application role "' + httpResponse.config.data.name + '".', httpResponse);
    };

    for (var nrIdx = 0; nrIdx < $scope.settings.newRoles.length; nrIdx++) {
      $scope.settings.newRoles[nrIdx].$save({realmId: 'liveoak-apps', appId: currentApp.id},
        addRoleSuccessCallback, addRoleFailureCallback
      );
    }

    if (!angular.equals(loRealmApp.defaultRoles, $scope.settings.defaultRoles)) {
      loRealmApp.defaultRoles = $scope.settings.defaultRoles;
      loRealmApp.$save({realmId: 'liveoak-apps', appId: currentApp.id},
        function(value/*, responseHeaders*/) {
          $scope.pendingTasks--;
          Notifications.success('The application default roles have been set to: "' + value.defaultRoles + '".');
        },
        function (httpResponse) {
          $scope.pendingTasks--;
          Notifications.error('Unable to configure the application default roles.', httpResponse);
        }
      );
    }

    // FIXME: Check this once REST works for renaming
    if(currentApp.name !== $scope.settings.name) {
      currentApp.name = $scope.settings.name;
      currentApp.$save();
    }
  };
});

loMod.controller('AppClientsCtrl', function($scope, $rootScope, $filter, LoRealmAppClientScopeMapping, currentApp, loRealmAppClients) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Clients', 'href': '#/applications/' + currentApp.id + '/application-clients'}
  ];

  $scope.appClients = $filter('filter')(loRealmAppClients, {'publicClient': true});

  for (var i = 0; i < $scope.appClients.length; i++) {
    //$scope.appClients[i].realmRoles = [];
    $scope.appClients[i].realmRoles = LoRealmAppClientScopeMapping.query({appId: currentApp.name, clientId: $scope.appClients[i].name});
  }

});

loMod.controller('AppClientCtrl', function($scope, $rootScope, $filter, $route, $http, LoRealmApp, LoRealmAppRoles, LoRealmAppClientScopeMapping, currentApp, loRealmAppClient, loRealmRoles, loClientRoles, scopeMappings) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Clients',      'href':'#/applications/' + currentApp.id + '/application-clients'}
  ];

  if (loRealmAppClient && loRealmAppClient.id) {
    $scope.create = false;
    $scope.appClient = loRealmAppClient;
    $scope.breadcrumbs.push({'label': loRealmAppClient.name, 'href':'#/applications/' + currentApp.id + '/application-clients/' + loRealmAppClient.name});
  }
  else {
    $scope.create = true;
    $scope.appClient = new LoRealmApp();
    $scope.appClient.bearerOnly = false;
    $scope.appClient.publicClient = true;
    $scope.breadcrumbs.push({'label': 'New Client', 'href':'#/applications/' + currentApp.id + '/application-clients/create-client'});
  }

  $scope.changed = false;

  $scope.addRedirectUri = function() {
    $scope.settings.redirectUris.push($scope.newRedirectUri);
    $scope.newRedirectUri = '';
  };

  $scope.deleteRedirectUri = function(index) {
    $scope.settings.redirectUris.splice(index, 1);
  };

  $scope.availableRoles = loClientRoles;//loRealmRoles.concat(loClientRoles);

  $scope.settings = {
    name: $scope.appClient.name,
    scopeMappings: [],
    redirectUris: angular.copy($scope.appClient.redirectUris) || []
  };
  angular.forEach(scopeMappings, function(role) {$scope.settings.scopeMappings.push(role.id);});

  var settingsBackup = angular.copy($scope.settings);

  $scope.$watch('settings', function() {
    $scope.changed = !angular.equals($scope.settings, settingsBackup);
  }, true);

  $scope.clear = function() {
    $scope.settings = angular.copy(settingsBackup);
  };

  var arrayObjectIndexOf = function(array, object) {
    for (var i = 0; i < array.length; i++){
      if (angular.equals(array[i], object)) {
        return i;
      }
    }
    return -1;
  };

  $scope.save = function() {

    if($scope.appClient.name !== $scope.settings.name || !angular.equals($scope.appClient.redirectUris, $scope.settings.redirectUris)) {
      $scope.appClient.name = $scope.settings.name;
      $scope.appClient.redirectUris = $scope.settings.redirectUris;
      if($scope.create) {
        $scope.appClient.$create();
      }
      else {
        $scope.appClient.$save();
      }
    }
    if(!angular.equals($scope.settings.scopeMappings, settingsBackup.scopeMappings)) {
      var smData = [];
      for(var i = 0; i < $scope.availableRoles.length; i++) {
        if($scope.settings.scopeMappings.indexOf($scope.availableRoles[i].id) > -1) {
          smData.push($scope.availableRoles[i]);
          console.log('Adding ' + $scope.availableRoles[i].name);
        }
      }

      // Find which to delete, if any
      var smDelete = [];
      for(var j = 0; j < scopeMappings.length; j++) {
        if(arrayObjectIndexOf(smData, scopeMappings[j]) === -1) {
          smDelete.push(scopeMappings[j]);
        }
      }

      // FIXME: It seems DELETE cannot have payload in ng-resource...
      // var smRes = new LoRealmAppClientScopeMapping(smDelete);
      // smRes.$delete({appId: $route.current.params.appId, clientId: $route.current.params.clientId});
      if(smDelete.length > 0) {
        $http.delete('/auth/rest/admin/realms/liveoak-apps/applications/' + $route.current.params.clientId +  '/scope-mappings/applications/' + $route.current.params.appId,
          {data: smDelete, headers : {'content-type' : 'application/json'}});
      }

      // FIXME: For some reason, using this is causing the [..] to be passed as JSON Object {..}
      // var smRes = new LoRealmAppClientScopeMapping(smData);
      // smRes.$save({appId: $route.current.params.appId, clientId: $route.current.params.clientId});
      if(smData.length > 0) {
        $http.post('/auth/rest/admin/realms/liveoak-apps/applications/' + $route.current.params.clientId +  '/scope-mappings/applications/' + $route.current.params.appId,
          smData);
      }
    }
  };

});

loMod.controller('NextStepsCtrl', function($scope, $rootScope, $routeParams, currentApp, loStorageList, loPush) {

  $rootScope.curApp = currentApp;

  $scope.storageList = [];

  /* jshint unused: false */
  angular.forEach(loStorageList._members, function (value, key) {
    if (value.hasOwnProperty('db')) {
      this.push({id: value.id, provider: value.hasOwnProperty('MongoClientOptions') ? 'mongoDB' : 'unknown'});
    }
  }, $scope.storageList);
  /* jshint unused: true */

  $scope.pushConfig = loPush;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Next Steps', 'href': '#/applications/' + currentApp.id + '/next-steps'}
  ];

});
