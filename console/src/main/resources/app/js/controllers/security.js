'use strict';

var loMod = angular.module('loApp.controllers.security', []);

loMod.controller('SecurityListCtrl', function($scope, $rootScope, $location, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoStorage, LoCollection, expAppResources, expApp, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'}
  ];

  $scope.storageList = $filter('filter')(expApp.members, {'type': 'database'});

  var _uriPolicies = $filter('filter')(expAppResources.members, {'id': 'uri-policy' });
  $scope.uriPolicies = _uriPolicies && _uriPolicies.length > 0 ? _uriPolicies[0].rules : [];

  var _aclPolicies = $filter('filter')(expAppResources.members, {'id': 'acl-policy' });
  $scope.acl = _aclPolicies && _aclPolicies.length > 0 ? _aclPolicies[0].autoRules : [];

  $scope.securedStorages = {uriPolicies: [], aclPolicies: []};
  $scope.securedCollections = {uriPolicies: [], aclPolicies: []};
  $scope.securedPush = {uriPolicies: [], aclPolicies: []};
  $scope.securedClients = {uriPolicies: [], aclPolicies: []};
  $scope.securedBusinessLogic = {uriPolicies: [], aclPolicies: []};

  $scope.hasPushRes = $filter('filter')(expAppResources.members, {'id': 'push' }).length > 0;
  $scope.hasClientsRes = $filter('filter')(expAppResources.members, {'id': 'application-clients' }).length > 0;
  $scope.hasBusinessLogicRes = $filter('filter')(expAppResources.members, {'id': 'scripts' }).length > 0;

  // Iterate through URI policies and distribute them per resource type
  for (var i = 0; i < $scope.uriPolicies.length; i++) {
    if ($scope.uriPolicies[i].uriPattern === '/' + currentApp.id + '/push') {
      $scope.securedPush.uriPolicies.push($scope.uriPolicies[i]);
    }
    else if ($scope.uriPolicies[i].uriPattern === '/' + currentApp.id + '/clients' || $scope.uriPolicies[i].uriPattern === '/' + currentApp.id + '/clients/*') {
      $scope.securedClients.uriPolicies.push($scope.uriPolicies[i]);
    }
    else if ($scope.uriPolicies[i].uriPattern.indexOf('/admin/applications/' + currentApp.id + '/resources/scripts') === 0) {
      $scope.securedBusinessLogic.uriPolicies.push($scope.uriPolicies[i]);
    }
    else {
      var pathElemsUri = $scope.uriPolicies[i].uriPattern.split('/');
      var extraUri = pathElemsUri[pathElemsUri.length-1] === '*' ? -1 : 0;
      if (pathElemsUri.length + extraUri === 3) {
        // storage
        if ($filter('filter')($scope.storageList, {'id': pathElemsUri[2]}, true).length > 0) {
          $scope.securedStorages.uriPolicies.push($scope.uriPolicies[i]);
        }
        else {
          if (!$scope.missingSecuredStorages) {$scope.missingSecuredStorages = {};}
          if (!$scope.missingSecuredStorages[pathElemsUri[2]]) {
            $scope.missingSecuredStorages[pathElemsUri[2]] = {id: pathElemsUri[2], uriPolicies: [], aclPolicies: []};
          }
          $scope.missingSecuredStorages[pathElemsUri[2]].uriPolicies.push($scope.uriPolicies[i]);
        }
      }
      else if (pathElemsUri.length + extraUri === 4) {
        // collection
        var uriCollStorage = $filter('filter')($scope.storageList, {'id': pathElemsUri[2]}, true);
        if (uriCollStorage[0] && uriCollStorage[0].members && $filter('filter')(uriCollStorage[0].members, {'id': pathElemsUri[3]}, true).length > 0) {
          $scope.securedCollections.uriPolicies.push($scope.uriPolicies[i]);
        }
        else {
          if (!$scope.missingSecuredCollections) { $scope.missingSecuredCollections = {}; }
          if (!$scope.missingSecuredCollections[pathElemsUri[3]]) {
            $scope.missingSecuredCollections[pathElemsUri[3]] = {id: pathElemsUri[3], storage: pathElemsUri[2], uriPolicies: [], aclPolicies: []};
          }
          $scope.missingSecuredCollections[pathElemsUri[3]].uriPolicies.push($scope.uriPolicies[i]);
        }
      }
    }
  }

  // Iterate through ACL policies and distribute them per resource type
  for (i = 0; i < $scope.acl.length; i++) {
    if ($scope.acl[i].resourcePath === '/' + currentApp.id + '/push') {
      $scope.securedPush.aclPolicies.push($scope.acl[i]);
    }
    else if ($scope.acl[i].resourcePath === '/' + currentApp.id + '/clients') {
      $scope.securedClients.aclPolicies.push($scope.acl[i]);
    }
    else if ($scope.acl[i].resourcePath.indexOf('/admin/applications/' + currentApp.id + '/resources/scripts') === 0) {
      $scope.securedBusinessLogic.aclPolicies.push($scope.acl[i]);
    }
    else {
      var pathElemsAcl = $scope.acl[i].resourcePath.split('/');
      var extraAcl = pathElemsAcl[pathElemsAcl.length-1] === '*' ? -1 : 0;
      if (pathElemsAcl.length + extraAcl === 3) {
        // storage
        if ($filter('filter')($scope.storageList, {'id': pathElemsAcl[2]}, true).length > 0) {
          $scope.securedStorages.aclPolicies.push($scope.acl[i]);
        }
        else {
          if (!$scope.missingSecuredStorages) { $scope.missingSecuredStorages = {}; }
          if (!$scope.missingSecuredStorages[pathElemsAcl[2]]) {
            $scope.missingSecuredStorages[pathElemsAcl[2]] = {id: pathElemsAcl[2], uriPolicies: [], aclPolicies: []};
          }
          $scope.missingSecuredStorages[pathElemsAcl[2]].aclPolicies.push($scope.acl[i]);
        }
      }
      else if (pathElemsAcl.length + extraAcl === 4) {
        // collection
        var aclCollStorage = $filter('filter')($scope.storageList, {'id': pathElemsAcl[2]}, true);
        if (aclCollStorage[0] && aclCollStorage[0].members && $filter('filter')(aclCollStorage[0].members, {'id': pathElemsAcl[3]}, true).length > 0) {
          $scope.securedCollections.aclPolicies.push($scope.acl[i]);
        }
        else {
          if (!$scope.missingSecuredCollections) {$scope.missingSecuredCollections = {};}
          if (!$scope.missingSecuredCollections[pathElemsAcl[3]]) {
            $scope.missingSecuredCollections[pathElemsAcl[3]] = {id: pathElemsAcl[3], storage: pathElemsAcl[2], uriPolicies: [], aclPolicies: []};
          }
          $scope.missingSecuredCollections[pathElemsAcl[3]].aclPolicies.push($scope.acl[i]);
        }
      }
    }
  }

  $scope.unsecuredCollections = [];
  $scope.unsecuredStorages = [];
  // Iterate through storages and collections to fetch unsecured ones
  angular.forEach($scope.storageList, function(storage) {
    angular.forEach(storage.members, function(collection) {
      if (($filter('filter')($scope.acl, {'resourcePath': collection.self.href })).length === 0 && ($filter('filter')($scope.uriPolicies, {'uriPattern': collection.self.href })).length === 0) {
        $scope.unsecuredCollections.push({storage: storage.id, collection: collection.id});
      }
    });
    if (($filter('filter')($scope.acl, {'resourcePath': storage.self.href }, true)).length === 0 && ($filter('filter')($scope.uriPolicies, {'uriPattern': storage.self.href }, true)).length === 0) {
      $scope.unsecuredStorages.push({id: storage.id});
    }
  });

  // Delete Collection Security
  $scope.modalResourceSecurityDelete = function(resourceId, parentId) {
    $scope.deleteResourceId = resourceId;
    $scope.deleteParentId = parentId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/resource-security-delete.html',
      controller: DeleteResourceSecurityModalCtrl,
      scope: $scope
    });
  };

  var DeleteResourceSecurityModalCtrl = function ($scope, $route, $modalInstance) {

    function deleteResourceSecurity(paths, resourceId, parentId) {
      var newAclPolicies = new LoACL();
      newAclPolicies.autoRules = $scope.acl;

      var newUriPolicies = new LoSecurity();
      newUriPolicies.rules = $scope.uriPolicies;

      for(var p in paths) {
        newAclPolicies.autoRules = $filter('filter')(newAclPolicies.autoRules, {'resourcePath': ('!' + paths[p])}, true);
        newUriPolicies.rules = $filter('filter')(newUriPolicies.rules, {'uriPattern': ('!' + paths[p])}, true);
      }

      var deleteUriSuccess = function(/*value, responseHeaders*/) {
        newAclPolicies.$save({appId: currentApp.name}, deleteAclSuccess, deleteFailure);
      };

      var deleteAclSuccess = function(/*value, responseHeaders*/) {
        Notifications.success('Deleted the application Security Policies for "' + (parentId ? (parentId + ' / ') : '') + resourceId + '".');
        $modalInstance.close();
        $route.reload();
      };

      var deleteFailure = function (httpResponse) {
        Notifications.error('Failed to delete the application Security Policies for "' + (parentId ? (parentId + ' / ') : '') + resourceId + '".', httpResponse);
      };

      newUriPolicies.$save({appId: currentApp.name}, deleteUriSuccess, deleteFailure);
    }

    $scope.resourceSecurityDelete = function (resourceId, parentId) {
      if (resourceId === 'Push') {
        deleteResourceSecurity(['/' + currentApp.id + '/push'], resourceId, parentId);
      }
      else if (resourceId === 'Business Logic') {
        deleteResourceSecurity(['/admin/applications/' + currentApp.id + '/resources/scripts'], resourceId, parentId);
      }
      else if (resourceId === 'Clients') {
        deleteResourceSecurity(['/' + currentApp.id + '/clients', '/' + currentApp.id + '/clients/*'], resourceId, parentId);
      }
      else if (!parentId) {
        deleteResourceSecurity(['/' + currentApp.id + '/' + resourceId, '/' + currentApp.id + '/' + resourceId + '/*'], resourceId, parentId);
      }
      else {
        deleteResourceSecurity(['/' + currentApp.id + '/' + parentId + '/' + resourceId, '/' + currentApp.id + '/' + parentId + '/' + resourceId + '/*'], resourceId, parentId);
      }
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  };

  // Restore Resource
  $scope.modalResourceRestore = function(resourceId, parentId) {
    $scope.isStorage = parentId ? false : true;
    $scope.restoreResourceId = parentId ? (parentId + '/' + resourceId) : resourceId;

    if (parentId && $filter('filter')($scope.storageList, {'id': parentId}).length === 0) {
      $scope.restoreParentId = parentId;
    }

    $modal.open({
      templateUrl: '/admin/console/templates/modal/security/resource-restore.html',
      controller: RestoreResourceModalCtrl,
      scope: $scope
    });
  };

  var RestoreResourceModalCtrl = function ($scope, $route, $modalInstance) {

    var restoreStorage = function(storageId) {
      var storageData = {
        id: storageId,
        type: 'mongo',
        config: {
          db: $scope.curApp.id,
          servers: [{host: 'localhost', port: 27017}],
          credentials: []
        }
      };

      return LoStorage.create({appId: $scope.curApp.id}, storageData).$promise;
    };

    var restoreCollection = function(collection) {
      var resources = collection.split('/');
      var collectionData = {id : resources[1]};
      return LoCollection.create({appId: $scope.curApp.id, storageId: resources[0]}, collectionData).$promise;
    };

    var closeAndReload = function(notifFunc, message, httpResponse) {
      notifFunc(message, httpResponse);
      $modalInstance.close();
      $route.reload();
    };

    $scope.resourceRestore = function (resourceId, parentId) {
      if (parentId || $scope.isStorage) {
        var restoredStoragePromise = restoreStorage(parentId || resourceId);

        restoredStoragePromise.then(function() {
          if ($scope.isStorage) {
            closeAndReload(Notifications.success, 'The storage "' + resourceId + '" has been created.');
          }
          else {
            var restoredCollectionPromise = restoreCollection(resourceId);
            restoredCollectionPromise.then(function() {
              closeAndReload(Notifications.success, 'The storage "' + parentId + '" and collection "' + resourceId + '" have been created.');
            },
            function(httpResponse) {
              closeAndReload(Notifications.httpError, 'The storage "' + parentId + '" has been created but not able to create collection "' + resourceId + '".', httpResponse);
            });
          }
        },
        function(httpResponse) {
          closeAndReload(Notifications.httpError, 'Not able to create the storage "' + resourceId + '".', httpResponse);
        });
      }
      else {
        var restoredCollectionPromise = restoreCollection(resourceId);
        restoredCollectionPromise.then(function() {
          closeAndReload(Notifications.success, 'The collection "' + resourceId + '" has been created.');
        },
        function(httpResponse) {
          closeAndReload(Notifications.httpError, 'Not able to create the collection "' + resourceId + '".', httpResponse);
        });
      }
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

// -- Security / Collections ---------------------------------------------------

loMod.controller('SecurityCollectionsCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoRealmAppRoles, loRealmAppRoles, uriPolicies, aclPolicies, currentApp, loStorageList) {

  $rootScope.curApp = currentApp;

  $scope.currentCollection = $route.current.params.storageId + '/' + $route.current.params.collectionId;
  $scope.currentCollectionDisplay = $route.current.params.storageId + ' / ' + $route.current.params.collectionId;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure Collection', 'href': '#/applications/' + currentApp.id + '/security/secure-collections/'},
    {'label': $scope.currentCollectionDisplay, 'href': '#/applications/' + currentApp.id + '/security/' + $scope.currentCollection}
  ];

  $scope.storageList = $filter('filter')(loStorageList.members, {'type': 'database'});

  $scope.collectionsList = [];
  angular.forEach($scope.storageList, function(storage) {
    angular.forEach(storage.members, function(collection) {
      $scope.collectionsList.push({id: storage.id + '/' + collection.id, name: storage.id + ' / ' + collection.id});
    });
  });

  $scope.$watch('currentCollection',  function(/*newVal*/) {
    $location.path('applications/' + currentApp.id + '/security/policies/storage/' + $scope.currentCollection);
  });

  var userPath = '/' + currentApp.id + '/' + $scope.currentCollection;
  var superUserPath = userPath + '/*';

  var newUriPolicies;
  if(!uriPolicies) {
    newUriPolicies = createUriPolicies(userPath, superUserPath, LoSecurity);
    uriPolicies = newUriPolicies.config;
  }

  var newAclPolicies;
  if(!aclPolicies) {
    newAclPolicies = createAclPolicies(userPath, LoACL);
    aclPolicies = newAclPolicies.config;
  }

  var colUriPolicies = $filter('filter')(uriPolicies.rules, {'uriPattern': userPath });

  $scope.appRoles = loRealmAppRoles;

  $scope.settings = initializeUriPolicies(colUriPolicies, userPath, superUserPath, $filter);

  $scope.availablePermissions = ['READ','UPDATE','DELETE'];

  $scope.settings.ownerPermissions = initializeAutoAddedOwnerPermissions(aclPolicies, userPath);

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
      else if (uriPolicies.rules[u].uriPattern === superUserPath) {
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
    if(!hasSuperRead) { uriPolicies.rules.push(newUriPolicyRule(superUserPath, ['READ'], $scope.settings.readAllRoles)); }
    if(!hasSuperUpdate) { uriPolicies.rules.push(newUriPolicyRule(superUserPath, ['UPDATE'], $scope.settings.updateAllRoles)); }
    if(!hasSuperDelete) { uriPolicies.rules.push(newUriPolicyRule(superUserPath, ['DELETE'], $scope.settings.deleteAllRoles)); }

    var uriSuccess = function(value/*, responseHeaders*/) {
      settingsBackup = angular.copy($scope.settings);
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
      settingsBackup.ownerPermissions = $scope.settings.ownerPermissions;
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
    modalAddRole($modal, $scope, LoRealmAppRoles, currentApp);
  };

  $scope.toggleItem = function(item, array) {
    toggleItem(item, array);
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

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);
});

// -- Security / Storage -------------------------------------------------------

loMod.controller('SecurityStorageCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoRealmAppRoles, currentApp, loStorageList, loRealmAppRoles, uriPolicies, aclPolicies) {

  $rootScope.curApp = currentApp;

  $scope.currentStorage = $route.current.params.storageId;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure Storage', 'href': '#/applications/' + currentApp.id + '/security/secure-storage/'},
    {'label': $scope.currentStorage, 'href': '#/applications/' + currentApp.id + '/security/policies/storage/' + $scope.currentStorage}
  ];

  // watch for currentStorage dropdown changes, so we switch the route accordingly
  $scope.$watch('currentStorage',  function(/*newVal*/) {
    $location.path('applications/' + currentApp.id + '/security/policies/storage/' + $scope.currentStorage);
  });

  // list of storage resources for this application
  $scope.storageList = $filter('filter')(loStorageList.members, {'type': 'database'});

  // list of roles fetched from Keycloak
  $scope.appRoles = loRealmAppRoles;
  // list of available permissions for the creator
  $scope.availablePermissions = ['READ','UPDATE','DELETE'];

  // define security uris for user and superuser
  var userPath = '/' + currentApp.id + '/' + $scope.currentStorage;
  var superUserPath = userPath + '/*';

  // initialize URI Policies in case they are not present
  var newUriPolicies;
  if(!uriPolicies) {
    newUriPolicies = createUriPolicies(userPath, superUserPath, LoSecurity);
    uriPolicies = newUriPolicies.config;
  }

  var newAclPolicies;
  if(!aclPolicies) {
    newAclPolicies = createAclPolicies(userPath, LoACL);
    aclPolicies = newAclPolicies.config;
  }

  // initialize settings object we will work with for storing information, with resource uri policies
  var resUriPolicies = $filter('filter')(uriPolicies.rules, {'uriPattern': userPath });
  $scope.settings = initializeUriPolicies(resUriPolicies, userPath, superUserPath, $filter);

  // initialize auto added owner permissions (resource creator can...)
  $scope.settings.ownerPermissions = initializeAutoAddedOwnerPermissions(aclPolicies, userPath);

  // settings backup to compare for changes and rollback
  var settingsBackup = angular.copy($scope.settings);

  $scope.changed = false;

  // add/remove item from array
  $scope.toggleItem = function(item, array) {
    toggleItem(item, array);
  };

  // keep a watch on setting to be aware of changes
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
    consolidateUriPolicies(uriPolicies, userPath, superUserPath, $scope.settings);

    var uriSuccess = function(value/*, responseHeaders*/) {
      settingsBackup = angular.copy($scope.settings);
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
      settingsBackup.ownerPermissions = $scope.settings.ownerPermissions;
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
    modalAddRole($modal, $scope, LoRealmAppRoles, currentApp);
  };

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);
});

