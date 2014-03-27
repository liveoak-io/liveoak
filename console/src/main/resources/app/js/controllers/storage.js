'use strict';

var loMod = angular.module('loApp.controllers.storage', []);

loMod.controller('StorageCtrl', function($scope, $rootScope, $location, $log, LoStorage, loStorage, Notifications, currentApp) {

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

  $scope.changed = false;

  $scope.create = true;

  if (loStorage.id){
    $scope.create = false;
    if (loStorage.credentials.length === 0 ){
      loStorage.credentials = [{'mechanism':'MONGODB-CR'}];
    }
    $scope.breadcrumbs.push({'label': loStorage.id, 'href':'#/applications/' + currentApp.id + '/storage/' + loStorage.id});
  }
  else {
    $scope.breadcrumbs.push({'label': 'New Storage', 'href':'#/applications/' + currentApp.id + '/storage/create-storage'});
  }

  var storageModelBackup = angular.copy(loStorage);
  $scope.storageModel = angular.copy(loStorage);

  $scope.clear = function(){
    $scope.storageModel = angular.copy(storageModelBackup);
    $scope.passwdConfirm = '';
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

    var unameSet = ($scope.storageModel.credentials[0].hasOwnProperty('username') && $scope.storageModel.credentials[0].username !== '');
    var paswdSet = ($scope.storageModel.credentials[0].hasOwnProperty('password') && $scope.storageModel.credentials[0].password !== '');

    if ((unameSet && !paswdSet)||(!unameSet && paswdSet)) {

      Notifications.error('Fill in both username and password.');
    } else if (paswdSet && !angular.equals($scope.storageModel.credentials[0].password, $scope.passwdConfirm)) {

      Notifications.error('Password does not match the password confirmation.');
    } else {

      var credentials = [];

      // Send the credentials data only if username or password was set
      if ($scope.storageModel.credentials.length > 0 &&
        ($scope.storageModel.credentials[0].username || $scope.storageModel.credentials[0].password)) {

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

      // Create new storage resource
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
            Notifications.httpError('Failed to create new storage', httpResponse);
          });
      }
      // Update the storage resource
      else {
        $log.debug('Updating storage resource: ' + $scope.storageModel.id);

        if (($scope.storageModel.credentials[0].username === '' && $scope.storageModel.credentials[0].password === '') ||
            (!$scope.storageModel.credentials[0].hasOwnProperty('username') && !$scope.storageModel.credentials[0].hasOwnProperty('password'))) {

          $scope.storageModel.credentials = [];
        } else {
          $scope.storageModel.credentials[0].database = $scope.storageModel.db;
        }

        LoStorage.update({appId: $scope.curApp.id, storageId: storageModelBackup.id}, $scope.storageModel,
        function(){
          // Update success
          Notifications.success('Storage successfully udpated.');
          $location.path('applications/' + currentApp.id + '/storage');
        },
        function(httpResponse){
          // Update failure
          Notifications.httpError('Failed to update the storage', httpResponse);
        });
      }
    }
  };

});

loMod.controller('StorageListCtrl', function($scope, $rootScope, $log, $routeParams, loStorageList, currentApp) {

  $log.debug('StorageListCtrl');

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Storage',       'href':'#/applications/' + currentApp.id + '/storage'}
  ];

  $scope.createdId = $routeParams.created;

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

loMod.controller('StorageCollectionCtrl', function($scope, $rootScope, $log, $route, currentApp,
                                                   currentCollectionList, LoCollection, $routeParams, LoCollectionItem) {

  $log.debug('StorageCollectionCtrl');

  $rootScope.curApp = currentApp;
  $scope.collectionList = currentCollectionList._members;

  $scope.predicate = 'id';

  $scope.setFilter = function(predicate, reverse){
    $scope.predicate = predicate;
    $scope.reverse = reverse;
  };

  var loadCollectionData = function(colId) {
    $scope.columns = [];
    $scope.myData = LoCollectionItem.getList({appId: currentApp.id, storageId: $routeParams.storageId, collectionId: colId});
    $scope.myData.$promise.then(function (data) {
      if (data._members && data._members[0]) {
        for (var c in data._members[0]) {
          if (c !== 'self') {
            $scope.columns.push(c);
          }
        }
      }
    });
  };

  // If nothing is selected, select the 1st item from the list
  var selectFirst = function() {
    if ($scope.collectionList) {

      if (!$scope.collectionId) {
        $scope.collectionId = $scope.collectionList[0].id;
      }
    }
  };

  selectFirst();

  var loadCollectionList = function(callback) {
    var promise = LoCollection.getList({appId: currentApp.id, storageId: $routeParams.storageId});
    promise.$promise.then(function (data) {
      $scope.collectionList = data._members;
      if (callback) {
        callback();
      }
    });
  };

  loadCollectionList();

  $scope.$watch('collectionId', function(){
      if ( $scope.collectionId ) {
        loadCollectionData($scope.collectionId);
      }
    }
  );

  $scope.create = function() {
    LoCollection.create({appId: currentApp.id, storageId: $routeParams.storageId}, {id : $scope.newCollectionId});
    loadCollectionList();
  };

  $scope.remove = function() {
    LoCollection.delete({appId: currentApp.id, storageId: $routeParams.storageId, collectionId: $scope.collectionId});
    // There's a watcher on $scope.collectionId, list will be updated from there
    $scope.collectionId = false;
    loadCollectionList(selectFirst);
  };

  $scope.removeLine = function(id) {
    LoCollectionItem.delete({appId: currentApp.id, storageId: $routeParams.storageId,
      collectionId: $scope.collectionId, itemId: id});

    loadCollectionData($scope.collectionId);
  };

});
