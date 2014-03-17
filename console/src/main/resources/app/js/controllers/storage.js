'use strict';

var loMod = angular.module('loApp');

loMod.controller('StorageCtrl', function($scope, $log, LoStorage, loStorage, Notifications, Current) {

  $log.debug('StorageCtrl');

  Current.getCurrent().then(function(result){
    $scope.curApp = result;
  });

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
        LoStorage.create({appId: $scope.curApp.id}, data);
      } else {
        $log.debug('Updating storage resource: ' + data.id);
        LoStorage.update({appId: $scope.curApp.id}, data);
      }
      Notifications.success('New storage successfully created.');
      storageModelBackup = angular.copy($scope.storageModel);
      $scope.changed = false;
    }
  };

});

loMod.controller('StorageListCtrl', function($scope, $log, $routeParams, LoAppList, loStorageList, Current, currentApp) {

  $log.debug('StorageListCtrl');

  $scope.curApp=currentApp;
  LoAppList.get(function(data){
    $scope.applications = data._members;
  });

  $scope.loStorageList=loStorageList;

  $scope.resources = [];

  for(var i =0; i < loStorageList._members.length; i++){

    var resource = loStorageList._members[i];


    if (resource.hasOwnProperty('db')){
      $scope.resources.push({
        provider: 'Mongo DB',
        path: resource.id,
        host: resource.servers[0].host,
        port: resource.servers[0].port,
        database: resource.db
      });
    }
  }

  Current.getCurrent().then(function(result){
    $scope.curApp = result;
  });

});