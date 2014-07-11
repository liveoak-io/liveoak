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

  $scope.collStorage = $scope.storageList.length > 0 ? $scope.storageList[0].id : undefined;

  // Create Collection Modal ---------------------------------------------------
  $scope.modalCollectionAdd = function() {
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/collection-add.html',
      controller: 'AddCollectionModalCtrl',
      scope: $scope,
      resolve: {
        currentApp: function () {
          return currentApp;
        }
      }
    });
  };
});

loMod.controller('AddCollectionModalCtrl', function ($scope, $modalInstance, $location, LoCollection, Notifications, currentApp) {

  $scope.collectionCreate = function(collectionName) {
    var newCollectionPromise = LoCollection.create({appId: currentApp.id, storageId: $scope.collStorage},
      {id : collectionName}).$promise;

    newCollectionPromise.then(
      function() {
        Notifications.success('The collection "' + $scope.collStorage + ' / ' + collectionName + '" has been created. Secure it.');
        $modalInstance.close();
        $location.path('applications/' + currentApp.id + '/security/policies/' + $scope.collStorage + '/' + collectionName);
      },
      function() {
        Notifications.error('Not able to create the collection "' + $scope.collStorage + ' / ' + collectionName + '".');
      }
    );
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});

loMod.controller('SecurityCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoRealmAppRoles, loRealmAppRoles, uriPolicies, aclPolicies, currentApp, loStorageList) {

  $rootScope.curApp = currentApp;

  $scope.currentCollection = $route.current.params.storageId + '/' + $route.current.params.collectionId;
  $scope.currentCollectionDisplay = $route.current.params.storageId + ' / ' + $route.current.params.collectionId;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure ' + $scope.currentCollectionDisplay, 'href': '#/applications/' + currentApp.id + '/security/' + $scope.currentCollection}
  ];

  $scope.storageList = $filter('filter')(loStorageList._members, {'type': 'database'});

  $scope.collectionsList = [];
  angular.forEach($scope.storageList, function(storage) {
    angular.forEach(storage._members, function(collection) {
      $scope.collectionsList.push({id: storage.id + '/' + collection.id, name: storage.id + ' / ' + collection.id});
    });
  });

  $scope.$watch('currentCollection',  function(/*newVal*/) {
    $location.path('applications/' + currentApp.id + '/security/policies/' + $scope.currentCollection);
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

  $scope.modalAddRole = function() {
    var modalAddRole = $modal.open({
      templateUrl: '/admin/console/templates/modal/application/role-add.html',
      controller: 'AddRoleModalCtrl',
      scope: $scope,
      resolve: {
        roles: function () {
          return $scope.appRoles;
        },
        currentApp: function () {
          return currentApp;
        }
      }
    });

    modalAddRole.result.then(
      function(newRole) { // modal completion
        $scope.appRoles = LoRealmAppRoles.query({appId: currentApp.id});
        $scope.settings.accessRoles.push(currentApp.name + '/' + newRole);
      }
    );
  };

  $scope.toggleItem = function(item, array) {
    var found = $.inArray(item, array);
    if (found >= 0) {
      array.splice(found, 1);
    }
    else {
      array.push(item);
    }
  };

  $scope.collStorage = $route.current.params.storageId;

  // Create Collection Modal ---------------------------------------------------
  $scope.modalCollectionAdd = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/collection-add.html',
      controller: 'AddCollectionModalCtrl',
      scope: $scope,
      resolve: {
        currentApp: function () {
          return currentApp;
        }
      }
    });
  };
});

