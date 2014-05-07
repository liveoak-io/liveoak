'use strict';

var loMod = angular.module('loApp.controllers.security', []);

loMod.controller('SecurityCtrl', function($scope, $rootScope, $location, $log, currentApp) {
  $rootScope.curApp = currentApp;

});