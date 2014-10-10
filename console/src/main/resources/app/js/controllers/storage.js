'use strict';

var loMod = angular.module('loApp.controllers.storage', []);

loMod.controller('StorageCtrl', function($scope, $rootScope, $location, $routeParams, $log, LoStorage, loStorage, Notifications, currentApp) {

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
  $scope.fix = $routeParams.fix;

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

  $scope.passwordInputType = 'password';
  $scope.changePasswordInputType = function() {
    if ($scope.passwordInputType === 'password') {
      $scope.passwordInputType = 'text';
    }
    else {
      $scope.passwordInputType = 'password';
    }
  };

  $scope.clear = function(){
    $scope.storageModel = angular.copy(storageModelBackup);
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

      Notifications.error('Please fill in both the username and password fields.');
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
            Notifications.success('The storage "' + data.id + '" has been created.');
            storageModelBackup = angular.copy($scope.storageModel);
            $scope.changed = false;
            $location.search('created', $scope.storageModel.id).path('applications/' + currentApp.id + '/storage');
          },
          // error
          function(httpResponse) {
            Notifications.httpError('Failed to create the storage "' + data.id + '".', httpResponse);
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
          Notifications.success('The storage "' + storageModelBackup.id + '" has been updated.');
          $location.path('applications/' + currentApp.id + '/storage');
        },
        function(httpResponse){
          // Update failure
          Notifications.httpError('Failed to update the storage "' + storageModelBackup.id + '".', httpResponse);
        });
      }
    }
  };

});

