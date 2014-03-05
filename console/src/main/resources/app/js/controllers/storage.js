'use strict';

var loMod = angular.module('loApp');

loMod.controller('StorageCtrl', function($scope, $log, loStorage, Notifications) {

  $log.debug('StorageCtrl');

  $scope.storageModel = loStorage;
  var storageModelCopy = angular.copy(loStorage);

  $scope.clear = function(){
    $scope.storageModel = angular.copy(storageModelCopy);
  };

  $scope.save = function(){
    if(!angular.equals($scope.storageModel.passwd, $scope.passwdConfirm)){
      Notifications.error('Password does not match the password confirmation.');
    } else {
      Notifications.success('New storage successfully created.');
    }
  };

});

loMod.controller('StorageListCtrl', function($scope, $log, $routeParams) {

  $log.debug('StorageListCtrl');

  $scope.curApp.name = $routeParams.appId;

  $scope.applications =
  {
    'My App': { 'storage': [{
      'provider': 'Mongo DB',
      'path': 'storage',
      'host': '10.20.50.100',
      'port': '27017',
      'database': 'my_app'
    }]},
    'Other App': { 'storage': [
      {
        'provider': 'H2',
        'path': 'storage-h2',
        'host': '10.21.51.81',
        'port': '9092',
        'database': 'other_app_cache'
      },
      {
        'provider': 'MySQL',
        'path': 'storage-mysql',
        'host': '10.21.51.82',
        'port': '3306',
        'database': 'other_app'
      }
    ]}
  };

});