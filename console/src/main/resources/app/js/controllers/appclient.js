'use strict';

var loMod = angular.module('loApp.controllers.appclient', []);

loMod.controller('AppClientsCtrl', function($scope, $rootScope, $filter, $modal, $routeParams, Notifications, LoRealmApp,
                                            loClients, LoRealmAppClientScopeMapping, currentApp, loRealmAppRoles) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Clients', 'href': '#/applications/' + currentApp.id + '/application-clients'}
  ];

  $scope.createdId = $routeParams.created;

  $scope.namePrefix = currentApp.id + '.client.';
  $scope.availableRoles = loRealmAppRoles;

  loClients.$promise.then(function () {
    $scope.appClients = loClients.members;
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

    $scope.clientDelete = function (clientId) {
      LoClient.delete({appId: currentApp.name, clientId: clientId},
        // success
        function() {
          Notifications.success('The client "' + clientId + '" has been deleted.');
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete the client "' + clientId + '".', httpResponse);
        }
      )
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };
});

loMod.controller('AppClientCtrl', function($scope, $rootScope, $filter, $route, $location, $http, Notifications,
                                           LoRealmApp, LoRealmAppRoles, LoRealmAppClientScopeMapping, currentApp,
                                           loRealmRoles, loRealmAppRoles, scopeMappings, LoClient,
                                           loClient, LiveOak, $modal, $q, loClients) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Clients',       'href':'#/applications/' + currentApp.id + '/application-clients'}
  ];

  $scope.namePrefix = currentApp.id + '.client.';

  $scope.platformsBasic = ['html5', 'android', 'ios'];
  $scope.platformsCustom = [];

  var appClients = [];

  if(loClients.members){
    for(var id in loClients.members){
      var clientName = loClients.members[id]['app-key'];
      appClients.push(clientName);
    }
  }

  if (loClient && loClient.id) {
    $scope.loClient = loClient;
    if ($scope.platformsBasic.indexOf(loClient.type) === -1) {
      $scope.platformsCustom.push(loClient.type);
    }
    $scope.create = false;
    $scope.breadcrumbs.push({'label': loClient.id, 'href':'#/applications/' + currentApp.id + '/application-clients/' + loClient.id});
  }
  else {
    $scope.create = true;
    $scope.loClient = new LoClient();
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
    name: $scope.loClient.id,
    displayName: $scope.loClient.id,
    type: loClient.type,
    scopeMappings: loClient['app-roles'] || [],
    redirectUris: loClient['redirect-uris'] || [],
    webOrigins: loClient['web-origins'] || []
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

    $scope.loClient.id = $scope.settings.displayName;
    $scope.loClient.type = $scope.settings.type;
    $scope.loClient['redirect-uris'] = $scope.settings.redirectUris;
    $scope.loClient['web-origins'] = $scope.settings.webOrigins;
    $scope.loClient['app-roles'] = $scope.settings.scopeMappings;

    var appClientPromise = $scope.create ? $scope.loClient.$create({appId: currentApp.id}) : $scope.loClient.$update({appId: currentApp.id, clientId: $route.current.params.clientId});

    appClientPromise.then(
      function(client){
        $scope.loClient['app-key'] = client['app-key'];
        onSaveSuccessful();
      },
      function(httpResponse) {
        if (httpResponse) {
          Notifications.httpError('The LO client "' + ($scope.create ? $scope.appClient.name : originalName) + '" could not be ' + ($scope.create ? 'created' : 'updated') + '.', httpResponse);
        }
      }
    );
  };

  var onSaveSuccessful = function() {
    $scope.changed = false; // required due to LIVEOAK-736

    Notifications.success('The client "' + $scope.loClient.id + '" has been ' + ($scope.create ? 'created': 'updated') + '.');

    if ($scope.create) {
      $location.search('created', $scope.loClient['app-key']).path('applications/' + currentApp.name + '/application-clients');
    }
    else {
      $location.path('applications/' + currentApp.name + '/application-clients');
    }
  };

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);

});
