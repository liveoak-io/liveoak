'use strict';

var loMod = angular.module('loApp.controllers.application', []);

loMod.controller('AppListCtrl', function($scope, $routeParams, $location, $modal, $filter, Notifications, examplesList, loAppList, LoApp, LoStorage, LoPush, LoRealmApp) {

  $scope.applications = [];
  $scope.exampleApplications = [];

  $scope.createdId = $routeParams.created;

  var increaseStorages = function(app) {
    return function (resources) {
      if (resources._members) {
        for (var j = 0; j < resources._members.length; j++) {
          if (resources._members[j].hasOwnProperty('MongoClientOptions')) {
            app.mongoStorages++;
          }
          else if(resources._members[j].hasOwnProperty('upsURL')) {
            app.push = resources._members[j];
          }
        }
      }
    };
  };

  var filtered = $filter('orderBy')($filter('filter')(loAppList._members, {'visible': true}), 'name');
  for (var i = 0; i < filtered.length; i++) {
    var app = {
      id: filtered[i].id,
      name: filtered[i].name,
      visible: filtered[i].visible
    };
    app.storage = LoStorage.getList({appId: app.id});

    app.mongoStorages = 0;
    app.storage.$promise.then(increaseStorages(app));

    if ($filter('filter')(examplesList, {'id': app.id}, true).length > 0) {
      app.example = true;
      $scope.exampleApplications.push(app);
    }
    else {
      $scope.applications.push(app);
    }
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
          Notifications.success('The application "' + appId + '" has been deleted.');
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete the application "' + appId + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

  // Create Application
  $scope.modalApplicationCreate = function() {
    $modal.open({
      templateUrl: '/admin/console/templates/modal/application/application-create.html',
      controller: CreateApplicationModalCtrl,
      scope: $scope
    });
  };

  var CreateApplicationModalCtrl = function ($scope, $modalInstance, $log, LoApp) {

    $scope.setupStep = 1; // current step
    $scope.setupSteps = 3; // total steps

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
          return $scope.$parent.appModel.id;
        case 2:
          return $scope.$parent.storagePath;
        case 3:
          return !$scope.pushModel ||
            ($scope.pushModel &&
              ($scope.pushModel.upsURL && $scope.pushModel.applicationId && $scope.pushModel.masterSecret) ||
              (!$scope.pushModel.upsURL && !$scope.pushModel.applicationId && !$scope.pushModel.masterSecret));
        default:
          return false;
      }
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    $scope.create = function() {
      var data = {
        id: $scope.appModel.id,
        name: $scope.appModel.id,
        type: 'application',
        config: {
        }
      };

      new LoRealmApp({name: $scope.appModel.id, 'bearerOnly': true}).$create({realmId: 'liveoak-apps'},
        function(/*realmApp*/) {
          LoApp.create(data,
            // success
            function(/*value, responseHeaders*/) {
              // Needed resources.. probably move this to server side in the future
              new LoApp({type:'aggregating-filesystem',config:{directory:'${io.liveoak.js.dir}'}}).$addResource({appId: $scope.appModel.id, resourceId: 'client'});
              new LoApp({type:'keycloak'}).$addResource({appId: $scope.appModel.id, resourceId: 'auth'});
              new LoApp({
                type: 'security',
                config:{
                  policies: [
                    {
                      policyName : 'URIPolicy',
                      policyResourceEndpoint: '/' + $scope.appModel.id + '/uri-policy/authzCheck'
                    },
                    {
                      policyName : 'ACLPolicy',
                      policyResourceEndpoint: '/' + $scope.appModel.id + '/acl-policy/authzCheck',
                      includedResourcePrefixes: [ '/' + $scope.appModel.id ]
                    }
                  ]
                }
              }).$addResource({appId: $scope.appModel.id, resourceId: 'authz'});
              new LoApp({
                type: 'uri-policy',
                config: {
                  rules: [
                    {
                      'uriPattern' : '*',
                      'requestTypes' : [ '*' ],
                      'allowedUsers': [ '*' ]
                    }
                  ]
                }
              }).$addResource({appId: $scope.appModel.id, resourceId: 'uri-policy'});
              new LoApp({type: 'acl-policy', config: {autoRules: []}}).$addResource({appId: $scope.appModel.id, resourceId: 'acl-policy'});
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
                          redirectOnNewAppSuccess();
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
                    redirectOnNewAppSuccess();
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

    var redirectOnNewAppSuccess = function() {
      $modalInstance.close();
      //$location.search('created', $scope.appModel.id).path('applications');
      $location.path('applications/' + $scope.appModel.id + '/next-steps');
    };
  };

  // We keep this at parent scope so it doesn't go away with [accidental] modal close
  $scope.setupType = 'basic'; // setup type: basic or diy
  $scope.storagePath = 'storage';
  $scope.appModel = {
  };

});

loMod.controller('AppClientsCtrl', function($scope, $rootScope, $filter, $modal, Notifications, LoRealmApp, LoRealmAppClientScopeMapping, currentApp, loRealmAppClients) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Clients', 'href': '#/applications/' + currentApp.id + '/application-clients'}
  ];

  var appFilter = function(element) {
    return element.publicClient && element.name !== 'security-admin-console';
  };

  $scope.appClients = $filter('filter')(loRealmAppClients, appFilter);

  for (var i = 0; i < $scope.appClients.length; i++) {
    $scope.appClients[i].realmRoles = LoRealmAppClientScopeMapping.query({appId: currentApp.name, clientId: $scope.appClients[i].name});
  }

  // Delete Client
  $scope.modalClientDelete = function(clientId) {
    $scope.deleteClientId = clientId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/application/client-delete.html',
      controller: DeleteClientModalCtrl,
      scope: $scope
    }).result.then(
      function() {
        LoRealmApp.query().$promise.then(function(data) {
          $scope.appClients = $filter('filter')(data, appFilter);
        });
      }
    );
  };

  var DeleteClientModalCtrl = function ($scope, $modalInstance, $log, LoRealmApp) {

    $scope.clientDelete = function (clientId) {
      $log.debug('Deleting client: ' + clientId);
      LoRealmApp.delete({appId: clientId},
        // success
        function(/*value, responseHeaders*/) {
          Notifications.success('The client "' + clientId + '" has been deleted.');
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete the client "' + clientId + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

});

loMod.controller('AppClientCtrl', function($scope, $rootScope, $filter, $route, $location, $http, Notifications, LoRealmApp, LoRealmAppRoles, LoRealmAppClientScopeMapping, currentApp, loRealmAppClient, loRealmRoles, loRealmAppRoles, scopeMappings) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Clients',       'href':'#/applications/' + currentApp.id + '/application-clients'}
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

  $scope.availableRoles = $filter('orderBy')(loRealmAppRoles, 'name');//loRealmRoles.concat(loRealmAppRoles);
  $scope.noRoles = ['The application has no Roles'];

  $scope.settings = {
    name: $scope.appClient.name,
    scopeMappings: [],
    redirectUris: angular.copy($scope.appClient.redirectUris) || [],
    webOrigins: angular.copy($scope.appClient.webOrigins) || []
  };

  angular.forEach(scopeMappings, function(role) {$scope.settings.scopeMappings.push(role.id);});

  var settingsBackup = angular.copy($scope.settings);

  $scope.redirectUris = [];

  angular.forEach($scope.settings.redirectUris, function (uri) {
    $scope.redirectUris.push({'val': uri});
  });

  if ($scope.redirectUris.length === 0) {
    $scope.redirectUris.push({});
  }

  var redirectUrisBackup = angular.copy($scope.redirectUris);

  $scope.webOrigins = [];

  angular.forEach($scope.settings.webOrigins, function (uri) {
    $scope.webOrigins.push({'val': uri});
  });

  if ($scope.webOrigins.length === 0) {
    $scope.webOrigins.push({});
  }

  var webOriginsBackup = angular.copy($scope.webOrigins);

  $scope.checkUrl = function(uri){
    return (/^http[s]?:\/\/[^/]*/).test(uri);
  };

  $scope.changeRedirectUri = function(uri){
    if(!$scope.newWebOrigin && (/^http[s]?:\/\/[^/]*/).test(uri)) {
      var newWebOrigin = uri.match(/^http[s]?:\/\/[^/]*/)[0];
      if ($scope.settings.webOrigins.indexOf(newWebOrigin) === -1) {
        $scope.webOrigins[$scope.webOrigins.length - 1] = {'val': newWebOrigin};
      }
    }
  };

  $scope.addRedirectUri = function() {
    $scope.redirectUris.push({});
  };

  $scope.deleteRedirectUri = function(index) {
    $scope.redirectUris.splice(index, 1);
  };

  $scope.addWebOrigin = function() {
    $scope.webOrigins.push({});
  };

  $scope.deleteWebOrigin = function(index) {
    $scope.webOrigins.splice(index, 1);
  };

  $scope.$watch('settings', function() {
    $scope.changed = !angular.equals($scope.settings, settingsBackup);
  }, true);

  function checkValChange(toInspect, backup){
    var newCopy = angular.copy(toInspect);

    var last = newCopy[newCopy.length-1];

    if (!last.hasOwnProperty('val') || last.val === ''){
      newCopy.pop();
    }

    return !angular.equals(newCopy, backup);
  }

  $scope.$watch('redirectUris', function(a) {
    $scope.changed = checkValChange(a, redirectUrisBackup);
  }, true);

  $scope.$watch('webOrigins', function(a) {
    $scope.changed = checkValChange(a, webOriginsBackup);
  }, true);

  $scope.clear = function() {
    $scope.settings = angular.copy(settingsBackup);
    $scope.redirectUris = angular.copy(redirectUrisBackup);
    $scope.webOrigins = angular.copy(webOriginsBackup);
  };

  var arrayObjectIndexOf = function(array, object) {
    for (var i = 0; i < array.length; i++){
      if (angular.equals(array[i], object)) {
        return i;
      }
    }
    return -1;
  };

  var saveScopeMappings = function() {
    var smData = [];
    for(var i = 0; i < $scope.availableRoles.length; i++) {
      if($scope.settings.scopeMappings.indexOf($scope.availableRoles[i].id) > -1) {
        smData.push($scope.availableRoles[i]);
      }
    }

    var smDelete = [];
    for(var j = 0; j < scopeMappings.length; j++) {
      if(arrayObjectIndexOf(smData, scopeMappings[j]) === -1) {
        smDelete.push(scopeMappings[j]);
      }
    }

    var scopeMappingsUrl = '/auth/admin/realms/liveoak-apps/applications/' + $scope.settings.name +  '/scope-mappings/applications/' + $route.current.params.appId;

    var scopeMappingsAdd = function() {
      if(smData.length > 0) {
        // FIXME: For some reason, using this is causing the [..] to be passed as JSON Object {..}
        // var smRes = new LoRealmAppClientScopeMapping(smData);
        // smRes.$save({appId: $route.current.params.appId, clientId: $route.current.params.clientId});
        $http.post(scopeMappingsUrl, smData).then(
          function() {
            onSaveSuccessful();
          },
          function(httpResponse) {
            Notifications.httpError('Failed to update the scope roles.', httpResponse);
          }
        );
      }
    };

    // FIXME: It seems DELETE cannot have payload in ng-resource...
    // var smRes = new LoRealmAppClientScopeMapping(smDelete);
    // smRes.$delete({appId: $route.current.params.appId, clientId: $route.current.params.clientId});
    if(smDelete.length > 0) {
      $http.delete(scopeMappingsUrl, {data: smDelete, headers : {'content-type' : 'application/json'}}).then(
        function() {
          scopeMappingsAdd();
        },
        function(httpResponse) {
          Notifications.httpError('Failed to update the scope roles.', httpResponse);
        }
      );
    }
    else {
      scopeMappingsAdd();
    }
  };

  $scope.save = function() {
    $scope.settings.redirectUris = [];

    angular.forEach($scope.redirectUris, function(uri) {
      if (uri.hasOwnProperty('val')) {
        $scope.settings.redirectUris.push(uri.val);
      }
    });

    $scope.settings.webOrigins= [];
    angular.forEach($scope.webOrigins, function (uri) {
      if (uri.hasOwnProperty('val')) {
        $scope.settings.webOrigins.push(uri.val);
      }
    });

    var nameChanged = $scope.appClient.name !== $scope.settings.name;
    var redirectUrisChanged = !angular.equals($scope.appClient.redirectUris, $scope.settings.redirectUris);
    var webOriginsChanged = !angular.equals($scope.appClient.webOrigins, $scope.settings.webOrigins);
    if (nameChanged || redirectUrisChanged || webOriginsChanged) {
      var originalName = $scope.appClient.name;
      $scope.appClient.name = $scope.settings.name;
      $scope.appClient.redirectUris = $scope.settings.redirectUris;
      $scope.appClient.webOrigins = $scope.settings.webOrigins;
      var appClientPromise = $scope.create ? $scope.appClient.$create() : $scope.appClient.$save({appId: originalName});
      appClientPromise.then(
        function(appClient) {
          $scope.appClient = appClient;
          if(!angular.equals($scope.settings.scopeMappings, settingsBackup.scopeMappings)) {
            saveScopeMappings();
          }
          else {
            onSaveSuccessful();
          }
        },
        function(httpResponse) {
          Notifications.httpError('The client "' + originalName + '" could not be ' + ($scope.create ? 'created': 'updated') + '.', httpResponse);
        }
      );
    }
    else if (!angular.equals($scope.settings.scopeMappings, settingsBackup.scopeMappings)) {
      saveScopeMappings();
    }
  };

  var onSaveSuccessful = function() {
    Notifications.success('The client "' + $scope.appClient.name + '" has been ' + ($scope.create ? 'created': 'updated') + '.');
    var nxtLoc = 'applications/' + currentApp.name + '/application-clients/' + $scope.appClient.name;
    if ($location.path().replace(/\/+/g, '') === nxtLoc.replace(/\/+/g, '')) {
      $route.reload();
    }
    else {
      $location.path('applications/' + currentApp.name + '/application-clients/' + $scope.appClient.name);
    }
  };
});

loMod.controller('NextStepsCtrl', function($scope, $rootScope, $routeParams, currentApp, loStorageList) {

  $rootScope.curApp = currentApp;

  $scope.storageList = [];

  /* jshint unused: false */
  angular.forEach(loStorageList._members, function (value, key) {
    if (value.hasOwnProperty('db')) {
      this.push({id: value.id, provider: value.hasOwnProperty('MongoClientOptions') ? 'mongoDB' : 'unknown'});
    }
    else if(value.hasOwnProperty('upsURL')) {
      $scope.pushConfig = value;
    }
  }, $scope.storageList);
  /* jshint unused: true */

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Next Steps', 'href': '#/applications/' + currentApp.id + '/next-steps'}
  ];

});

loMod.controller('ExampleListCtrl', function($scope, $location, $filter, Notifications, LoAppExamples, LoApp, examplesList, loAppList) {

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': 'Example Applications', 'href': '#/example-applications'}
  ];

  $scope.examples = examplesList;

  for (var i = 0; i < examplesList.length; i++) {
    var example = examplesList[i];
    if ($filter('filter')(loAppList._members, {'id': example.id}, true).length > 0) {
      example.installed = true;
    }
  }

  $scope.goto = function(example) {
    $location.path('applications/' +  example.id);
  };

  $scope.import = function(example) {
    example.importing = true;

    var parentId = example.path.substr(0, example.path.indexOf('/'));
    var exampleId = example.path.substr(example.path.indexOf('/') + 1);
    var data = {
      id: example.id,
      name: example.id,
      type: 'application'
    };
    var config = LoAppExamples.get({parentId: parentId, exampleId: exampleId},
      function() {
        // FIXME: need to adapt config to match paths, etc...
        new LoApp($.extend(data, config)).$create({},
          function(/*value, responseHeaders*/) {
            new LoApp({}).$addResource({appId: example.id}, function() {
              Notifications.success('The example application "' + example.id + '" has been installed.');
              example.importing = false;
              example.installed = true;
            });
          },
          function(httpResponse) {
            Notifications.httpError('Failed to install the example application "' + example.id + '".', httpResponse);
            example.importing = false;
          }
        );
      },
      function(httpResponse) {
        Notifications.httpError('Failed to retrieve the example application "' + example.id + '" configuration.', httpResponse);
        example.importing = false;
      }
    );
  };

  $scope.install = function(example) {
    example.installing = true;

    new LoAppExamples({ id: example.id, localPath: example.path }).$install({},
      function(/*value, responseHeaders*/) {
        Notifications.success('The example application "' + example.id + '" has been installed.');
        example.installing = false;
        example.installed = true;
      },
      function(httpResponse) {
        Notifications.httpError('Failed to install the example application "' + example.id + '".', httpResponse);
        example.installing = false;
      }
    );
  };

});

