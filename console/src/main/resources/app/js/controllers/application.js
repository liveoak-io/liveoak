'use strict';

var loMod = angular.module('loApp.controllers.application', []);

loMod.controller('AppListCtrl', function($scope, $routeParams, $location, $modal, Notifications, loAppList, LoApp, LoStorage, LoPush) {

  $scope.applications = [];

  $scope.createdId = $routeParams.created;

  var increaseStorages = function (resources) {
    for (var j = 0; j < resources._members.length; j++) {
      if(resources._members[j].hasOwnProperty('MongoClientOptions')) {
        app.mongoStorages++;
      }
    }
  };

  for (var i = 0; i < loAppList._members.length; i++) {
    var app = {
      id: loAppList._members[i].id,
      name: loAppList._members[i].name
    };
    app.storage = LoStorage.getList({appId: app.id});

    app.mongoStorages = 0;
    app.storage.$promise.then(increaseStorages);
    app.push = LoPush.get({appId: app.id});

    $scope.applications.push(app);
  }

  // Delete Application
  $scope.modalApplicationDelete = function(appId) {
    $scope.deleteAppId = appId;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/application/application-delete.html',
      controller: DeleteApplicationModalCtrl,
      scope: $scope
    });
  };

  var DeleteApplicationModalCtrl = function ($scope, $modalInstance, $log, LoApp) {

    $scope.applicationDelete = function (appId) {
      $log.debug('Deleting application: ' + appId);
      LoApp.delete({appId: appId},
        // success
        function(/*value, responseHeaders*/) {
          Notifications.success('Application "' + appId + '" deleted successfully.');
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete application "' + appId + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

  // New Application Wizard

  $scope.setupStep = 1; // current step
  $scope.setupSteps = 3; // total steps
  $scope.setupType = 'basic'; // setup type: basic or diy

  $scope.storagePath = '';

  $scope.nextStep = function() {
    $scope.setupStep++;
  };

  $scope.prevStep = function() {
    $scope.setupStep--;
  };

  $scope.stepValid = function() {
    // Step 1: Require name
    switch($scope.setupStep) {
      case 1:
        return $scope.appModel.id;
      case 2:
        return $scope.storagePath;
      case 3:
        return !$scope.pushModel ||
          ($scope.pushModel &&
            ($scope.pushModel.upsURL && $scope.pushModel.applicationId && $scope.pushModel.masterSecret) ||
            (!$scope.pushModel.upsURL && !$scope.pushModel.applicationId && !$scope.pushModel.masterSecret));
      default:
        return false;
    }
  };

  $scope.appModel = {
  };

  var redirectOnNewAppSuccess = function() {
    //$location.search('created', $scope.appModel.id).path('applications');
    $location.path('applications/' + $scope.appModel.id + '/next-steps');
  };

  $scope.create = function() {
    var data = {
      id: $scope.appModel.id,
      name: $scope.appModel.id,
      type: 'application',
      config: {
      }
    };

    LoApp.create(data,
      // success
      function(/*value, responseHeaders*/) {
        Notifications.success('New application successfully created.');
        if($scope.setupType === 'basic') {
          var storageData = {
            id: $scope.storagePath,
            type: 'mongo',
            config: {
              db: $scope.appModel.id,
              servers: [{host: 'localhost', port: 27017}],
              credentials: []
            }
          };

          LoStorage.create({appId: $scope.appModel.id}, storageData,
            // success
            function(/*value, responseHeaders*/) {
              Notifications.success('New storage successfully created.');

              if($scope.pushModel && $scope.pushModel.upsURL) {
                var pushData = {
                  type: 'ups',
                  config: {
                    upsURL: $scope.pushModel.upsURL,
                    applicationId: $scope.pushModel.applicationId,
                    masterSecret: $scope.pushModel.masterSecret
                  }
                };

                LoPush.update({appId: $scope.appModel.id}, pushData,
                  // success
                  function (/*value, responseHeaders*/) {
                    Notifications.success('Push configuration created successfully.');
                    redirectOnNewAppSuccess();
                  },
                  // error
                  function (httpResponse) {
                    Notifications.httpError('Failed to update Push configuration', httpResponse);
                  }
                );
              }
              else {
                redirectOnNewAppSuccess();
              }
            },
            // error
            function(httpResponse) {
              Notifications.httpError('Failed to create new storage', httpResponse);
              // TODO: Rollback ?
            });
        }
        else {
          redirectOnNewAppSuccess();
        }
      },
      // error
      function(httpResponse) {
        Notifications.httpError('Failed to create new application', httpResponse);
      });
  };

});

loMod.controller('AppSettingsCtrl', function($scope, $rootScope, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Settings',      'href':'#/applications/' + currentApp.id + '/application-settings'}
  ];

});

loMod.controller('AppClientsCtrl', function($scope, $rootScope, currentApp) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Clients',      'href':'#/applications/' + currentApp.id + '/application-clients'}
  ];

});

loMod.controller('NextStepsCtrl', function($scope, $rootScope, $routeParams, currentApp, loStorageList, loPush) {

  $rootScope.curApp = currentApp;

  $scope.storageList = [];

  /* jshint unused: false */
  angular.forEach(loStorageList._members, function (value, key) {
    if (value.hasOwnProperty('db')) {
      this.push({id: value.id, provider: value.hasOwnProperty('MongoClientOptions') ? 'mongoDB' : 'unknown'});
    }
  }, $scope.storageList);
  /* jshint unused: true */

  $scope.pushConfig = loPush;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Next Steps', 'href': '#/applications/' + currentApp.id + '/next-steps'}
  ];

});
