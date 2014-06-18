'use strict';

var loMod = angular.module('loApp.controllers.security', []);

loMod.controller('SecurityListCtrl', function($scope, $rootScope, $location, $log, $filter, $modal, Notifications, LoSecurity, LoACL, expAppResources, expApp, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'}
  ];

  $scope.storageList = $filter('filter')(expApp._members, {'type': 'database'});

  var _uriPolicies = $filter('filter')(expAppResources._members, {'id': 'uri-policy' });
  $scope.uriPolicies = _uriPolicies.length > 0 ? _uriPolicies[0].rules : [];

  var _aclPolicies = $filter('filter')(expAppResources._members, {'id': 'acl-policy' });
  $scope.acl = _aclPolicies.length > 0 ? _aclPolicies[0].autoRules : [];

  $scope.securedColls = [];
  $scope.unsecuredColls = [];
  angular.forEach($scope.storageList, function(storage) {
    angular.forEach(storage._members, function(collection) {
      if (($filter('filter')($scope.acl, {'resourcePath': collection.self.href })).length > 0 || ($filter('filter')($scope.uriPolicies, {'uriPattern': collection.self.href })).length > 0) {
        $scope.securedColls.push(collection.self.href);
      }
      else {
        $scope.unsecuredColls.push({storage: storage.id, collection: collection.id});
      }
    });
  });

  // Delete Collection Security
  $scope.modalCollectionSecurityDelete = function(storageId, collectionId) {
    $scope.deleteStorageId = storageId;
    $scope.deleteCollectionId = collectionId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/collection-security-delete.html',
      controller: DeleteCollectionSecurityModalCtrl,
      scope: $scope
    });
  };

  var DeleteCollectionSecurityModalCtrl = function ($scope, $modalInstance) {

    $scope.collectionSecurityDelete = function (storageId, collectionId) {
      Notifications.warn('Just kidding, Security settings for ' + storageId + ' / ' + collectionId + ' were not deleted.. yet!');
      $modalInstance.close();
      // FIXME: Delete only single security config, not all!
      // new LoSecurity().$delete({appId: currentApp.id});
      // new LoACL().$delete({appId: currentApp.id});
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };
});

