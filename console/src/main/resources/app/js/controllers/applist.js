'use strict';

var loMod = angular.module('loApp');

loMod.controller('AppListCtrl', function($scope, $routeParams, Current, breadcrumbs, loAppList) {

  $scope.breadcrumbs = breadcrumbs;

  $scope.applications = loAppList._members;

  // FIXME: Mock data
  $scope.applications.push({ 'name': 'Mock My App', 'storage': ['Mongo DB'], 'push': false, 'secured': false });
  $scope.applications.push({ 'name': 'Mock Other App', 'storage': ['H2', 'MySQL'], 'push': true, 'secured': true });

});