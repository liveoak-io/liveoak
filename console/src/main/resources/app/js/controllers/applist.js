'use strict';

var loMod = angular.module('loApp.controllers.application', []);

loMod.controller('AppListCtrl', function($scope, $routeParams, $location, Notifications, loAppList, LoApp, LoStorage, LoPush) {

  $scope.applications = [];

  $scope.createdId = $routeParams.created;

  for (var i = 0; i < loAppList._members.length; i++) {
    var app = {
      id: loAppList._members[i].id,
      name: loAppList._members[i].name
    };
    app.storage = LoStorage.getList({appId: app.id});
    $scope.applications.push(app);
  }

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
            ($scope.pushModel.upsServerURL && $scope.pushModel.applicationId && $scope.pushModel.masterSecret) ||
            (!$scope.pushModel.upsServerURL && !$scope.pushModel.applicationId && !$scope.pushModel.masterSecret));
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

              if($scope.pushModel && $scope.pushModel.upsServerURL) {
                var pushData = {
                  type: 'ups',
                  config: {
                    upsServerURL: $scope.pushModel.upsServerURL,
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