// -- Security / Push ----------------------------------------------------------

loMod.controller('SecurityPushCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoRealmAppRoles, currentApp, loAppExpanded, loRealmAppRoles, uriPolicies, aclPolicies) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure Push', 'href': '#/applications/' + currentApp.id + '/security/secure-push'}
  ];

  // list of roles fetched from Keycloak
  $scope.appRoles = loRealmAppRoles;
  // list of available permissions for the creator
  $scope.availablePermissions = ['READ','UPDATE','DELETE'];

  // define security uris for user
  var userPath = '/' + currentApp.id + '/push';

  // initialize URI Policies in case they are not present
  var newUriPolicies;
  if(!uriPolicies) {
    newUriPolicies = createUriPolicies(userPath, undefined, LoSecurity);
    uriPolicies = newUriPolicies.config;
  }

  var newAclPolicies;
  if(!aclPolicies) {
    newAclPolicies = createAclPolicies(userPath, LoACL);
    aclPolicies = newAclPolicies.config;
  }

  // initialize settings object we will work with for storing information, with resource uri policies
  var resUriPolicies = $filter('filter')(uriPolicies.rules, {'uriPattern': userPath });
  $scope.settings = initializeUriPolicies(resUriPolicies, userPath, null, $filter);

  // initialize auto added owner permissions (resource creator can...)
  $scope.settings.ownerPermissions = initializeAutoAddedOwnerPermissions(aclPolicies, userPath);

  // settings backup to compare for changes and rollback
  var settingsBackup = angular.copy($scope.settings);

  $scope.changed = false;

  // add/remove item from array
  $scope.toggleItem = function(item, array) {
    toggleItem(item, array);
  };

  // keep a watch on setting to be aware of changes
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
    consolidateUriPolicies(uriPolicies, userPath, null, $scope.settings);

    var uriSuccess = function(value/*, responseHeaders*/) {
      settingsBackup = angular.copy($scope.settings);
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
      settingsBackup.ownerPermissions = $scope.settings.ownerPermissions;
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
    modalAddRole($modal, $scope, LoRealmAppRoles, currentApp);
  };

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);
});

