'use strict';

var loMod = angular.module('loApp');

loMod.controller('GlobalCtrl', function($log, $rootScope, $scope, LiveOak) {

  $log.debug('GlobalCtrl' + LiveOak);

  /* jshint ignore:start */
  $scope.auth = LiveOak.auth;
  $scope.username = LiveOak.auth.idToken && LiveOak.auth.idToken.preferred_username;
  $scope.isAdmin = LiveOak.auth.hasResourceRole('admin');
  $scope.authenticated = LiveOak.auth.authenticated;
  /* jshint ignore:end */

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

angular.module('loApp.controllers').controller('AppDropdownCtrl', function() {
  /*
  //TODO Inspect if this code is needed and remove it respectively

  $scope.applications = [];

  $scope.appList2 = LoAppList.get(function(data){
    for (var i in data._members){
      var member = data._members[i];
      $scope.applications.push({id: member.id, name:member.name});
    }
  });

  $scope.curApp = Current;
  */

});