// -- Security Roles -----------------------------------------------------------

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

  var resetEnv = function() {
    $scope.settings = {
      roles: loRealmAppRoles,
      defaultRoles: loRealmApp.defaultRoles || []
    };
  };

  $scope.toggleDefaultRole = function(roleName) {
    var idx = $scope.settings.defaultRoles.indexOf(roleName);
    if (idx > -1) {
      $scope.settings.defaultRoles.splice(idx, 1);
    }
    else {
      $scope.settings.defaultRoles.push(roleName);
    }

    loRealmApp.defaultRoles = $scope.settings.defaultRoles;
    loRealmApp.$save({realmId: 'liveoak-apps', appId: currentApp.id},
      function(value/*, responseHeaders*/) {
        Notifications.success('The application default roles have been set to: "' + value.defaultRoles + '".');
      },
      function (httpResponse) {
        Notifications.error('Unable to configure the application default roles.', httpResponse);
      }
    );
  };

  $scope.modalAddRole = function() {
    var modalAddRole = $modal.open({
      templateUrl: '/admin/console/templates/modal/application/role-add.html',
      controller: 'AddRoleModalCtrl',
      scope: $scope,
      resolve: {
        roles: function () {
          return $scope.settings.roles;
        },
        currentApp: function () {
          return currentApp;
        }
      }
    });

    modalAddRole.result.then(
      function() { // modal completion
        $scope.settings.roles = LoRealmAppRoles.query({appId: currentApp.id});
      }
    );
  };

  // Delete Role
  $scope.modalDeleteRole = function(role) {
    $scope.role = role;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/role-delete.html',
      controller: DeleteRoleModalCtrl,
      scope: $scope
    }).result.then(
      function() {
        $scope.settings.roles = LoRealmAppRoles.query({appId: currentApp.id});
      }
    );
  };

  var DeleteRoleModalCtrl = function ($scope, $modalInstance) {

    var deleteRoleSuccessCallback = function(value) {
      Notifications.success('The application role "' +  value.name + '" has been deleted.');
      $modalInstance.close();
    };

    var deleteRoleFailureCallback = function(httpResponse) {
      Notifications.error('Unable to delete the application role "' +  httpResponse.config.data.name + '".', httpResponse);
    };

    $scope.roleDelete = function (role) {
      role.$delete({realmId: 'liveoak-apps', appId: currentApp.id, roleName: role.name},
        deleteRoleSuccessCallback, deleteRoleFailureCallback
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  };

});

