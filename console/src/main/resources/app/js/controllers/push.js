'use strict';

var loMod = angular.module('loApp.controllers.push', []);

loMod.controller('PushCtrl', function($scope, $rootScope, $log, LoPush, loPush, Notifications, currentApp,
                                      loRemoteCheck, loPushPing, $resource, $modal) {

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

  // Field indicating if the pre-configured URL could be reached (successful ping)
  $scope.connected = false;

  $scope.checkPushAuth = function() {
    // Turns on the spinner in pre-configured push panel;
    $scope.configuredUrlPing = true;
    $scope.connected = false;

    $resource($scope.pushModel.upsURL + '/rest/ping/').get({}, function(){
      $scope.configuredUrlPing = false;
      $scope.connected = true;
    }, function(error){
      $scope.configuredUrlPing = false;
      $scope.connected = false;
      if( error.status === 401 ) {
        $scope.connected = true;
      }
    });
  };

  if($scope.pushModel.upsURL) {
    $scope.checkPushAuth();
  }

  $scope.checkPushUrl = function(pushUrl){
    // Turns on the spinner in push url input message;
    $scope.pushUrlPing = true;

    var _res = loPushPing(pushUrl + '/rest/ping/');
    var _resMethod = _res.ping;

    var _callbacks = {
      success: function(){
        $scope.pushUrlPing = false;
        // Hides the message about URL not reachable under the url input;
        $scope.pushUrlInvalid = false;
      },
      error: function(error){
        $scope.pushUrlPing = false;
        if( error.status === 401 ) {
          $scope.pushUrlInvalid = false;
        } else {
          $scope.pushUrlInvalid = true;
        }
      }
    };

    // Actual checking of URL happens 300ms after user stops typing
    this.timeout = loRemoteCheck(this.timeout, _resMethod, {}, _callbacks);
  };

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
          $scope.checkPushAuth();
        },
        // error
        function(httpResponse) {
          Notifications.httpError('Failed to ' + ($scope.create ? 'create' : 'update') + ' the push configuration.', httpResponse);
        }
      );
    }
  };

  $scope.pushDelete = function () {
    $log.debug('Deleting push resource.');
    LoPush.delete({appId: $scope.curApp.id},
      // success
      function(/*value, responseHeaders*/) {
        Notifications.success('The push configuration has been deleted.');
        $scope.pushModel = {};
        pushModelBackup = {};
        $scope.pushForm.$setPristine();
        $scope.changed = false;
        $scope.create = true;
      },
      // error
      function(httpResponse) {
        Notifications.httpError('Failed to delete the push configuration.', httpResponse);
      }
    );
  };

  $scope.modalPushDelete = function(){
    $modal.open({
      templateUrl: '/admin/console/templates/modal/push/push-delete.html',
      controller: 'DefaultModalCtrl',
      scope: $scope
    });
  };

  $rootScope.preventLoseChanges($scope/*, $scope.save, $scope.clear*/);
});
