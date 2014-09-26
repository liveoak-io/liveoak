'use strict';

var loMod = angular.module('loApp.controllers.businesslogic', []);

loMod.controller('BusinessLogicListCtrl', function($scope, $rootScope, $routeParams, $location, $http, $modal, $filter, Notifications, currentApp, triggeredScripts) {
  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Business Logic', 'href': '#/applications/' + currentApp.id + '/business-logic'}
  ];

  $scope.scripts = triggeredScripts;

  $scope.showScript = function (script) {
    if (!script.src) {
      $http({method: 'GET', url: '/admin/applications/' + currentApp.id + '/resources/scripts/resource-triggered-scripts/' + script.id + '/script'}).
        success(function (data/*, status*/) {
          script.src = data;
          script.size = (data.length / 1024).toFixed(2);
          script.show = true;
        }).
        error(function (/*httpResponse, status*/) {
          Notifications.error('Failed to retreive the sources for script "' + script.id + '".');
        });
    }
    else {
      script.show = !script.show;
    }
  };

  $scope.viewerOptions = {
    lineWrapping : true,
    lineNumbers: true,
    extraKeys: {'Ctrl-Space': 'autocomplete'},
    mode: {name: 'javascript', globalVars: true},
    readOnly: true
  };

  // Delete Script
  $scope.modalScriptDelete = function(script) {
    $scope.script = script;
    $modal.open({
      templateUrl: '/admin/console/templates/modal/business-logic/script-delete.html',
      controller: DeleteScriptModalCtrl,
      scope: $scope
    });
  };

  var DeleteScriptModalCtrl = function ($scope, $modalInstance, $route, $log, LoBusinessLogicScripts) {

    $scope.scriptDelete = function (script) {
      LoBusinessLogicScripts.delete({appId: $scope.curApp.id, type: 'resource-triggered-scripts', scriptId: $scope.script.id},
        // success
        function(/*value, responseHeaders*/) {
          Notifications.success('The script "' + script.id + '" has been deleted.');
          $route.reload();
          $modalInstance.close();
        },
        // error
        function (httpResponse) {
          Notifications.httpError('Failed to delete the script "' + script.id + '".', httpResponse);
        }
      );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

  };

});

loMod.controller('BusinessLogicDetailsCtrl', function($scope, $rootScope, $routeParams, $location, $http, $modal, $filter, Notifications, currentApp, currentScript) {

  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Business Logic', 'href': '#/applications/' + currentApp.id + '/business-logic'},
    {'label': (currentScript.id || 'New Script'), 'href': '#/applications/' + currentApp.id + '/business-logic'}
  ];

  $scope.create = currentScript.id ? false : true;

  $scope.script = currentScript;

  // settings backup to compare for changes and rollback
  var settingsBackup;

  $scope.changed = false;

  // Get the script itself
  if(!$scope.create) {
    $http({method: 'GET', url: '/admin/applications/' + currentApp.id + '/resources/scripts/resource-triggered-scripts/' + currentScript.id + '/script'}).
      success(function (data/*, status*/) {
        $scope.script.code = data;
      }).
      error(function (/*httpResponse, status*/) {
        $scope.script.code = '';
      }).
      finally(function() {
        settingsBackup = angular.copy($scope.script);

        $scope.$watch('script', function() {
          $scope.changed = !angular.equals($scope.script, settingsBackup);
        }, true);
      });
  }
  else {
    settingsBackup = angular.copy($scope.script);

    $scope.$watch('script', function() {
      $scope.changed = !angular.equals($scope.script, settingsBackup);
    }, true);
  }

  $scope.editorOptions = {
    lineWrapping : true,
    lineNumbers: true,
    matchBrackets: true,
    autoCloseBrackets: true,
    gutters: ['CodeMirror-lint-markers'],
    lint: true,
    styleActiveLine: true,
    extraKeys: {'Ctrl-Space': 'autocomplete'},
    mode: {name: 'javascript', globalVars: true}
  };

  $scope.clear = function() {
    $scope.script = angular.copy(settingsBackup);
  };

  $scope.save = function() {
    var scriptMeta = angular.copy($scope.script);
    var scriptCode = angular.copy($scope.script.code);
    delete(scriptMeta.code);
    scriptMeta.$create({'appId': currentApp.id, 'type':'resource-triggered-scripts'},
      function(data) {
        $scope.script = angular.copy(data);
        //Notifications.success('The script "' + $scope.script.id + '" has been created.');
        //$scope.script.$setSource({'appId': currentApp.id, 'type':'resource-triggered-scripts'});
        $http({
          method: 'POST',
          url: '/admin/applications/' + currentApp.id + '/resources/scripts/resource-triggered-scripts/' + $scope.script.id,
          headers: { 'Content-Type':'application/javascript' },
          data: scriptCode
        }).
        success(function (data/*, status*/) {
          Notifications.success('The script "' + $scope.script.id + '" has been created.');
          $scope.script.code = data;
          settingsBackup = angular.copy($scope.script);
        }).
        error(function (/*httpResponse, status*/) {
          Notifications.error('Failed to set script source for "' + $scope.script.id + '".');
        }).
        finally(function() {
          $scope.create = false;
        });
        $location.path('/applications/' + currentApp.id + '/business-logic');
      },
      function(httpResponse) {
        console.log(httpResponse);
        Notifications.httpError('Failed to ' + ($scope.create ? 'create' : 'update') + ' the script "' + $scope.script.id + '".', httpResponse);
      }
    );
  };

});