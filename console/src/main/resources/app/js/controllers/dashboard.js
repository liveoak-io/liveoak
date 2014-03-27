'use strict';

var loMod = angular.module('loApp');

loMod.controller('DashboardCtrl', function($scope, $rootScope, $routeParams, currentApp, loStorageList, loPush) {

  $rootScope.curApp = currentApp;

  $scope.storageList = [];

  /* jshint unused: false */
  angular.forEach(loStorageList._members, function(value, key) {
    if (value.hasOwnProperty('db')) {
      this.push(value.id);
    }
  }, $scope.storageList);
  /* jshint unused: true */

  $scope.pushConfig = loPush;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Dashboard',     'href':'#/applications/' + currentApp.id + '/dashboard'}
  ];

  $scope.dataPeriods = [ 'Hour', 'Day', 'Week', 'Month' ];
  $scope.range = 'Hour';

  // FIXME: Fetch from rest. Mock data.
  $scope.data = {
    'LiveOak Admin Console': {
      metrics: {
        'Hour': {requests: 100, users: 7, notifications: 33, storage: 0.6},
        'Day': {requests: 150, users: 15, notifications: 44, storage: 1.5},
        'Week': {requests: 127, users: 13, notifications: 39, storage: 1.2},
        'Month': {requests: 172, users: 18, notifications: 51, storage: 1.8}
      }
    }
  };
});