loMod.controller('AddRoleModalCtrl', function($scope, $modalInstance, Notifications, LoRealmAppRoles, currentApp, roles) {
  $scope.newRole = new LoRealmAppRoles();

  $scope.addRole = function () {
    for(var i = 0; i < roles.length; i++) {
      if(roles[i].name === $scope.newRole.name) {
        Notifications.error('The role with name "' + $scope.newRole.name + '" already exists in the application "' + currentApp.id + '".');
        return;
      }
    }

    var addRoleSuccessCallback = function(value) {
      Notifications.success('The application role "' + value.name + '" has been created.');
      $modalInstance.close(value.name);
    };

    var addRoleFailureCallback = function(httpResponse) {
      Notifications.error('Unable to create the application role "' + httpResponse.config.data.name + '".', httpResponse);
    };

    $scope.newRole.$save({realmId: 'liveoak-apps', appId: currentApp.id},
      addRoleSuccessCallback, addRoleFailureCallback
    );
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});

// -- Security Users -----------------------------------------------------------

loMod.controller('SecurityUsersCtrl', function($scope, $rootScope, $log, $route, $modal, currentApp, realmUsers, Notifications, LoRealmUsers) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Users', 'href': '#/applications/' + currentApp.id + '/security/users'}
  ];

  $scope.users = realmUsers;

  // Delete Application
  $scope.modalUserDelete = function(userId) {
    $scope.deleteUserId = userId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/user-delete.html',
      controller: DeleteUserModalCtrl,
      scope: $scope
    }).result.then(function () {
        $scope.users = realmUsers = LoRealmUsers.query();
      });
  };

  var DeleteUserModalCtrl = function ($scope, $modalInstance, $log, LoRealmUsers) {

    $scope.userDelete = function (userId) {
      $log.debug('Deleting user: ' + userId);
      LoRealmUsers.delete({userId: userId},
        // success
        function(/*value, responseHeaders*/) {
          Notifications.success('The user "' + userId + '" has been deleted.');
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete the user "' + userId + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

});

loMod.controller('SecurityUsersAddCtrl', function($scope, $rootScope, $log, $route, $location, $http, $modal, currentApp, userProfile, userRoles, appRoles, LoRealmUsers, Notifications) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Users', 'href': '#/applications/' + currentApp.id + '/security/users'}
  ];

  $scope.changed = false;
  $scope.create = !userProfile.username;

  if($scope.create) {
    $scope.breadcrumbs.push({'label': 'Add User', 'href': '#/applications/' + currentApp.id + '/security/add-user'});
  }
  else {
    $scope.breadcrumbs.push({'label': userProfile.username, 'href': '#/applications/' + currentApp.id + '/security/users/' + userProfile.username});
  }

  $scope.appRoles = appRoles;

  var userModel = {
    profile: userProfile,
    roles: []
  };

  var userModelBackup = angular.copy(userModel);

  angular.forEach(userRoles, function(role) {userModelBackup.roles.push(role.id);});
  $scope.userModel = angular.copy(userModelBackup);

  $scope.clear = function() {
    $scope.userModel = angular.copy(userModelBackup);
    $scope.changed = false;
  };

  $scope.$watch('userModel', function() {
    $scope.changed = !angular.equals($scope.userModel, userModelBackup);
  }, true);

  $scope.inputType = 'password';
  $scope.changeInputType = function() {
    if ($scope.inputType === 'password') {
      $scope.inputType = 'text';
    }
    else {
      $scope.inputType = 'password';
    }
  };

  var userSaveSuccess = function() {
    $scope.changed = false;
    userProfile = angular.copy($scope.userModel.profile);

    $location.url('/applications/' + currentApp.name + '/security/users');
    Notifications.success('The user "' + $scope.userModel.profile.username + '" has been ' + ($scope.create ? 'created' : 'updated') + '.');

    // Set password
    if ($scope.userModel.password) {
      new LoRealmUsers({type: 'password', value: $scope.userModel.password}).$resetPassword({userId: $scope.userModel.profile.username},
        function () {
          Notifications.success('The password for the user "' + $scope.userModel.profile.username + '" was updated.');
        },
        function (httpResponse) {
          Notifications.error('Failed to set the password for user "' + $scope.userModel.profile.username + '".', httpResponse);
        }
      );
    }

    var roleMappingsUrl = '/auth/admin/realms/liveoak-apps/users/' + $scope.userModel.profile.username + '/role-mappings/applications/' + currentApp.name;

    var rolesData = [];
    for(var i = 0; i < appRoles.length; i++) {
      if($scope.userModel.roles.indexOf(appRoles[i].id) > -1) {
        rolesData.push(appRoles[i]);
      }
    }

    $http.delete(roleMappingsUrl, {data: appRoles, headers : {'content-type' : 'application/json'}}).then(
      function() {
        $http.post(roleMappingsUrl, rolesData).then(
          function() {
            Notifications.success('The roles for the user "' + $scope.userModel.profile.username + '" have been updated.');
          },
          function(httpResponse) {
            Notifications.error('Failed to set the roles for user "' + $scope.userModel.profile.username + '".', httpResponse);
          }
        );
      },
      function(httpResponse) {
        Notifications.error('Failed to clear the roles for user "' + $scope.userModel.profile.username + '".', httpResponse);
      }
    );
    /*
     // FIXME: It seems DELETE cannot have payload in ng-resource...
     new LoRealmUsers(appRoles).$deleteRoles({appId: currentApp.name, userId: $scope.userModel.profile.username},
     function() {
     // FIXME: For some reason, using this is causing the [..] to be passed as JSON Object {..}
     new LoRealmUsers($scope.userModel.roles).$addRoles({appId: currentApp.name, userId: $scope.userModel.profile.username},
     function() {
     Notifications.success('The roles for the user "' + $scope.userModel.profile.username + '" have been updated.');
     },
     function(httpResponse) {
     Notifications.error('Failed to set the roles for user ' + $scope.userModel.profile.username + '.', httpResponse);
     }
     );
     },
     function(httpResponse) {
     Notifications.error('Failed to clear the roles for user ' + $scope.userModel.profile.username + '.', httpResponse);
     }
     );
     */
  };

  var userSaveFailure = function(httpResponse) {
    Notifications.error('Failed to create the user ' + $scope.userModel.profile.username + '.', httpResponse);
  };

  $scope.save = function() {
    console.log($scope.userModel);
    if (!$scope.userModel.profile.username) {
      Notifications.error('The "Username" field is required for user creation.');
    }
    else {
      if ($scope.create) {
        $scope.userModel.profile.$save({}, userSaveSuccess, userSaveFailure);
      }
      else {
        $scope.userModel.profile.$update({userId: $scope.userModel.profile.username}, userSaveSuccess, userSaveFailure);
      }
    }
  };

  // Delete Application
  $scope.modalResetPassword = function() {
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/password-reset.html',
      controller: ResetPasswordModalCtrl,
      scope: $scope
    });
  };

  var ResetPasswordModalCtrl = function ($scope, $modalInstance, $log, $route, LoRealmUsers) {

    $scope.userId = $route.current.params.userId;

    $scope.userPasswordReset = function () {
      $log.debug('Resetting password for user: ' + $scope.userId);
      new LoRealmUsers({type: 'password', value: $scope.$parent.userModel.password}).$resetPassword({userId: $scope.userId},
        function () {
          Notifications.success('The password for the user "' + $scope.userId + '" was reset.');
          delete $scope.$parent.userModel.password;
          $modalInstance.close();
        },
        function (httpResponse) {
          Notifications.error('Failed to reset the password for user "' + $scope.userId + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };


});
