'use strict';

var loMod = angular.module('loApp.controllers.appclient', []);

loMod.controller('AppClientsCtrl', function($scope, $rootScope, $filter, $modal, $routeParams, Notifications, LoRealmApp,
                                            loClients, LoRealmAppClientScopeMapping, currentApp, loRealmAppClients) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Clients', 'href': '#/applications/' + currentApp.id + '/application-clients'}
  ];

  $scope.createdId = $routeParams.created;

  $scope.namePrefix = 'liveoak.client.' + currentApp.id + '.';

  var idClientMap = {};

  for (var client in loRealmAppClients){
    var realmClient = loRealmAppClients[client];
    idClientMap[realmClient.id] = realmClient;
  }

  loClients.$promise.then(function () {
    $scope.appClients = loClients.members;
    for (var j = 0; $scope.appClients && j < $scope.appClients.length; j++) {
      var kcClient = idClientMap[$scope.appClients[j].id];
      $scope.appClients[j].realmRoles = LoRealmAppClientScopeMapping.query({appId: currentApp.name, clientId: kcClient.name});
      $scope.appClients[j].kcClient = kcClient;
    }
  });

  // Delete Client
  $scope.modalClientDelete = function(clientId, kcId) {
    $scope.deleteClientId = clientId;
    $scope.deleteKcId = kcId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/application/client-delete.html',
      controller: DeleteClientModalCtrl,
      scope: $scope
    }).result.then(
      function() {
        var _deletedClient = $filter('filter')($scope.appClients, {'id' : kcId})[0];
        var _deletedIndex = $scope.appClients.indexOf(_deletedClient);

        $scope.appClients.splice(_deletedIndex, 1);
      }
    );
  };

  var DeleteClientModalCtrl = function ($scope, $modalInstance, $log, LoRealmApp, LoClient) {

    $scope.clientDelete = function (clientId, kcId) {
      LoClient.delete({appId: currentApp.name, clientId: kcId},
        // success
        angular.noop,
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete the LO client "' + clientId + '" data.', httpResponse);
        }
      ).$promise.then(function() {
          return LoRealmApp.delete({appId: clientId}).$promise;

        }).then( function() {
          Notifications.success('The client "' + clientId + '" has been deleted.');
          $modalInstance.close();

        }, function (httpResponse) {
          Notifications.httpError('Failed to delete the KC client "' + clientId + '".', httpResponse);
        }
      );

    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };
});

