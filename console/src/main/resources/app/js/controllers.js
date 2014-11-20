'use strict';

/* Controllers */

// Only defining controllers module, each module is defined at it's own file at app/js/controllers/*.js
var loMod = angular.module('loApp.controllers', [
  'loApp.controllers.application',
  'loApp.controllers.appclient',
  'loApp.controllers.businesslogic',
  'loApp.controllers.storage',
  'loApp.controllers.security',
  'loApp.controllers.global',
  'loApp.controllers.push',
  'loApp.controllers.dashboard'
]);

loMod.controller('DefaultModalCtrl', function($scope, $modalInstance) {
  $scope.close = function () {
    $modalInstance.close();
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});