loMod.controller('StorageListCtrl', function($scope, $rootScope, $log, $routeParams, loStorageList, currentApp,
                                             LoStorage, Notifications, $modal, $filter) {

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

  if (loStorageList.members) {
    for (var i = 0; i < loStorageList.members.length; i++) {

      var resource = loStorageList.members[i];

      if (resource.hasOwnProperty('db')) {
        $scope.resources.push({
          provider: 'mongoDB',
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
        Notifications.success('The storage "' + $scope.storageId + '" has been deleted.');
        var _deletedStorage = $filter('filter')($scope.resources, {'path':$scope.storageId})[0];
        var _deletedIndex = $scope.resources.indexOf(_deletedStorage);

        $scope.resources.splice(_deletedIndex, 1);
      },
      function(httpResponse){
        Notifications.httpError('Failed to delete the storage "' + $scope.storageId + '".', httpResponse);
      });
  };
});

loMod.controller('StorageCollectionCtrl', function($scope, $rootScope, $log, $route, currentApp, $modal, Notifications,
                                                   currentCollectionList, LoCollection, $routeParams, LoCollectionItem,
                                                   LiveOak, $location, $window, $cookieStore, $q, loRemoteCheck, loJSON) {

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

  $scope.collectionList = currentCollectionList.members;

  $scope.collectionData = [];
  $scope.collectionDataBackup = [];

  $scope.live = {};

  $scope.columnsHidden = [];
  $scope.newRow = {};
  $scope.newRows = [];
  $scope.rowsToDelete = [];

  // TODO - seems to be redundant because with each column change, there's a data change.
  $scope.isColumnChange = false;
  $scope.isClearAll = false;
  $scope.isDataChange = false;

  $scope.searchColumns = [];
  $scope.searchConditions = [{type:'E', text:''}];
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
  if ( currentCollectionList && $scope.collectionId) {

    loadCollectionData($scope.collectionId, true);

    var connectCallback = function() {
      if ($scope.subscriptionId){
        $log.debug('Removing subscription "' + $scope.subscriptionId + '"');
        LiveOak.unsubscribe($scope.subscriptionId);
        $rootScope.subscriptionId = false;
      }

      if (!$scope.subscriptionId) {
        var urlSubscribe = '/' + currentApp.id + '/' + $routeParams.storageId + '/' + $scope.collectionId + '/*';
        $rootScope.subscriptionId = LiveOak.subscribe(urlSubscribe, function (data) {
          $log.debug('Data reload callback');
          //Notifications.warn('Data were changed outside console: ' + JSON.stringify(data));
          loadCollectionData($scope.collectionId, true);
          $scope.live = data;
        });

        $log.debug('Subscribe id is: ' + $scope.subscriptionId);
      }
    };

    LiveOak.auth.updateToken(5).success(function() {
      $log.debug('Valid token found. Issuing authenticated connect');
      LiveOak.connect('Bearer', LiveOak.auth.token, connectCallback);
    }).error(function() {
      $log.debug('Can\'t retrieve valid token. Issuing unauthenticated connect');
      LiveOak.connect(connectCallback);
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
    $location.path('/applications/'+currentApp.id+'/storage/'+$routeParams.storageId+'/browse/'+$scope.collectionId);
  };

  $scope.$watch('collectionData', function(){
      $log.debug('Collection data changed.');
      if(!angular.equals($scope.collectionData, $scope.collectionDataBackup)){
        $log.debug('Collection data differs from original.');
        $scope.isDataChange = true;
      } else {
        $scope.isDataChange = false;
      }
      if ($scope.collectionForm.$valid) {
        updateExportUrl();
      }
    }, true
  );

  $scope.$watch('columns', function(){
      $log.debug('Columns number changed.');
      if(!angular.equals($scope.collectionData, $scope.collectionDataBackup)){
        $log.debug('Column number differs from original.');
        $scope.isColumnChange = true;
      }
    }, true
  );

  $scope.isInfoClosed = $cookieStore.get($scope.username + '_isInfoClosed');

  $scope.infoClose = function() {
    $cookieStore.put($scope.username + '_isInfoClosed', true);
    $scope.isInfoClosed = true;
  };

  var ModalInstanceCtrl = function ($scope, $modalInstance, FileReader, Notifications) {

    $scope.close = function () {
      $modalInstance.close();
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    $scope.setFormScope= function(scope){
      $scope.modalScope = scope;
      $scope.modalScope.progress = 0;

      $scope.modalScope.$on('fileProgress', function(e, progress) {
        $scope.modalScope.progress = progress.loaded / progress.total;
      });
    };

    $scope.checkColName = function (collectionName) {
      var _resMethod = LoCollection.check;
      var _resParams = {appId: currentApp.id, storageId: $routeParams.storageId, collectionId: collectionName};

      var _formCtrl = $scope.modalScope.loCollectionCreate;
      var _inputCtrl = _formCtrl.collectionName;

      this.timeout = loRemoteCheck(this.timeout, _resMethod, _resParams, _formCtrl, _inputCtrl, 'collectionName');
    };

    $scope.getFile = function () {

      $scope.modalScope.$apply(function(){
        $scope.modalScope.progress = 0;
        $scope.modalScope.inProgress = true;
      });
      FileReader.readAsDataUrl($scope.modalScope.file, $scope.modalScope)
        .then(function(data) {
          try {
            $scope.jsonData = JSON.parse(data);
            $scope.modalScope.progress = 1;
          } catch (e){
            Notifications.error('An error occured during the JSON parsing: ' + e);
            $scope.modalScope.progress = 0;
            $scope.modalScope.isError = true;
          }
        });
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

  $scope.modalCollectionImport = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/storage/collection-import.html',
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
      Notifications.success('The collection "' + collectionName + '" has been created.');
      goToCollection(collectionName);
    }, function() {
      Notifications.error('Not able to create the collection "' + collectionName + '".');
    });
  };

  $scope.collectionDelete = function() {
    $log.debug('Deleting collection: ' + $scope.collectionId);
    var deletePromise = LoCollection.delete({appId: currentApp.id, storageId: $routeParams.storageId,
      collectionId: $scope.collectionId}).$promise;

    deletePromise.then(function(){
      Notifications.success('The collection "' + $scope.collectionId + '" has been deleted.');
      loadCollectionList(selectFirst);
    }, function() {
      Notifications.error('Failed to delete the collection "' + $scope.collectionId + '".');
    });
  };

  function updateExportUrl(){
    try {
      $log.debug('Updating export url');
      var dataObj = angular.copy($scope.collectionData);
      var dataExportObj = [];
      for (var row in dataObj) {
        if (dataObj[row].hasOwnProperty('id')) {
          delete dataObj[row].id;
        }
        if (dataObj[row].hasOwnProperty('self')) {
          delete dataObj[row].self;
        }
        dataExportObj.push(angular.toJson(loJSON.parseJSON(dataObj[row])));
      }

      var blob = new Blob(['[' + dataExportObj.toString() + ']'], { type: 'text/plain' });
      var urlCreator = $window.URL || $window.webkitURL || $window.mozURL || $window.msURL;

      $scope.urlExport = urlCreator.createObjectURL(blob);
      $scope.jsonName = $scope.collectionId + '_' + (new Date()).toISOString() + '.json';
    } catch(e) {
      // Do nothing if the collection form is invalid
    }
  }

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

    for (var k in $scope.collectionData){
      var item = $scope.collectionData[k];
      if (item.id === id) {
        rowToRemove = k;
        break;
      }
    }

    if (rowToRemove > -1){
      $scope.rowsToDelete.push(id);
    } else {
      $log.error('Unable to find item to remove. The ID "'+rowToRemove+'" does not exist.');
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
/*
    // Commenting because the empty string is not the same as no value.
    for (var k in $scope.collectionData){
      var item = $scope.collectionData[k];
      item[columnName] = '';
    }
*/
    $scope.isDataChange = true;
    $scope.isColumnChange = true;
  };

  $scope.columnRemove = function(columnName){
    $log.debug('Removing column ' + columnName );
    var index = $scope.columns.indexOf(columnName);

    if (index > -1) {
      $scope.columns.splice(index, 1);

      for (var k in $scope.collectionData) {
        var item = $scope.collectionData[k];
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

    for (var i in $scope.collectionData){
      var row = $scope.collectionData[i];
      row[col] = '';
    }
  };

  $scope.columnHide = function(col) {
    $scope.columnsHidden.push(col);
  };

  $scope.columnAvailable = function(input){

    if ($scope.columns.indexOf(input) > -1){
      return false;
    }

    return true;
  };

  $scope.collectionClear = function(){
    $log.debug('Clearing collection ' + $scope.collectionId );

    $scope.columns = ['id'];
    $scope.collectionData = [];
    $scope.columnsHidden = [];
    $scope.isClearAll = true;
  };

  $scope.searchClear = function(){
    $scope.searchQuery = '';
    $scope.filterColumns = [];
    $scope.filterConditions = [];

    $scope.searchColumns = [];
    $scope.searchConditions = [{type:'E', text:''}];
  };

  $scope.searchConditionAdd = function(){
    $scope.searchConditions.push({type:'E', text:''});
  };

  $scope.searchConditionsEmpty = true;

  $scope.$watch('searchConditions', function(){
    for (var index in $scope.searchConditions){
      var condition = $scope.searchConditions[index];

      if (!condition.text || condition.text === ''){
        $scope.searchConditionsEmpty = true;
        return;
      }
    }
    $scope.searchConditionsEmpty = false;
  }, true);

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

  $scope.collectionImport = function(removeAll, data) {

    if (removeAll) {
      $scope.collectionClear();
    }

    for(var i in data){
      var row = data[i];

      for (var j in row){
        if (row.hasOwnProperty(j) && ($scope.columns.indexOf(j) === -1)) {
          $scope.columnAdd(j);
        }
      }

      $scope.newRows.push(loJSON.toStringObject(row));
    }

  };

  $scope.save = function(){
    $log.debug('Saving collection.');

    var promises = [];

    // If Clear All was clicked, no need to delete specific rows, everything will be deleted.
    if ($scope.isClearAll) {
      for (var j in $scope.collectionDataBackup){
        var itemToClear = $scope.collectionDataBackup[j];

        var deleteAllPromise = LoCollectionItem.delete({appId: currentApp.id, storageId: $routeParams.storageId,
          collectionId: $scope.collectionId, itemId: itemToClear.id.substring(1,itemToClear.id.length-1)}).$promise;

        promises.push(deleteAllPromise);
      }
    } else {

      var removeFromList = function(id){
        for (var k in $scope.collectionData){
          var item = $scope.collectionData[k];
          if (item.id === id){
            $scope.collectionDataBackup.splice(k,1);
            return;
          }
        }
      };

    // Else delete specific rows only
      for (var i in $scope.rowsToDelete) {
        var itemToDelete = $scope.rowsToDelete[i];
        if (itemToDelete) {
          var deletePromise = LoCollectionItem.delete({appId: currentApp.id, storageId: $routeParams.storageId,
            collectionId: $scope.collectionId, itemId: itemToDelete.substring(1,itemToDelete.length-1)}).$promise;

          promises.push(deletePromise);

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
    for (var k in $scope.collectionData){
      var item = $scope.collectionData[k];

      var itemToSave = angular.copy(item);
      delete itemToSave.self;

      // Update existing items
      var itemFromBackup = findById(itemToSave.id, $scope.collectionDataBackup);
      if (itemFromBackup && itemFromBackup.self) {
        delete itemFromBackup.self;
      }

      if (itemToSave.id) {

        $log.debug('Checking for update: ' + angular.toJson(itemToSave));
        if (itemFromBackup && !angular.equals(itemToSave, itemFromBackup)) {

          var decodedItemToSave = loJSON.parseJSON(itemToSave);
          var updatePromise = LoCollectionItem.update({appId: currentApp.id, storageId: $routeParams.storageId,
            collectionId: $scope.collectionId, itemId: decodedItemToSave.id}, decodedItemToSave).$promise;

          promises.push(updatePromise);
        } else {
          $log.debug('Not updating:        ' + angular.toJson(itemToSave));
        }
      }
      // Create new items
      else {

      }
    }

    function errorCreate(result){
      var errorType = result.data.message || result.data['error-type'];
      var object = result.config.data;
      Notifications.error('Error: "' + errorType + '" during saving "' + JSON.stringify(object) + '" to the collection \"' + $scope.collectionId + '\".');
    }

    for (var l in $scope.newRows){
      var newRowToSave = $scope.newRows[l];
      $log.debug('Creating: ' + newRowToSave);
      var decodedNewRowToSave = loJSON.parseJSON(newRowToSave);

      var promiseCreate = LoCollectionItem.create({appId: currentApp.id, storageId: $routeParams.storageId,
        collectionId: $scope.collectionId}, decodedNewRowToSave, angular.noop, errorCreate).$promise;

      promises.push(promiseCreate);
    }

    $q.all(promises).then(function() {
      Notifications.success('The changes in the collection "' + $scope.collectionId + '" have been saved.');
    }, function() {
      Notifications.warn('There were some errors during update of the collection "' + $scope.collectionId + '".');
    });

    resetEnv();
    loadCollectionData($scope.collectionId, true);
  };

  $scope.reset = function(){
    $log.debug('Resetting the page.');

    resetEnv();

    $scope.columns = angular.copy($scope.columnsBackup);
    $scope.collectionData = angular.copy($scope.collectionDataBackup);
  };

  function loadCollectionData(colId, loadColumns) {

    $log.debug('Loading collection data.');

    var dataPromise = LoCollectionItem.getList({appId: currentApp.id, storageId: $routeParams.storageId, collectionId: colId}).$promise;

    dataPromise.then(function (data) {
      if (loadColumns) {

        $scope.columns = ['id'];

        for (var rowIndex in data.members) {
          if (data.members && data.members[rowIndex]) {
            for (var c in data.members[rowIndex]) {
              if (c !== 'self' && c !== 'id' && $scope.columns.indexOf(c) === -1) {
                $scope.columns.push(c);
              }
            }
          }
        }
      }

      $scope.collectionData = [];

      for (var rIndex in data.members){
        var rData = data.members[rIndex];
        $scope.collectionData.push(loJSON.toStringObject(rData));
      }

      $scope.collectionDataBackup = angular.copy($scope.collectionData);
      $scope.columnsBackup = angular.copy($scope.columns);
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

  $scope.isValidJSON = loJSON.isValidJSON;

  function loadCollectionList(callback) {
    $log.debug('Loading collection list');
    var promise = LoCollection.getList({appId: currentApp.id, storageId: $routeParams.storageId});
    promise.$promise.then(function (data) {
      $scope.collectionList = data.members;
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
    $scope.searchConditions = [{type:'E', text:''}];
  }
});
