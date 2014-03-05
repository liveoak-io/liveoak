'use strict';

var loMod = angular.module('loApp');

loMod.controller('AppListCtrl', function($scope, $routeParams, Current, breadcrumbs) {

  $scope.breadcrumbs = breadcrumbs;

  // FIXME: Mock data
  $scope.applications =
    [
      { 'name': 'My App', 'storage': ['Mongo DB'], 'push': false, 'secured': false },
      { 'name': 'Other App', 'storage': ['H2', 'MySQL'], 'push': true, 'secured': true }
    ];

});