// -- Security / Clients -------------------------------------------------------

loMod.controller('SecurityClientsCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoRealmAppRoles, currentApp, loAppExpanded, loRealmAppRoles, uriPolicies, aclPolicies) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure Clients', 'href': '#/applications/' + currentApp.id + '/security/secure-clients'}
  ];

  // list of roles fetched from Keycloak
  $scope.appRoles = loRealmAppRoles;
  // list of available permissions for the creator
  $scope.availablePermissions = ['READ','UPDATE','DELETE'];

  // define security uris for user and superuser
  var userPath = '/' + currentApp.id + '/clients';
  var superUserPath = userPath + '/*';

  // initialize URI Policies in case they are not present
  var newUriPolicies;
  if(!uriPolicies) {
    newUriPolicies = createUriPolicies(userPath, superUserPath, LoSecurity);
    uriPolicies = newUriPolicies.config;
  }

  var newAclPolicies;
  if(!aclPolicies) {
    newAclPolicies = createAclPolicies(userPath, LoACL);
    aclPolicies = newAclPolicies.config;
  }

  // initialize settings object we will work with for storing information, with resource uri policies
  var resUriPolicies = $filter('filter')(uriPolicies.rules, {'uriPattern': userPath });
  $scope.settings = initializeUriPolicies(resUriPolicies, userPath, superUserPath, $filter);

  // initialize auto added owner permissions (resource creator can...)
  $scope.settings.ownerPermissions = initializeAutoAddedOwnerPermissions(aclPolicies, userPath);

  // settings backup to compare for changes and rollback
  var settingsBackup = angular.copy($scope.settings);

  $scope.changed = false;

  // add/remove item from array
  $scope.toggleItem = function(item, array) {
    toggleItem(item, array);
  };

  // keep a watch on setting to be aware of changes
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
    consolidateUriPolicies(uriPolicies, userPath, superUserPath, $scope.settings);

    var uriSuccess = function(value/*, responseHeaders*/) {
      settingsBackup = angular.copy($scope.settings);
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
      settingsBackup.ownerPermissions = $scope.settings.ownerPermissions;
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
    modalAddRole($modal, $scope, LoRealmAppRoles, currentApp);
  };

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);
});

