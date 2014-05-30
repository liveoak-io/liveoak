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

  $scope.cancelCreate = function() {
    $location.path('applications/' + currentApp.id + '/storage');
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

loMod.controller('StorageListCtrl', function($scope, $rootScope, $log, $routeParams, loStorageList, currentApp,
                                             LoStorage, Notifications, $modal) {

  $log.debug('StorageListCtrl');

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Storage',       'href':'#/applications/' + currentApp.id + '/storage'}
  ];

  $scope.createdId = $routeParams.created;

  $scope.loStorageList=loStorageList;

  $scope.storageId = '';

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

  var ModalInstanceCtrl = function ($scope, $modalInstance) {

    $scope.close = function () {
      $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

  $scope.modalStorageDelete = function(id){
    console.log('opening model '+id);
    $scope.storageId = id;

    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/storage-delete.html',
      controller: ModalInstanceCtrl,
      scope: $scope
    });
  };

  $scope.storageDelete = function(){
    LoStorage.delete({ appId : currentApp.id, storageId : $scope.storageId},
      function(){
        Notifications.success('Storage successfully deleted.');
      },
      function(httpResponse){
        Notifications.httpError('Failed to delete the storage.', httpResponse);
      });
  };
});

loMod.controller('StorageCollectionCtrl', function($scope, $rootScope, $log, $route, currentApp, $modal, Notifications,
                                                   currentCollectionList, LoCollection, $routeParams, LoCollectionItem,
                                                   LiveOak, $location) {

  $log.debug('StorageCollectionCtrl');
  $rootScope.curApp = currentApp;

  $scope.storageId = $routeParams.storageId;

  if ($routeParams.collectionId) {
    $log.debug('$routeParams.collectionId: ' + $routeParams.collectionId);
    $scope.collectionId = $routeParams.collectionId;
  }

  $scope.breadcrumbs = [
    {'label': 'Applications',   'href':'#/applications'},
    {'label': currentApp.name,  'href':'#/applications/' + currentApp.id},
    {'label': 'Storage',        'href':'#/applications/' + currentApp.id + '/storage'},
    {'label': $scope.storageId, 'href':'#/applications/' + currentApp.id + '/storage/' + $routeParams.storageId}

  ];

  if ($scope.collectionId){
    $scope.breadcrumbs.push({'label': $scope.collectionId,    'href':''});
  } else {
    $scope.breadcrumbs.push({'label': 'Collections',    'href':''});
  }

  $scope.collectionList = currentCollectionList._members;
  $scope.collectionData = {};
  $scope.collectionDataBackup = {};
  $scope.live = {};

  $scope.subscriptionId = false;

  $scope.columnsHidden = [];
  $scope.newRow = {};
  $scope.newRows = [];
  $scope.rowsToDelete = [];

  // TODO - seems to be redundant because with each column change, there's a data change.
  $scope.isColumnChange = false;
  $scope.isClearAll = false;
  $scope.isDataChange = false;

  $scope.searchColumns = [];
  $scope.searchConditions = [{type:'EQUALS', text:''}];
  $scope.showAdvanced = false;

  $scope.searchQuery = '';
  $scope.filterColumns = [];
  $scope.filterConditions = [];

  // Search query string
  $scope.predicate = 'id';

  if (!$scope.collectionId) {
    selectFirst();
  }

  // Load data for selected collection
  if ( currentCollectionList ) {

    loadCollectionData($scope.collectionId, true);

    LiveOak.connect(function () {
      if ($scope.subscriptionId){
        $log.debug('Removing subscription \"' + $scope.subscriptionId + '\"');
        LiveOak.unsubscribe($scope.subscriptionId);
        $scope.subscriptionId = false;
      }

      if (!$scope.subscriptionId) {
        var urlSubscribe = '/' + currentApp.id + '/' + $routeParams.storageId + '/' + $scope.collectionId + '/*';
        $scope.subscriptionId = LiveOak.subscribe(urlSubscribe, function (data) {
          //Notifications.warn('Data were changed outside console: ' + JSON.stringify(data));
          $log.debug('UPS read: ' + JSON.stringify(data));
          loadCollectionData($scope.collectionId, true);
          $scope.live = data;
        });

        $log.debug('Subscribe id is: ' + $scope.subscriptionId);
      }
    });
  }

  // Filter for hidden columns
  $scope.notHidden = function(item) {
    if ($scope.columnsHidden.indexOf(item) === -1){
      return true;
    }
    return false;
  };

  $scope.isNextHidden = function(currentColumn, inverse) {
    var currentColumnIndex = $scope.columns.indexOf(currentColumn);

    // If current column doesn't exist the next column cannot be hidden.
    if (currentColumnIndex === -1){
      return false;
    }

    // If the current column is the last column, there is no next column to be hidden.
    if (currentColumnIndex === $scope.columns.length) {
      return false;
    }

    var nextColumnName;
    if(inverse) {
      nextColumnName = $scope.columns[currentColumnIndex - 1];
    } else {
      nextColumnName = $scope.columns[currentColumnIndex + 1];
    }

    if ($scope.columnsHidden.indexOf(nextColumnName) > -1){
      return true;
    }

    return false;
  };

  $scope.unhideNext = function(currentColumn, inverse) {
    var currentColumnIndex = $scope.columns.indexOf(currentColumn);

    // If current column doesn't exist the next column cannot be hidden.
    if (currentColumnIndex === -1){
      return false;
    }

    // If the current column is the last column, there is no next column to unhide.
    if (currentColumnIndex === $scope.columns.length) {
      return false;
    }

    var nextColumnName;
    if (inverse) {
      nextColumnName = $scope.columns[currentColumnIndex - 1];
    } else {
      nextColumnName = $scope.columns[currentColumnIndex + 1];
    }

    var hiddenColumnIndex = $scope.columnsHidden.indexOf(nextColumnName);

    if (hiddenColumnIndex > -1){
      $scope.columnsHidden.splice(hiddenColumnIndex, 1);
      $scope.unhideNext(nextColumnName, inverse);
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

  $scope.setFilter = function(predicate, reverse){
    $scope.predicate = predicate;
    $scope.reverse = reverse;
  };

  $scope.changeCollection = function() {
    console.log('Selected: ' + $scope.collectionId);
    $location.path('/applications/'+currentApp.id+'/storage/'+$routeParams.storageId+'/browse/'+$scope.collectionId);
  };

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
      goToCollection(collectionName);
    });
  };

  $scope.collectionDelete = function() {
    $log.debug('Deleting collection: ' + $scope.collectionId);
    var deletePromise = LoCollection.delete({appId: currentApp.id, storageId: $routeParams.storageId,
      collectionId: $scope.collectionId}).$promise;

    deletePromise.then(function(){
      loadCollectionList(selectFirst);
    });
  };

  $scope.rowAdd = function(){
    $log.debug('Creating new row ' + $scope.newRow);
    $scope.newRows.push({});
  };

  $scope.rowRemoveNew = function (index) {
    $scope.newRows.splice(index, 1);
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
      $scope.rowsToDelete.push(id);
    } else {
      $log.error('Unable to find item to remove. The ID \"'+rowToRemove+'\" does not exist.');
    }

    $scope.isDataChange = true;
  };

  $scope.rowRemoveUndo = function(id) {
    $log.debug('Un-deleting row ' + id);

    var undoRowId = $scope.rowsToDelete.indexOf(id);

    if (undoRowId > -1){
      $scope.rowsToDelete.splice(undoRowId, 1);
    }
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
  };

  $scope.collectionClear = function(){
    $log.debug('Clearing collection ' + $scope.collectionId );

    $scope.columns = ['id'];
    $scope.collectionData._members = [];
    $scope.columnsHidden = [];
    $scope.isClearAll = true;
  };

  $scope.searchClear = function(){
    $scope.searchQuery = '';
    $scope.filterColumns = [];
    $scope.filterConditions = [];

    $scope.searchColumns = [];
    $scope.searchConditions = [{type:'EQUALS', text:''}];
  };

  $scope.searchConditionAdd = function(){
    $scope.searchConditions.push({type:'E', text:''});
  };

  $scope.searchConditionRemove = function(index){
    $log.debug('Going to remove '+index);
    $scope.searchConditions.splice(index,1);
  };

  $scope.advancedSearch = function(){
    $scope.filterColumns = angular.copy($scope.searchColumns);

    $scope.filterConditions = [];

    angular.forEach($scope.searchConditions, function(condition){
      if (condition.text !== '') {
        $scope.filterConditions.push(condition);
      }
    });

    $scope.showAdvanced = false;
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

      var removeFromList = function(id){
        for (var k in $scope.collectionData._members){
          var item = $scope.collectionData._members[k];
          if (item.id === id){
            $scope.collectionDataBackup._members.splice(k,1);
            return;
          }
        }
      };

    // Else delete specific rows only
      for (var i in $scope.rowsToDelete) {
        var itemToDelete = $scope.rowsToDelete[i];
        if (itemToDelete) {
          $log.debug('Going to delete: ' + JSON.stringify(itemToDelete));
          var deletePromise = LoCollectionItem.delete({appId: currentApp.id, storageId: $routeParams.storageId,
            collectionId: $scope.collectionId, itemId: itemToDelete}).$promise;

          deletePromise.then(removeFromList(itemToDelete));
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

      // If the JSON value could be represented as a number, auto-type the string to the number type
      itemToSave = autoType(itemToSave);

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

      }
    }

    for (var l in $scope.newRows){
      var newRowToSave = autoType($scope.newRows[l]);

      $log.debug('Creating: ' + JSON.stringify(newRowToSave));
      LoCollectionItem.create({appId: currentApp.id, storageId: $routeParams.storageId,
        collectionId: $scope.collectionId}, newRowToSave);
    }

    Notifications.success('The changes in \"' + $scope.collectionId + '\" have been saved.');

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

  function loadCollectionData(colId, loadColumns) {

    $log.debug('Loading collection data.');

    var dataPromise = LoCollectionItem.getList({appId: currentApp.id, storageId: $routeParams.storageId, collectionId: colId}).$promise;

    dataPromise.then(function (data) {
      if (loadColumns) {

        $scope.columns = ['id'];

        for (var rowIndex in data._members) {
          if (data._members && data._members[rowIndex]) {
            for (var c in data._members[rowIndex]) {
              if (c !== 'self' && c !== 'id' && $scope.columns.indexOf(c) === -1) {
                $scope.columns.push(c);
              }
            }
          }
        }
      }
      $scope.columnsBackup = angular.copy($scope.columns);
      $scope.collectionData = data;
      $scope.collectionDataBackup = angular.copy(data);
      $scope.isDataChange = false;
    });
  }

  function goToCollection(colId){
    $log.debug('Going to: ' + colId);
    if(colId) {
      $location.path('/applications/' + currentApp.id + '/storage/' + $routeParams.storageId + '/browse/' + colId);
    } else {
      $location.path('/applications/' + currentApp.id + '/storage/' + $routeParams.storageId + '/browse');
    }
  }

  // If nothing is selected, select the 1st item from the list
  function selectFirst() {
    if ($scope.collectionList) {
      goToCollection($scope.collectionList[0].id);
    } else {
      goToCollection(undefined);
    }
  }

  function autoType(obj){
    // If the JSON value could be represented as a number, auto-type the string to the number type
    for( var fieldId in obj){
      if (fieldId !== 'id' && obj.hasOwnProperty(fieldId)){
        var fieldVal = obj[fieldId];
        if (!isNaN(fieldVal)){
          obj[fieldId] = parseFloat(fieldVal);
        }
      }
    }

    return obj;
  }

  function loadCollectionList(callback) {
    $log.debug('Loading collection list');
    var promise = LoCollection.getList({appId: currentApp.id, storageId: $routeParams.storageId});
    promise.$promise.then(function (data) {
      $scope.collectionList = data._members;
      if (callback) {
        callback();
      }
    });
  }

  function resetEnv(){
    $scope.isColumnChange = false;
    $scope.isClearAll = false;
    $scope.isDataChange = false;
    $scope.newRows = [];
    $scope.rowsToDelete = [];
    $scope.searchQuery = '';
    $scope.filterColumns = [];
    $scope.filterConditions = [];
    $scope.searchColumns = [];
    $scope.searchConditions = [{type:'EQUALS', text:''}];
  }
});