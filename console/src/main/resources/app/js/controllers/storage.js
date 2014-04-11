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

  if (loStorageList._members) {
    for (var i = 0; i < loStorageList._members.length; i++) {

      var resource = loStorageList._members[i];

      if (resource.hasOwnProperty('db')) {
        $scope.resources.push({
          provider: 'Mongo DB',
          path: resource.id,
          host: resource.servers[0].host,
          port: resource.servers[0].port,
          database: resource.db
        });
      }
    }
  }

});

loMod.controller('StorageCollectionCtrl', function($scope, $rootScope, $log, $route, currentApp, $modal, Notifications,
                                                   currentCollectionList, LoCollection, $routeParams, LoCollectionItem,
                                                   LiveOak) {

  $log.debug('StorageCollectionCtrl');

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Storage',       'href':'#/applications/' + currentApp.id + '/storage'}
  ];

  $scope.collectionList = currentCollectionList._members;
  $scope.collectionData = {};
  $scope.collectionDataBackup = {};
  $scope.live = {};

  $scope.columnsHidden = [];
  $scope.newRow = {};
  $scope.rowsToDelete = [];

  $scope.isColHidden = false;

  // TODO - seems to be redundant because with each column change, there's a data change.
  $scope.isColumnChange = false;
  $scope.isClearAll = false;
  $scope.isDataChange = false;

  // Filter for hidden columns
  $scope.notHidden = function(item) {
    if ($scope.columnsHidden.indexOf(item) === -1){
      return true;
    }
    return false;
  };

  // Filter for the id column
  $scope.notId = function(item) {
    if (item !== 'id'){
      return true;
    }
    return false;
  };

  // Search query string
  $scope.predicate = 'id';

  $scope.setFilter = function(predicate, reverse){
    $scope.predicate = predicate;
    $scope.reverse = reverse;
  };

  var loadCollectionData = function(colId, loadColumns) {

    $log.debug('Loading collection data.');

    var dataPromise = LoCollectionItem.getList({appId: currentApp.id, storageId: $routeParams.storageId, collectionId: colId}).$promise;

    dataPromise.then(function (data) {
      if (loadColumns) {

        $scope.columns = ['id'];

        if (data._members && data._members[0]) {
          for (var c in data._members[0]) {
            if (c !== 'self' && c !== 'id') {
              $scope.columns.push(c);
            }
          }
        }
      }
      $scope.columnsBackup = angular.copy($scope.columns);
      $scope.collectionData = data;
      $scope.collectionDataBackup = angular.copy(data);
      $scope.isDataChange = false;
    } );
  };

  // If nothing is selected, select the 1st item from the list
  var selectFirst = function() {
    if ($scope.collectionList) {
      $scope.collectionId = $scope.collectionList[0].id;
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

  var resetEnv = function(){
    $scope.columnsHidden = [];
    $scope.isColHidden = false;
    $scope.isColumnChange = false;
    $scope.isClearAll = false;
    $scope.isDataChange = false;
    $scope.rowsToDelete = [];
  };

  $scope.$watch('collectionId', function(){
      $log.debug('Collection ID changed.');
      if ( $scope.collectionId ) {
        loadCollectionData($scope.collectionId, true);

        LiveOak.connect( function() {
          var urlSubscribe = '/' + currentApp.id + '/' + $routeParams.storageId + '/' + $scope.collectionId + '/*';
          LiveOak.subscribe(urlSubscribe, function (data) {
            //Notifications.warn('Data were changed outside console: ' + JSON.stringify(data));

            $log.debug('UPS read: ' + JSON.stringify(data));

            loadCollectionData($scope.collectionId, true);
            $scope.live = data;
          });
        });

        resetEnv();
      }
    }
  );

  $scope.$watch('collectionData._members', function(){
      $log.debug('Collection data changed.');
      if(!angular.equals($scope.collectionData._members, $scope.collectionDataBackup._members)){
        $log.debug('Collection data differs from original.');
        $scope.isDataChange = true;
      }
    }, true
  );

  $scope.$watch('columns', function(){
      $log.debug('Columns number changed.');
      if(!angular.equals($scope.collectionData._members, $scope.collectionDataBackup._members)){
        $log.debug('Column number differs from original.');
        $scope.isColumnChange = true;
      }
    }, true
  );

  var ModalInstanceCtrl = function ($scope, $modalInstance) {

    $scope.close = function () {
      $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

  $scope.modalRowAdd = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/row-add.html',
      controller: ModalInstanceCtrl,
      scope: $scope
    });
  };

  $scope.modalColumnAdd = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/column-add.html',
      controller: ModalInstanceCtrl,
      scope: $scope
    });
  };

  $scope.modalCollectionAdd = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/collection-add.html',
      controller: ModalInstanceCtrl,
      scope: $scope
    });
  };

  $scope.modalCollectionDelete = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/collection-delete.html',
      controller: ModalInstanceCtrl,
      scope: $scope
    });
  };

  $scope.modalDataDeleteAll = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/data-clear.html',
      controller: ModalInstanceCtrl,
      scope: $scope
    });
  };

  $scope.collectionCreate = function(collectionName) {
    var newCollectionPromise = LoCollection.create({appId: currentApp.id, storageId: $routeParams.storageId},
      {id : collectionName}).$promise;

    newCollectionPromise.then(function(){
      $scope.collectionId = collectionName;
      loadCollectionList();
    });
  };

  $scope.collectionDelete = function() {
    $log.debug('Deleting collection: ' + $scope.collectionId);
    var deletePromise = LoCollection.delete({appId: currentApp.id, storageId: $routeParams.storageId,
      collectionId: $scope.collectionId}).$promise;
    // There's a watcher on $scope.collectionId, list will be updated from there
    deletePromise.then(function(){
      loadCollectionList(selectFirst);
    });
  };

  $scope.rowAdd = function(){
    $log.debug('Creating new row ' + $scope.newRow);

    if (!$scope.collectionData._members){
      $scope.collectionData._members = [];
    }

    $scope.collectionData._members.push($scope.newRow);
    $scope.newRow = {};
  };

  $scope.rowRemove = function(id) {
    $log.debug('Deleting row ' + id);

    var rowToRemove = -1;

    for (var k in $scope.collectionData._members){
      var item = $scope.collectionData._members[k];
      if (item.id === id) {
        rowToRemove = k;
        break;
      }
    }

    if (rowToRemove > -1){
      $scope.collectionData._members.splice(rowToRemove, 1);
    } else {
      $log.error('Unable to find item to remove. The ID \"'+rowToRemove+'\" does not exist.');
    }

    $scope.rowsToDelete.push(id);
    $scope.isDataChange = true;
  };

  $scope.columnAdd = function(columnName){
    $log.debug('Adding column ' + columnName );
    if ($scope.columnNew !== '') {
      $scope.columns.push(columnName);
    }

    for (var k in $scope.collectionData._members){
      var item = $scope.collectionData._members[k];
      item[columnName] = '';
    }

    $scope.isDataChange = true;
    $scope.isColumnChange = true;
  };

  $scope.columnRemove = function(columnName){
    $log.debug('Removing column ' + columnName );
    var index = $scope.columns.indexOf(columnName);

    if (index > -1) {
      $scope.columns.splice(index, 1);

      for (var k in $scope.collectionData._members) {
        var item = $scope.collectionData._members[k];
        if (item.hasOwnProperty(columnName)) {
          item[columnName] = null;
        }
      }

      $scope.isDataChange = true;
      $scope.isColumnChange = true;
    }
  };

  $scope.columnClear = function(col){
    $log.debug('Clearing column ' + col );

    for (var i in $scope.collectionData._members){
      var row = $scope.collectionData._members[i];
      row[col] = '';
    }
  };

  $scope.columnHide = function(col) {
    $scope.columnsHidden.push(col);
    $scope.isColHidden = true;
  };

  $scope.collectionClear = function(){
    $log.debug('Clearing collection ' + $scope.collectionId );

    $scope.columns = ['id'];
    $scope.collectionData._members = [];
    $scope.columnsHidden = [];
    $scope.isClearAll = true;
  };

  $scope.save = function(){
    $log.debug('Saving collection.');

    // If Clear All was clicked, no need to delete specific rows, everything will be deleted.
    if ($scope.isClearAll) {
      for (var j in $scope.collectionDataBackup._members){
        var itemToClear = $scope.collectionDataBackup._members[j];

        LoCollectionItem.delete({appId: currentApp.id, storageId: $routeParams.storageId,
          collectionId: $scope.collectionId, itemId: itemToClear.id});
      }
    } else {
    // Else delete specific rows only
      for (var i in $scope.rowsToDelete) {
        var itemToDelete = $scope.rowsToDelete[i];
        if (itemToDelete) {
          $log.debug('Going to delete: ' + JSON.stringify(itemToDelete));
          LoCollectionItem.delete({appId: currentApp.id, storageId: $routeParams.storageId,
            collectionId: $scope.collectionId, itemId: itemToDelete});
        }
      }
    }

    var findById = function(myId, myCollection){
      for (var l in myCollection){
        if (myCollection[l].id === myId){
          return angular.copy(myCollection[l]);
        }
      }

      return undefined;
    };

    // Then update all changed data in the table
    for (var k in $scope.collectionData._members){
      var item = $scope.collectionData._members[k];

      var itemToSave = angular.copy(item);
      delete itemToSave.self;

      // Update existing items
      var itemFromBackup = findById(itemToSave.id, $scope.collectionDataBackup._members);
      if (itemFromBackup && itemFromBackup.self) {
        delete itemFromBackup.self;
      }

      if (itemToSave.id) {
        $log.debug('Checking for update: ' + JSON.stringify(itemToSave));
        if (itemFromBackup && !angular.equals(itemToSave, itemFromBackup)) {
          $log.debug('Updating: ' + JSON.stringify(itemToSave));
          LoCollectionItem.update({appId: currentApp.id, storageId: $routeParams.storageId,
            collectionId: $scope.collectionId, itemId: itemToSave.id}, itemToSave);
        } else {
          $log.debug('Not updating:        ' + JSON.stringify(itemToSave));
        }
      }
      // Create new items
      else {
        $log.debug('Creating: ' + itemToSave);
        LoCollectionItem.create({appId: currentApp.id, storageId: $routeParams.storageId,
          collectionId: $scope.collectionId}, itemToSave);
      }
    }

    Notifications.info('Collection data saved.');

    resetEnv();
    loadCollectionData($scope.collectionId, true);
  };

  $scope.reset = function(){
    $log.debug('Resetting the page.');

    resetEnv();

    $scope.columns = angular.copy($scope.columnsBackup);
    $scope.collectionData = angular.copy($scope.collectionDataBackup);
    //loadCollectionData($scope.collectionId, true);
  };

});