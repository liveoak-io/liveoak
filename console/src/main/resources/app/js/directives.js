'use strict';

/* Directives */
var loDirectives = angular.module('loApp.directives', []);

loDirectives.directive('loNavbar', function () {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-navbar.html',
  };
});

loDirectives.directive('loNavigation', function () {
  return {
    scope: {
      loCurrent: '@',
      loApps: '=',
      loApp: '='
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-navigation.html'
  };
});

loDirectives.directive('loAppSummary', function () {
  return {
    scope: {
      app: '=loApp'
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-app-summary.html'
  };
});

loDirectives.directive('loStorageSummary', function () {
  return {
    scope: {
      storage: '=loStorage',
      loApp: '='
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-storage-summary.html'
  };
});

loDirectives.directive('appVersion', ['version', function(version) {
  return function(scope, elm) {
    elm.text(version);
  };
}]);

/* Collapsible forms - Apply the lo-collapse attribute the the <legend> element. Aplly lo-collapse='true' to auto-hide
   current form elements.
 */
loDirectives.directive('loCollapse', function() {
  return {
    scope: {
      loCollapse: '='
    },
    restrict: 'A',
    link : function(scope, element) {
      var collapse = function(){
        element.toggleClass('collapsed');
        element.parent().find('.form-group').toggleClass('hidden');
      };

      if (scope.loCollapse){
        collapse();
      }

      element.click(function() {
        collapse();
      });
    }
  };
});

loDirectives.directive('loButtonSave', function (Notifications) {
  return {
    require: '^form',
    restrict: 'A',
    scope: {
      kcChanged: '=',
      loButtonSave: '&'
    },
    replace: false,
    link: function (scope, elem, attr, ctrl) {

      elem.addClass('btn btn-primary btn-lg');
      elem.attr('type','submit');

      if(ctrl){
        elem.bind('click', function(){
          var form = elem.closest('form');
          var ngValid = form.find('.ng-valid');
          ngValid.parent().removeClass('has-error');

          if (ctrl.$valid){
            scope.$apply(function() {
              scope.loButtonSave();
            });
          } else {
            Notifications.error('Missing or invalid field(s). Please verify the fields in red.');

            var ngInvalid = form.find('.ng-invalid');
            ngInvalid.parent().addClass('has-error');

            scope.$apply();
          }
        });
      } else {
        console.error('Parent form not found.');
      }
    }
  };
});


loDirectives.directive('loButtonClear', function () {
  return {
    scope: {
      loButtonClear: '&'
    },
    restrict: 'A',
    link: function (scope, elem) {
      elem.addClass('btn btn-default btn-lg');
      elem.attr('type','submit');
      elem.bind('click', function() {
        scope.$apply(function() {
          var form = elem.closest('form');
          if (form && form.attr('name')) {
            form.find('.ng-valid').removeClass('has-error');
            form.find('.ng-invalid').removeClass('has-error');
            scope.loButtonClear();
          }
        });
      });
    }
  };
});
