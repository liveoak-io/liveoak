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