// -- Security / Business Logic ------------------------------------------------

loMod.controller('SecurityBusinessLogicCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, LoRealmAppRoles, currentApp, loRealmAppRoles, uriPolicies, aclPolicies) {

  $scope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure Business Logic', 'href': '#/applications/' + currentApp.id + '/security/secure-logic'}
  ];

  // list of roles fetched from Keycloak
  $scope.appRoles = loRealmAppRoles;
  // list of available permissions for the creator
  $scope.availablePermissions = ['READ','UPDATE','DELETE'];

  // define security uris for user and superuser
  var userPath = '/admin/applications/' + currentApp.id + '/resources/scripts';
  var superUserPath = userPath + '/*';

  // initialize URI Policies in case they are not present
  var newUriPolicies;
  if(!uriPolicies) {
    newUriPolicies = createUriPolicies(userPath, superUserPath, LoSecurity);
    uriPolicies = newUriPolicies.config;
  }

  var newAclPolicies;
  if(!aclPolicies) {
    newAclPolicies = createAclPolicies(userPath, LoACL);
    aclPolicies = newAclPolicies.config;
  }

  // initialize settings object we will work with for storing information, with resource uri policies
  var resUriPolicies = $filter('filter')(uriPolicies.rules, {'uriPattern': userPath });
  $scope.settings = initializeUriPolicies(resUriPolicies, userPath, superUserPath, $filter);

  // initialize auto added owner permissions (resource creator can...)
  $scope.settings.ownerPermissions = initializeAutoAddedOwnerPermissions(aclPolicies, userPath);

  // settings backup to compare for changes and rollback
  var settingsBackup = angular.copy($scope.settings);

  $scope.changed = false;

  // add/remove item from array
  $scope.toggleItem = function(item, array) {
    toggleItem(item, array);
  };

  // keep a watch on setting to be aware of changes
  var hasWatch = false;
  var doWatch = function() {
    if (!hasWatch) {
      $scope.$watch('settings', function() {
        $scope.changed = !angular.equals($scope.settings, settingsBackup);
      }, true);
      hasWatch = true;
    }
    else {
      $scope.changed = !angular.equals($scope.settings, settingsBackup);
    }
  };
  doWatch();

  $scope.clear = function() {
    $scope.settings = angular.copy(settingsBackup);
  };

  $scope.save = function() {
    consolidateUriPolicies(uriPolicies, userPath, superUserPath, $scope.settings);

    var uriSuccess = function(value/*, responseHeaders*/) {
      settingsBackup = angular.copy($scope.settings);
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
      settingsBackup.ownerPermissions = $scope.settings.ownerPermissions;
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
    modalAddRole($modal, $scope, LoRealmAppRoles, currentApp);
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

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);
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

  // Delete User
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

loMod.controller('SecurityUsersAddCtrl', function($scope, $rootScope, $log, $route, $location, $http, $modal, currentApp, userProfile, userRoles, appRoles, LoRealmUsers, Notifications, LiveOak) {

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

    var roleMappingsUrl = LiveOak.getAuthServerUrl() + '/admin/realms/liveoak-apps/users/' + $scope.userModel.profile.username + '/role-mappings/applications/' + currentApp.name;

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

  // Reset Password
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

var toggleItem = function(item, array) {
  var found = $.inArray(item, array);
  if (found >= 0) {
    array.splice(found, 1);
  }
  else {
    array.push(item);
  }
};

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

var initializeAutoAddedOwnerPermissions = function(aclPolicies, path) {
  for (var i = 0; i < aclPolicies.autoRules.length; i++) {
    if(aclPolicies.autoRules[i].resourcePath === path) {
      return aclPolicies.autoRules[i].autoAddedOwnerPermissions;
    }
  }

  return [];
};

var createUriPolicies = function(userPath, superUserPath, LoSecurity) {
  var newUriPolicies = new LoSecurity();
  newUriPolicies.type = 'uri-policy';
  newUriPolicies.config = {
    id : 'uri-policy',
    rules : []
  };

  if (userPath) {
    newUriPolicies.config.rules.push(newUriPolicyRule(userPath, ['READ'], []));
    newUriPolicies.config.rules.push(newUriPolicyRule(userPath, ['CREATE'], []));
  }
  if (superUserPath) {
    newUriPolicies.config.rules.push(newUriPolicyRule(superUserPath, ['READ'], []));
    newUriPolicies.config.rules.push(newUriPolicyRule(superUserPath, ['UPDATE'], []));
    newUriPolicies.config.rules.push(newUriPolicyRule(superUserPath, ['DELETE'], []));
  }

  return newUriPolicies;
};

var initializeUriPolicies = function(uriPolicies, userPath, superUserPath, $filter) {
  var settings = {};
  if (uriPolicies.length > 0) {
    if (userPath) {
      var _access = ($filter('filter')(uriPolicies, {'uriPattern': userPath, requestTypes: 'READ' }, true));
      var _create = ($filter('filter')(uriPolicies, {'uriPattern': userPath, requestTypes: 'CREATE' }, true));
      settings.accessRoles = _access.length > 0 ? _access[0].allowedRoles : [];
      settings.createEntryRoles = _create.length > 0 ? _create[0].allowedRoles : [];
    }
    if (superUserPath) {
      var _readAll = ($filter('filter')(uriPolicies, {'uriPattern': superUserPath, requestTypes: 'READ' }, true));
      var _updateAll = ($filter('filter')(uriPolicies, {'uriPattern': superUserPath, requestTypes: 'UPDATE' }, true));
      var _deleteAll = ($filter('filter')(uriPolicies, {'uriPattern': superUserPath, requestTypes: 'DELETE' }, true));
      settings.readAllRoles = _readAll.length > 0 ? _readAll[0].allowedRoles : [];
      settings.updateAllRoles = _updateAll.length > 0 ? _updateAll[0].allowedRoles : [];
      settings.deleteAllRoles = _deleteAll.length > 0 ? _deleteAll[0].allowedRoles : [];
    }
  }
  else {
    settings.accessRoles      = [];
    settings.createEntryRoles = [];
    settings.readAllRoles     = [];
    settings.updateAllRoles   = [];
    settings.deleteAllRoles   = [];
  }
  return settings;
};

var consolidateUriPolicies = function(uriPolicies, userPath, superUserPath, settings) {
  var hasUserRead, hasUserCreate, hasSuperRead, hasSuperUpdate, hasSuperDelete = false;
  for (var u = 0; u < uriPolicies.rules.length; u++) {
    if (userPath && uriPolicies.rules[u].uriPattern === userPath) {
      if (angular.equals(uriPolicies.rules[u].requestTypes, ['READ'])) {
        uriPolicies.rules[u].allowedRoles = settings.accessRoles;
        hasUserRead = true;
      }
      else if (angular.equals(uriPolicies.rules[u].requestTypes, ['CREATE'])) {
        uriPolicies.rules[u].allowedRoles = settings.createEntryRoles;
        hasUserCreate = true;
      }
    }
    else if (superUserPath && uriPolicies.rules[u].uriPattern === superUserPath) {
      if (angular.equals(uriPolicies.rules[u].requestTypes, ['READ'])) {
        uriPolicies.rules[u].allowedRoles = settings.readAllRoles;
        hasSuperRead = true;
      }
      else if (angular.equals(uriPolicies.rules[u].requestTypes, ['UPDATE'])) {
        uriPolicies.rules[u].allowedRoles = settings.updateAllRoles;
        hasSuperUpdate = true;
      }
      else if (angular.equals(uriPolicies.rules[u].requestTypes, ['DELETE'])) {
        uriPolicies.rules[u].allowedRoles = settings.deleteAllRoles;
        hasSuperDelete = true;
      }
    }
  }

  if(userPath && !hasUserRead) { uriPolicies.rules.push(newUriPolicyRule(userPath, ['READ'], settings.accessRoles)); }
  if(userPath && !hasUserCreate) { uriPolicies.rules.push(newUriPolicyRule(userPath, ['CREATE'], settings.createEntryRoles)); }
  if(superUserPath && !hasSuperRead) { uriPolicies.rules.push(newUriPolicyRule(superUserPath, ['READ'], settings.readAllRoles)); }
  if(superUserPath && !hasSuperUpdate) { uriPolicies.rules.push(newUriPolicyRule(superUserPath, ['UPDATE'], settings.updateAllRoles)); }
  if(superUserPath && !hasSuperDelete) { uriPolicies.rules.push(newUriPolicyRule(superUserPath, ['DELETE'], settings.deleteAllRoles)); }
};

var createAclPolicies = function(userPath, LoACL) {
  var newAclPolicies = new LoACL();
  newAclPolicies.type = 'acl-policy';
  newAclPolicies.config = {
    id: 'acl-policy',
    autoRules: [{
      resourcePath: userPath,
      autoAddedOwnerPermissions: []
    }]
  };

  return newAclPolicies;
};

loMod.controller('NoSecurityCtrl', function($scope, $rootScope, $location, $log, $filter, $modal, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security Policies', 'href': '#/applications/' + currentApp.id + '/security'}
  ];

});

var modalAddRole = function($modal, $scope, LoRealmAppRoles, currentApp) {
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