loMod.controller('AppClientCtrl', function($scope, $rootScope, $filter, $route, $location, $http, Notifications,
                                           LoRealmApp, LoRealmAppRoles, LoRealmAppClientScopeMapping, currentApp,
                                           loRealmAppClient, loRealmRoles, loRealmAppRoles, scopeMappings, LoClient,
                                           loClient, LiveOak, $modal, $q, loClients) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Clients',       'href':'#/applications/' + currentApp.id + '/application-clients'}
  ];

  $scope.namePrefix = 'liveoak.client.' + currentApp.id + '.';

  $scope.platformsBasic = ['html5', 'android', 'ios'];
  $scope.platformsCustom = [];

  var appClients = [];

  if(loClients.members){
    for(var id in loClients.members){
      var clientName = loClients.members[id]['security-key'];
      appClients.push(clientName);
    }
  }

  if (loRealmAppClient && loRealmAppClient.id) {
    $scope.loClient = loClient;
    if ($scope.platformsBasic.indexOf(loClient.type) === -1) {
      $scope.platformsCustom.push(loClient.type);
    }
    $scope.create = false;
    $scope.appClient = loRealmAppClient;
    $scope.breadcrumbs.push({'label': $filter('clientname')(loRealmAppClient.name, currentApp.id), 'href':'#/applications/' + currentApp.id + '/application-clients/' + loRealmAppClient.name});
  }
  else {
    $scope.create = true;
    $scope.appClient = new LoRealmApp();
    $scope.loClient = new LoClient();
    $scope.appClient.bearerOnly = false;
    $scope.appClient.publicClient = true;
    $scope.breadcrumbs.push({'label': 'New Client', 'href':'#/applications/' + currentApp.id + '/application-clients/create-client'});
  }

  $scope.changed = false;

  // Check if we have application-clients resource, create if not
  LoClient.getResource({appId: currentApp.name},
    // success
    angular.noop,
    // error
    function (/*httpResponse*/) {
      new LoClient({ id: 'application-clients', type: 'application-clients' }).$createResource({appId: currentApp.name});
    }
  );

  $scope.availableRoles = $filter('orderBy')(loRealmAppRoles, 'name');//loRealmRoles.concat(loRealmAppRoles);
  $scope.noRoles = ['The application has no Roles'];

  $scope.settings = {
    name: $scope.appClient.name,
    displayName: $filter('clientname')($scope.appClient.name, currentApp.id),
    type: loClient.type,
    scopeMappings: [],
    redirectUris: angular.copy($scope.appClient.redirectUris) || [],
    webOrigins: angular.copy($scope.appClient.webOrigins) || []
  };

  $scope.settings.hadPrefix = $scope.create || $scope.settings.displayName !== $scope.settings.name;
  var originalName = $scope.settings.name;

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
    return (/^.+:\/\/[^\/]*/).test(uri);
  };

  $scope.checkClientName = function(clientName){
    if (!clientName || clientName === ''){
      $scope.clientsForm.clientname.$setValidity('clientName', true);
    }
    else {
      $scope.settings.name = $scope.settings.hadPrefix ? $scope.namePrefix + $scope.settings.displayName : $scope.settings.displayName;

      if ($scope.settings.name !== originalName && appClients.indexOf($scope.settings.name) > -1) {
        $scope.clientsForm.clientname.$setValidity('clientName', false);
      } else {
        $scope.clientsForm.clientname.$setValidity('clientName', true);
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

  $scope.$watch('redirectUris', function(a) {
    $scope.changed = !angular.equals(a, redirectUrisBackup);
  }, true);

  $scope.$watch('webOrigins', function(a) {
    $scope.changed = !angular.equals(a, webOriginsBackup);
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

  var saveScopeMappings = function(showNotification) {
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

    var scopeMappingsUrl = LiveOak.getAuthServerUrl() + '/admin/realms/liveoak-apps/applications/' + $scope.settings.name +  '/scope-mappings/applications/' + $route.current.params.appId;

    var scopeMappingsAdd = function() {
      if(smData.length > 0) {
        // FIXME: For some reason, using this is causing the [..] to be passed as JSON Object {..}
        // var smRes = new LoRealmAppClientScopeMapping(smData);
        // smRes.$save({appId: $route.current.params.appId, clientId: $route.current.params.clientId});
        $http.post(scopeMappingsUrl, smData).then(
          function() {
            if (showNotification) {
              onSaveSuccessful();
            }
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

  $scope.modalPlatformAdd = function() {
    $modal.open({
      templateUrl: '/admin/console/templates/modal/application/platform-add.html',
      controller: ModalCtrlPlatformAdd,
      scope: $scope
    });
  };

  var ModalCtrlPlatformAdd = function ($scope, $modalInstance) {

    $scope.addPlatform = function(platformNew) {
      $scope.$parent.platformsCustom.push(platformNew);
      $scope.$parent.settings.type = platformNew;
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    $scope.checkPlatform = function (platform){
      if ($scope.$parent.platformsBasic.indexOf(platform) > -1 || $scope.$parent.platformsCustom.indexOf(platform) > -1){
        return false;
      }
      return true;
    };
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

    if ($scope.create) {
      if ($scope.settings.displayName.indexOf($scope.namePrefix) === -1) {
        $scope.settings.name = $scope.namePrefix + $scope.settings.displayName;
      }
      else {
        $scope.settings.name = $scope.settings.displayName;
      }
    }

    var nameChanged = $scope.appClient.name !== $scope.settings.name;
    var redirectUrisChanged = !angular.equals($scope.appClient.redirectUris, $scope.settings.redirectUris);
    var webOriginsChanged = !angular.equals($scope.appClient.webOrigins, $scope.settings.webOrigins);
    var typeChanged = !angular.equals($scope.loClient.type, $scope.settings.type);

    if (nameChanged || redirectUrisChanged || webOriginsChanged || typeChanged) {
      var originalName = $scope.appClient.name;
      $scope.appClient.name = $scope.settings.name;
      $scope.appClient.redirectUris = $scope.settings.redirectUris;
      $scope.appClient.webOrigins = $scope.settings.webOrigins;

      var appClientPromise = $scope.create ? $scope.appClient.$create() : $scope.appClient.$save({appId: originalName});

      // Save client in the KC
      appClientPromise.then(function(appClient) {
          $scope.appClient = appClient;
          if(!angular.equals($scope.settings.scopeMappings, settingsBackup.scopeMappings)) {
            saveScopeMappings();
          }
        },
        function(httpResponse) {
          if(httpResponse){
            Notifications.httpError('The KC client "' + originalName + '" could not be ' + ($scope.create ? 'created': 'updated') + '.', httpResponse);
          }

          return $q.reject();
        }
        // Get the ID of previously saved client
      ).then(function(){
          return LoRealmApp.get({appId: $scope.appClient.name}).$promise;
        }, function(httpResponse) {
          if(httpResponse){
            Notifications.httpError('The "' + originalName + '" client scope mappings could not be ' + ($scope.create ? 'created' : 'updated') + '.', httpResponse);
          }

          return $q.reject();
        }
        // Use this id when saving LO (with additional information about client type) client
      ).then(function(kcClient){
          $scope.loClient.id = kcClient.id;
          $scope.loClient.type = $scope.settings.type;
          $scope.loClient['security-key'] = $scope.appClient.name;

          return $scope.create ? $scope.loClient.$create({appId: currentApp.id}) : $scope.loClient.$update({appId: currentApp.id, clientId: $scope.appClient.id});
        }, function(){
          return $q.reject();
        }
        // Notify about operation status
      ).then(function(){
          onSaveSuccessful();
        }, function(httpResponse) {
          if (httpResponse) {
            Notifications.httpError('The LO client "' + originalName + '" could not be ' + ($scope.create ? 'created' : 'updated') + '.', httpResponse);
          }
        });
    }
    else if (!angular.equals($scope.settings.scopeMappings, settingsBackup.scopeMappings)) {
      saveScopeMappings(true);
    }
  };

  var onSaveSuccessful = function() {
    Notifications.success('The client "' + $scope.appClient.name + '" has been ' + ($scope.create ? 'created': 'updated') + '.');
    /*
     var nxtLoc = 'applications/' + currentApp.name + '/application-clients/' + $scope.appClient.name;
     if ($location.path().replace(/\/+/g, '') === nxtLoc.replace(/\/+/g, '')) {
     $route.reload();
     }
     */
    if ($scope.create) {
      $location.search('created', $scope.appClient.name).path('applications/' + currentApp.name + '/application-clients');
    }
    else {
      $location.path('applications/' + currentApp.name + '/application-clients');
    }
  };
});
