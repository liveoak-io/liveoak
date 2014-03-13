'use strict';

var loMod = angular.module('loApp');

loMod.controller('StorageCtrl', function($scope, $log, LoStorage, loStorage, Notifications, currentApp) {

  $log.debug('StorageCtrl');

  var currentAppId = currentApp;

  $scope.changed = false;

  var create = true;

  if (loStorage.id){
    create = false;
  }

  var storageModelBackup = angular.copy(loStorage);
  $scope.storageModel = angular.copy(loStorage);

  if(loStorage.hasOwnProperty('MongoClientOptions')){
    $scope.storageModel.type = 'mongo';
  }

  $scope.clear = function(){
    $scope.storageModel = angular.copy(storageModelBackup);
    $scope.changed = false;
  };

  $scope.$watch('storageModel', function() {
    if (!angular.equals($scope.storageModel, storageModelBackup)) {
      $scope.changed = true;
    } else {
      $scope.changed = false;
    }
  }, true);

  $scope.save = function(){
    if(!angular.equals($scope.storageModel.credentials[0].password, $scope.passwdConfirm)){
      Notifications.error('Password does not match the password confirmation.');
    } else {
      var data = {
        id: $scope.storageModel.id,
        type: $scope.storageModel.type,
        config: {
          db: $scope.storageModel.db,
          servers: $scope.storageModel.servers,
          credentials: [
            { mechanism:'MONGODB-CR',
              username: $scope.storageModel.credentials[0].username,
              password: $scope.storageModel.credentials[0].password,
              database: $scope.storageModel.db
            }
          ]
        }
      };

      $log.debug(''+data);
      if (create){
        $log.debug('Creating new storage resource: ' + data.id);
        LoStorage.create({appId: currentAppId}, data);
      } else {
        $log.debug('Updating storage resource: ' + data.id);
        LoStorage.update({appId: currentAppId}, data);
      }
      Notifications.success('New storage successfully created.');
      storageModelBackup = angular.copy($scope.storageModel);
      $scope.changed = false;
    }
  };

});

loMod.controller('StorageListCtrl', function($scope, $log, $routeParams, loStorageList) {

  $log.debug('StorageListCtrl');

  // TODO: Resources are stored here, but we use mock data since we don't know how to distinguish storage from other
  // resources right now
  $log.debug(loStorageList._members);

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