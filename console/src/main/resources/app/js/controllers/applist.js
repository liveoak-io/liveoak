'use strict';

var loMod = angular.module('loApp.controllers.application', []);

loMod.controller('AppListCtrl', function($scope, $routeParams, $location, Notifications, loAppList, LoApp, LoStorage) {

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

  $scope.setupStep = 1;
  $scope.setupType = "basic";

  $scope.nextStep = function() {
    $scope.setupStep++;
  };

  $scope.prevStep = function() {
    $scope.setupStep--;
  };

  $scope.prevStep = function() {
    $scope.setupStep--;
  };

  $scope.appModel = {
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
        $location.search('created', $scope.appModel.id).path('applications');
      },
      // error
      function(httpResponse) {
        Notifications.httpError('Failed to create new application', httpResponse);
      });
  }

});