'use strict';

var loMod = angular.module('loApp.controllers.global', []);

loMod.controller('GlobalCtrl', function($log, $rootScope, $scope, $location, LiveOak, LoApp) {

  $log.debug('GlobalCtrl' + LiveOak);

  $rootScope.$on('$routeChangeError',
    /* Following function can get use of 'event, current, previous, rejection' arguments to specify the error */
    function () {
      $log.error('failed to change routes');
      $location.path('/error');
    });

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

loMod.controller('HomeCtrl', function($log, $rootScope, $scope, $location, $filter, loAppList) {
  var filtered = $filter('filter')(loAppList._members, {'visible': true});
  if (filtered.length === 1) {
    $location.url('/applications/' + filtered[0].id);
  }
  else {
    $location.url('/applications');
  }
});

angular.module('loApp.controllers').controller('AppDropdownCtrl', function($rootScope, $filter, LoApp) {
  LoApp.getList(function(data){
    $rootScope.applications = $filter('filter')(data._members, {'visible': true});
  });
});
