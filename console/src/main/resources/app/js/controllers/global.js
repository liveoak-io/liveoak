'use strict';

var loMod = angular.module('loApp.controllers.global', []);

loMod.controller('GlobalCtrl', function($log, $rootScope, $scope, $location, $route, $modal, LiveOak) {

  $log.debug('GlobalCtrl' + LiveOak);

  $rootScope.$on('$routeChangeError',
    /* Following function can get use of 'event, current, previous, rejection' arguments to specify the error */
    function () {
      $log.error('failed to change routes');
      $location.path('/error');
    });

  // to avoid initial flashing of sidebar on non-sidebar pages
  $rootScope.hideSidebar = true;

  // originalPath is used to calculate the sidebar active element
  $rootScope.$on('$locationChangeSuccess', function() {
    $rootScope.oPath = $route.current.$$route.originalPath;
  });

  // so that it shows back when switching from non-sidebar to sidebar pages
  $rootScope.$on('$routeChangeSuccess', function() {
    delete $rootScope.hideSidebar;
  });

  // show prevent lose change
  $rootScope.preventLoseChanges = function(theScope/*, saveFun, discardFun*/) {
    //$scope.saveFun = saveFun;
    //$scope.discardFun = discardFun;
    theScope.$on('$locationChangeStart', function(event, next) {
      theScope.next = next.substr(next.indexOf('#')+1, next.length);
      if (theScope.changed) {
        event.preventDefault();
        $modal.open({
          templateUrl: '/admin/console/templates/modal/unsaved-changes.html',
          controller: UnsavedChangesCtrl,
          scope: theScope
        });
      }
    });
  };

  var UnsavedChangesCtrl = function ($scope, $modalInstance) {
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    $scope.discard = function() {
      $modalInstance.close();
      $scope.$parent.changed = false;
      $location.path($scope.$parent.next);
    };

    // Not being used yet. Requires changes:
    // - Need promise on save method for knowing when completed;
    // - Need to handle save method redirect;
    // - Possibly more...
    /*
    $scope.save = function() {
      $modalInstance.close();
      $scope.$parent.saveFun();
      isStillChanged(0, 10);
    };

    var isStillChanged = function(count, max) {
      if ($scope.$parent.changed) {
        if (count <= max) {
          $timeout(function() { isStillChanged(count++, max) }, 250);
        }
        else {
          // do what? notification?
        }
      }
      else {
        $location.path($scope.$parent.next);
      }
    }
    */
  };

  /* jshint ignore:start */
  $scope.auth = LiveOak.auth;
  $scope.username = LiveOak.auth.idToken && LiveOak.auth.idToken.preferred_username;
  $scope.isAdmin = LiveOak.auth.hasResourceRole('admin');
  $scope.authenticated = LiveOak.auth.authenticated;
  /* jshint ignore:end */

  var reIPv4String = '(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)';
  var reIPv6String = '\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*';
  var rePortString = '(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3})';
  var reHostString = '[\\w\\d]([\\w\\d-]*[\\w\\d])*(\\.[\\w\\d]([\\w\\d-]*[\\w\\d])*)*';

  $rootScope.reIPv4 = new RegExp('^' + reIPv4String + '$');
  $rootScope.reIPv6 = new RegExp('^' + reIPv6String + '$');
  $rootScope.rePort = new RegExp('^' + rePortString + '$');
  $rootScope.reHost = new RegExp('^' + reHostString + '$');
  $rootScope.reIPv4IPv6 = new RegExp('^' + reIPv4String + '$|^' + reIPv6String + '$');
  $rootScope.reIPv4Host = new RegExp('^' + reIPv4String + '$|^' + reHostString + '$');
  $rootScope.reIPv6Host = new RegExp('^' + reIPv6String + '$|^' + reHostString + '$');
  $rootScope.reIPv4IPv6Host = new RegExp('^' + reIPv4String + '$|^' + reIPv6String + '$|^' + reHostString + '$');

  $scope.userLabel = function() {
    var role = '';
    if ($scope.auth.hasResourceRole('admin')) {
      role = 'admin';
    } else if ($scope.auth.hasResourceRole('user')) {
      role = 'user';
    }
    return $scope.auth.username + ' (' + role + ')';
  };

});

loMod.controller('NavigationCtrl', function($scope, $rootScope, $filter, loLiveLoader, LoLiveAppList) {

  loLiveLoader(LoLiveAppList.getList, '/admin/applications/').then(function(data){
    $scope.applications = data.live.members;
  });

  $scope.$watch('oPath', function() {
    if ($rootScope.oPath) {
      var a = $rootScope.oPath;
      a = a.substr('/applications/:appId/'.length);
      $scope.current = a.substr(0, a.indexOf('/') > 0 ? a.indexOf('/') : a.length);
    }
  });
});

loMod.controller('ErrorCtrl', function() {
  window.location.href = '/admin/console/error.html';
});