'use strict';

var loMod = angular.module('loApp');

loMod.controller('StorageCtrl', function($scope, $rootScope, $location, $log, LoStorage, loStorage, LoAppList, Notifications, currentApp) {

  $log.debug('StorageCtrl');

  if(loStorage.hasOwnProperty('MongoClientOptions')){
    loStorage.type = 'mongo';
  }

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Storage',       'href':'#/applications/' + currentApp.id + '/storage'}
  ];

  LoAppList.get(function(data){
    $scope.applications = data._members;
  });

  $scope.changed = false;

  $scope.create = true;

  if (loStorage.id){
    $scope.create = false;
    $scope.breadcrumbs.push({'label': loStorage.id, 'href':'#/applications/' + currentApp.id + '/storage/' + loStorage.id});
  }
  else {
    $scope.breadcrumbs.push({'label': 'New Storage', 'href':'#/applications/' + currentApp.id + '/storage/create-storage'});
  }

  var storageModelBackup = angular.copy(loStorage);
  $scope.storageModel = angular.copy(loStorage);

  $scope.clear = function(){
    $scope.storageModel = angular.copy(storageModelBackup);
    $scope.changed = false;
  };

  $scope.$watch('storageModel', function() {
    if (!angular.equals($scope.storageModel, storageModelBackup)) {
      $log.debug('Scope changed');
      $scope.changed = true;
    } else {
      $scope.changed = false;
    }
  }, true);

  $scope.save = function(){
    if(!angular.equals($scope.storageModel.credentials[0].password, $scope.passwdConfirm)){
      Notifications.error('Password does not match the password confirmation.');
    } else {

      var credentials = [];

      // Send the credentials data only if username or password was set
      if ($scope.storageModel.credentials.length > 0 &&
        ($scope.storageModel.credentials[0].username || $scope.storageModel.credentials[0].password)){

        $log.debug('Credentials were set');
        credentials.push(
          { mechanism:'MONGODB-CR',
            username: $scope.storageModel.credentials[0].username,
            password: $scope.storageModel.credentials[0].password,
            database: $scope.storageModel.db
          }
        );
      }

      var data = {
        id: $scope.storageModel.id,
        type: $scope.storageModel.type,
        config: {
          db: $scope.storageModel.db,
          servers: $scope.storageModel.servers,
          credentials: credentials
        }
      };

      $log.debug(''+data);
      if ($scope.create){
        $log.debug('Creating new storage resource: ' + data.id);
        LoStorage.create({appId: $scope.curApp.id}, data,
          // success
          function(/*value, responseHeaders*/) {
            Notifications.success('New storage successfully created.');
            storageModelBackup = angular.copy($scope.storageModel);
            $scope.changed = false;
            $location.search('created', $scope.storageModel.id).path('applications/' + currentApp.id + '/storage');
          },
          // error
          function(httpResponse) {
            Notifications.error('Failed to create new storage (' + httpResponse.status + (httpResponse.data ? (' ' + httpResponse.data) : '') + ')');
          });
      } else {
        $log.debug('Updating storage resource: ' + data.id);
        LoStorage.update({appId: $scope.curApp.id}, data);
      }
    }
  };

});

loMod.controller('StorageListCtrl', function($scope, $rootScope, $log, $routeParams, LoAppList, loStorageList, currentApp) {

  $log.debug('StorageListCtrl');

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Storage',       'href':'#/applications/' + currentApp.id + '/storage'}
  ];

  $scope.createdId = $routeParams.created;

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

});

loMod.controller('StorageCollectionCtrl', function($scope, $rootScope, $log, currentApp, currentCollectionList, LoCollection, $routeParams) {

  $log.debug('StorageCollectionCtrl');

  $rootScope.curApp = currentApp;
  $scope.collectionList = currentCollectionList._members;
  $scope.collectionId = $scope.collectionList[0].id;
  $log.debug($scope.collectionId);

  $scope.currentCollectionList = currentCollectionList;
  $log.debug(currentCollectionList._members);

  $scope.myData = LoCollection.get({appId: currentApp.id, storageId: $routeParams.storageId, collectionId: 'chat'});

  $scope.columns = [];

  $scope.myData.$promise.then(function(data){
    for (var c in data._members[0]){
      if (c !== 'self'){
        $scope.columns.push(c);
      }
    }
    console.log($scope.columns);
  });

  $scope.storage = {
    item: {
      id: 1234,
      fName: 'First',
      lName: 'last',
      collection: {asd:'asd', gre: 12343}
    }
  };

});
