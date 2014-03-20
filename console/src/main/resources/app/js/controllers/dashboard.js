'use strict';

var loMod = angular.module('loApp');

loMod.controller('DashboardCtrl', function($scope, $rootScope, $routeParams, LoAppList, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Dashboard',     'href':'#/applications/' + currentApp.id + '/dashboard'}
  ];

  LoAppList.get(function(data){
    $scope.applications = data._members;
  });

  $scope.dataPeriods = [ 'Hour', 'Day', 'Week', 'Month' ];
  $scope.range = 'Hour';

  // FIXME: Fetch from rest. Mock data.
  $scope.data = {
    'LiveOak Admin Console': {
      status: 'Running',
      appKey: 'jieu89-eiW38oskj',
      appSecret: '9iugjW39vnlz90ei9ejiDoi',
      storage: ['storage'],
      push: false,
      metrics: {
        'Hour': {requests: 100, users: 7, notifications: 33, storage: 0.6},
        'Day': {requests: 150, users: 15, notifications: 44, storage: 1.5},
        'Week': {requests: 127, users: 13, notifications: 39, storage: 1.2},
        'Month': {requests: 172, users: 18, notifications: 51, storage: 1.8}
      }
    },
    'chat': {
      status: 'Stopped',
      appKey: 'amvr14-rhG02loak',
      appSecret: 'r6rnt41fiima9vu6icsxp1e',
      storage: ['storage-h2','storage-mysql'],
      push: true,
      metrics: {
        'Hour': {requests: 50, users: 4, notifications: 18, storage: 0.4},
        'Day': {requests: 76, users: 7, notifications: 23, storage: 0.7},
        'Week': {requests: 86, users: 6, notifications: 19, storage: 0.6},
        'Month': {requests: 101, users: 10, notifications: 26, storage: 1.2}
      }
    }
  };
});