'use strict';

var loMod = angular.module('loApp.controllers.push', []);

loMod.controller('PushCtrl', function($scope, $rootScope, $log, LoPush, loPush, Notifications, currentApp) {

  $log.debug('PushCtrl');

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications',       'href':'#/applications'},
    {'label': currentApp.name,      'href':'#/applications/' + currentApp.id},
    {'label': 'Push Notifications', 'href':'#/applications/' + currentApp.id + '/push'}
  ];

  $scope.changed = false;
  $scope.create = $.isEmptyObject(loPush);

  var pushModelBackup = angular.copy(loPush);
  $scope.pushModel = angular.copy(loPush);

  $scope.pushAppName = loPush.applicationId;

  $scope.clear = function() {
    $scope.pushModel = angular.copy(pushModelBackup);
    $scope.changed = false;
  };

  $scope.$watch('pushModel', function() {
    $scope.changed = !angular.equals($scope.pushModel, pushModelBackup);
  }, true);

  $scope.save = function(){
    if(!$scope.pushModel.upsURL || !$scope.pushModel.applicationId || !$scope.pushModel.masterSecret){
      Notifications.error('All fields are required for push configuration.');
    }
    else {

      var configuration = {
        upsURL: $scope.pushModel.upsURL,
        applicationId: $scope.pushModel.applicationId,
        masterSecret: $scope.pushModel.masterSecret
      };

      var data = $scope.create ? { type: 'ups', config: configuration } : configuration;

      $log.debug('Updating push resource: ' + data);
      LoPush.update({appId: $scope.curApp.id}, data,
        // success
        function(/*value, responseHeaders*/) {
          Notifications.success('The push configuration has been ' + ($scope.create ? 'created' : 'updated') + '.');
          pushModelBackup = angular.copy($scope.pushModel);
          $scope.pushAppName = $scope.pushModel.applicationId;
          $scope.changed = false;
          $scope.create = false;
        },
        // error
        function(httpResponse) {
          Notifications.httpError('Failed to ' + ($scope.create ? 'create' : 'update') + ' the push configuration.', httpResponse);
        }
      );
    }
  };

  $scope.delete = function() {
    $log.debug('Deleting push resource.');
    LoPush.delete({appId: $scope.curApp.id},
      // success
      function(/*value, responseHeaders*/) {
        Notifications.success('The push configuration has been deleted.');
        $scope.pushModel = {};
        pushModelBackup = angular.copy($scope.pushModel);
        $scope.changed = false;
        $scope.create = true;
      },
      // error
      function(httpResponse) {
        Notifications.httpError('Failed to delete the push configuration.', httpResponse);
      }
    );
  };

});
