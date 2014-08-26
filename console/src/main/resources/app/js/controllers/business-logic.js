'use strict';

var loMod = angular.module('loApp.controllers.businesslogic', []);

loMod.controller('BusinessLogicListCtrl', function($scope, $rootScope, $routeParams, $location, $http, $modal, $filter, Notifications, currentApp, triggeredScripts) {
  $rootScope.curApp = currentApp;

  $scope.breadcrumbs = [
    {'label': 'Applications', 'href': '#/applications'},
    {'label': currentApp.name, 'href': '#/applications/' + currentApp.id},
    {'label': 'Business Logic', 'href': '#/applications/' + currentApp.id + '/business-logic'}
  ];

  $scope.scripts  = triggeredScripts;

  $scope.showScript = function(script) {
    if (!script.src) {
      $http({method: 'GET', url: '/admin/applications/'+currentApp.id+'/resources/scripts/resource-triggered-scripts/'+script.id+'/script'}).
        success(function(data/*, status*/) {
          script.src = data;
          script.size = (data.length / 1024).toFixed(2);
          script.show = true;
        }).
        error(function(/*httpResponse, status*/) {
          Notifications.error('Failed to retreive the sources for script "' + script.id + '".');
        });
    }
    else {
      script.show = !script.show;
    }
  };

});