loMod.controller('SecurityCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, loRealmAppRoles, uriPolicies, aclPolicies, currentApp, loStorageList) {

  $rootScope.curApp = currentApp;

  $scope.currentCollection = $route.current.params.storageId + '/' + $route.current.params.collectionId;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure ' + $scope.currentCollection, 'href': '#/applications/' + currentApp.id + '/security/' + $scope.currentCollection}
  ];

  $scope.storageList = $filter('filter')(loStorageList._members, {'type': 'database'});

  $scope.collectionsList = [];
  angular.forEach($scope.storageList, function(storage) {
    angular.forEach(storage._members, function(collection) {
      $scope.collectionsList.push({id: storage.id + '/' + collection.id, name: storage.id + ' / ' + collection.id});
    });
  });

  $scope.$watch('currentCollection',  function(/*newVal*/) {
    $location.path('applications/' + currentApp.id + '/security/' + $scope.currentCollection);
  });

  var userPath = '/' + currentApp.id + '/' + $scope.currentCollection;
  var superuserPath = userPath + '/*';

  var newUriPolicyRule = function(uriPattern, requestTypes, allowedRoles) {
    return {
      uriPattern : uriPattern,
      requestTypes : requestTypes,
      allowedRoles : allowedRoles,
      deniedRoles : null,
      allowedUsers : null,
      deniedUsers : null
    };
  };

  var newUriPolicies;
  if(!uriPolicies) {
    newUriPolicies = new LoSecurity();
    newUriPolicies.type = 'uri-policy';
    newUriPolicies.config = {
      id : 'uri-policy',
      rules : [
        newUriPolicyRule(userPath, ['READ'], ['']),
        newUriPolicyRule(userPath, ['CREATE'], ['']),
        newUriPolicyRule(superuserPath, ['READ'], ['']),
        newUriPolicyRule(superuserPath, ['UPDATE'], ['']),
        newUriPolicyRule(superuserPath, ['DELETE'], [''])
      ]
    };
    uriPolicies = newUriPolicies.config;
  }

  var newAclPolicies;
  if(!aclPolicies) {
    newAclPolicies = new LoACL();
    newAclPolicies.type = 'acl-policy';
    newAclPolicies.config = {
      id: 'acl-policy',
      autoRules: [{
        resourcePath: userPath,
        autoAddedOwnerPermissions: []
      }]
    };
    aclPolicies = newAclPolicies.config;
  }

  var colUriPolicies = $filter('filter')(uriPolicies.rules, {'uriPattern': userPath });

  $scope.appRoles = loRealmAppRoles;
  $scope.settings = {};

  if (colUriPolicies.length > 0) {
    // FIXME: move this to some function which will create if doesn't exist
    var _access = ($filter('filter')(colUriPolicies, {'uriPattern': '!' + superuserPath, requestTypes: 'READ' }));
    var _create = ($filter('filter')(colUriPolicies, {'uriPattern': '!' + superuserPath, requestTypes: 'CREATE' }));
    var _readAll = ($filter('filter')(colUriPolicies, {'uriPattern': superuserPath, requestTypes: 'READ' }));
    var _updateAll = ($filter('filter')(colUriPolicies, {'uriPattern': superuserPath, requestTypes: 'UPDATE' }));
    var _deleteAll = ($filter('filter')(colUriPolicies, {'uriPattern': superuserPath, requestTypes: 'DELETE' }));

    $scope.settings.accessRoles = _access.length > 0 ? _access[0].allowedRoles : [];
    $scope.settings.createEntryRoles = _create.length > 0 ? _create[0].allowedRoles : [];
    $scope.settings.readAllRoles = _readAll.length > 0 ? _readAll[0].allowedRoles : [];
    $scope.settings.updateAllRoles = _updateAll.length > 0 ? _updateAll[0].allowedRoles : [];
    $scope.settings.deleteAllRoles = _deleteAll.length > 0 ? _deleteAll[0].allowedRoles : [];
  }
  else {
    $scope.settings.accessRoles      = [];
    $scope.settings.createEntryRoles = [];
    $scope.settings.readAllRoles     = [];
    $scope.settings.updateAllRoles   = [];
    $scope.settings.deleteAllRoles   = [];
  }

  var colAclPolicies = $filter('filter')(aclPolicies.autoRules, {'resourcePath': userPath });

  $scope.availablePermissions = ['READ','UPDATE','DELETE'];

  if(colAclPolicies.length > 0) {
    $scope.settings.ownerPermissions = colAclPolicies[0].autoAddedOwnerPermissions;
  }
  else {
    $scope.settings.ownerPermissions = [];
  }
  var settingsBackup = angular.copy($scope.settings);

  $scope.changed = false;

  function doWatch() {
    $scope.$watch('settings', function() {
      $scope.changed = !angular.equals($scope.settings, settingsBackup);
    }, true);
  }
  doWatch();

  $scope.clear = function() {
    $scope.settings = angular.copy(settingsBackup);
  };

  $scope.save = function() {
    var hasUserRead, hasUserCreate, hasSuperRead, hasSuperUpdate, hasSuperDelete = false;
    for (var u = 0; u < uriPolicies.rules.length; u++) {
      if (uriPolicies.rules[u].uriPattern === userPath) {
        if (angular.equals(uriPolicies.rules[u].requestTypes, ['READ'])) {
          uriPolicies.rules[u].allowedRoles = $scope.settings.accessRoles;
          hasUserRead = true;
        }
        else if (angular.equals(uriPolicies.rules[u].requestTypes, ['CREATE'])) {
          uriPolicies.rules[u].allowedRoles = $scope.settings.createEntryRoles;
          hasUserCreate = true;
        }
      }
      else if (uriPolicies.rules[u].uriPattern === superuserPath) {
        if (angular.equals(uriPolicies.rules[u].requestTypes, ['READ'])) {
          uriPolicies.rules[u].allowedRoles = $scope.settings.readAllRoles;
          hasSuperRead = true;
        }
        else if (angular.equals(uriPolicies.rules[u].requestTypes, ['UPDATE'])) {
          uriPolicies.rules[u].allowedRoles = $scope.settings.updateAllRoles;
          hasSuperUpdate = true;
        }
        else if (angular.equals(uriPolicies.rules[u].requestTypes, ['DELETE'])) {
          uriPolicies.rules[u].allowedRoles = $scope.settings.deleteAllRoles;
          hasSuperDelete = true;
        }
      }
    }

    if(!hasUserRead) { uriPolicies.rules.push(newUriPolicyRule(userPath, ['READ'], $scope.settings.accessRoles)); }
    if(!hasUserCreate) { uriPolicies.rules.push(newUriPolicyRule(userPath, ['CREATE'], $scope.settings.createEntryRoles)); }
    if(!hasSuperRead) { uriPolicies.rules.push(newUriPolicyRule(superuserPath, ['READ'], $scope.settings.readAllRoles)); }
    if(!hasSuperUpdate) { uriPolicies.rules.push(newUriPolicyRule(superuserPath, ['UPDATE'], $scope.settings.updateAllRoles)); }
    if(!hasSuperDelete) { uriPolicies.rules.push(newUriPolicyRule(superuserPath, ['DELETE'], $scope.settings.deleteAllRoles)); }

    var uriSuccess = function(value/*, responseHeaders*/) {
      settingsBackup.accessRoles = $scope.settings.accessRoles;
      settingsBackup.createEntryRoles = $scope.settings.createEntryRoles;
      settingsBackup.readAllRoles = $scope.settings.readAllRoles;
      settingsBackup.updateAllRoles = $scope.settings.updateAllRoles;
      settingsBackup.deleteAllRoles = $scope.settings.deleteAllRoles = $scope.settings.deleteAllRoles;
      doWatch();

      newUriPolicies = undefined;
      uriPolicies = value;
      Notifications.success('The application URI Policies have been updated.');
    };

    var uriFailure = function (httpResponse) {
      Notifications.error('Failed to update the application URI Policies.', httpResponse);
    };

    if (newUriPolicies) {
      newUriPolicies.$create({appId: currentApp.name}, uriSuccess, uriFailure);
    }
    else {
      // TODO: save only on diff
      uriPolicies.$save({appId: currentApp.name}, uriSuccess, uriFailure);
    }

    var hasACLPermissions = false;
    for (var i = 0; i < aclPolicies.autoRules.length; i++) {
      if (aclPolicies.autoRules[i].resourcePath === userPath) {
        aclPolicies.autoRules[i].autoAddedOwnerPermissions = $scope.settings.ownerPermissions;
        hasACLPermissions = true;
      }
    }

    if (!hasACLPermissions) {
      aclPolicies.autoRules.push({resourcePath: userPath, autoAddedOwnerPermissions: $scope.settings.ownerPermissions });
    }

    var aclSuccess = function(value/*, responseHeaders*/) {
      settingsBackup.ownerPermissions = $scope.settings.ownerPermissions = $scope.settings.ownerPermissions;
      doWatch();

      newAclPolicies = undefined;
      aclPolicies = value;
      Notifications.success('The application ACL Policies have been updated.');
    };
    var aclFailure = function (httpResponse) {
      Notifications.error('Failed to update the application ACL Policies.', httpResponse);
    };

    if(newAclPolicies) {
      newAclPolicies.$create({appId: currentApp.name}, aclSuccess, aclFailure);
    }
    else {
      // TODO: save only on diff
      aclPolicies.$save({appId: currentApp.name}, aclSuccess, aclFailure);
    }
  };

});

// -- Security Policies --------------------------------------------------------

loMod.controller('SecurityRolesCtrl', function($scope, $rootScope, $log, $route, $modal, currentApp, LoRealmApp, LoRealmAppRoles, loRealmApp, /*loRealmAppRoles,*/ Notifications) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Roles', 'href': '#/applications/' + currentApp.id + '/security/roles'}
  ];

  // FIXME: LIVEOAK-339 - Remove this once it's done properly on server-side
  var loRealmAppRoles;
  LoRealmAppRoles.query({appId: currentApp.id}).$promise.then(function(data) {
      loRealmAppRoles = data;
      resetEnv();
    }
  );

  var settingsBackup = {};
  var resetEnv = function() {
    $scope.settings = {
      roles: loRealmAppRoles,
      defaultRoles: angular.copy(loRealmApp.defaultRoles) || [],
      deletedRoles: [],
      newRoles: []
    };
    settingsBackup = angular.copy($scope.settings);
  };

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
      Notifications.success('The application role "' +  value.name + '" has been deleted.');
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
  };
});

