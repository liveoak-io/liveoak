'use strict';

var loMod = angular.module('loApp');

loMod.controller('PushCtrl', function($rootScope, $log, currentApp) {

  $log.debug('PushCtrl');

  $rootScope.curApp = currentApp;

});