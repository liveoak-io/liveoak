'use strict';

var loMod = angular.module('loApp.controllers.security', []);

loMod.controller('SecurityListCtrl', function($scope, $rootScope, $location, $log, $filter, $modal, Notifications, LoSecurity, LoACL, expAppResources, expApp, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security', 'href': '#/applications/' + currentApp.id + '/security'}
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

loMod.controller('SecurityCtrl', function($scope, $rootScope, $location, $route, $log, $filter, $modal, Notifications, LoSecurity, LoACL, currentCollectionList, loRealmAppRoles, uriPolicies, aclPolicies, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.currentStorage = $route.current.params.storageId;
  $scope.currentCollection = $route.current.params.collectionId;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Security', 'href': '#/applications/' + currentApp.id + '/security'},
    {'label': 'Secure ' + $scope.currentStorage + '/' + $scope.currentCollection, 'href': '#/applications/' + currentApp.id + '/security/...'}
  ];

  $scope.collectionsList = currentCollectionList._members;

  $scope.$watch('currentCollection',  function(/*newVal*/) {
    $location.path('applications/' + currentApp.id + '/security/' + $route.current.params.storageId + '/' + $scope.currentCollection);
  });

  var userPath = '/' + currentApp.id + '/' + $scope.currentStorage + '/' + $scope.currentCollection;
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

    if(!hasUserRead) { uriPolicies.rules.push(newUriPolicyRule(userPath, ['READ'], $scope.settings.accessRoles)) };
    if(!hasUserCreate) { uriPolicies.rules.push(newUriPolicyRule(userPath, ['CREATE'], $scope.settings.createEntryRoles)) };
    if(!hasSuperRead) { uriPolicies.rules.push(newUriPolicyRule(superuserPath, ['READ'], $scope.settings.readAllRoles)) };
    if(!hasSuperUpdate) { uriPolicies.rules.push(newUriPolicyRule(superuserPath, ['UPDATE'], $scope.settings.updateAllRoles)) };
    if(!hasSuperDelete) { uriPolicies.rules.push(newUriPolicyRule(superuserPath, ['DELETE'], $scope.settings.deleteAllRoles)) };

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
      Notifications.error('Unable to update the application URI Policies.', httpResponse);
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
      Notifications.error('Unable to update the application ACL Policies.', httpResponse);
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