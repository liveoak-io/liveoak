'use strict';

var loMod = angular.module('loApp.controllers.application', []);

loMod.controller('AppListCtrl', function($scope, $routeParams, breadcrumbs, loAppList, LoStorage) {

  $scope.breadcrumbs = breadcrumbs;

  $scope.applications = [];

  for (var i = 0; i < loAppList._members.length; i++) {
    var app = {
      id: loAppList._members[i].id,
      name: loAppList._members[i].name
    };
    app.storage = LoStorage.getList({appId: app.id});
    $scope.applications.push(app);
  }

});