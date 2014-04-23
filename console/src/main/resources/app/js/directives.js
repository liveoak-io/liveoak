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

loDirectives.directive('loBreadcrumbs', function () {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-breadcrumbs.html'
  };
});

loDirectives.directive('loAppSummary', function () {
  return {
    scope: {
      app: '=loApp',
      created: '='
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-app-summary.html'
  };
});

loDirectives.directive('loStorageSummary', function (LoCollection) {
  return {
    scope: {
      storage: '=loStorage',
      created: '=',
      loApp: '=',
      loDelete: '&'
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-storage-summary.html',
    link: function(scope){
      LoCollection.getList({appId: scope.loApp.id, storageId: scope.storage.path}, function(){
        scope.storage.hasCollections = true;
      });
    }
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
      element.addClass('clickable');
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
      elem.attr('type','button');
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

loDirectives.directive('loButtonDelete', function () {
  return {
    scope: {
      loButtonDelete: '&'
    },
    restrict: 'A',
    link: function (scope, elem) {
      elem.addClass('btn btn-danger btn-lg');
      elem.attr('type','button');
      elem.bind('click', function() {
        scope.$apply(function() {
          scope.loButtonDelete();
        });
      });
    }
  };
});

/*
 For this directive to work correctly the Collection for generating options must be set in ng-options or directly
 as a value for directive attribute, i.e. lo-select='myOptionCollection'

 TODO - There is a bug when using this directive within a ng-repeat loop. As a workaround, please use any value as
  a parameter of loSelect attribute. Please note, that this will treat your option list as a constant, and changes
  to the option list won't be reflected in the select element.
*/
loDirectives.directive('loSelect', function($timeout) {
  return {
    restrict: 'A',
    link: function(scope, element, iAttrs){
      // Name of the options collection
      var options;

      if (iAttrs.loSelect){
        options = iAttrs.loSelect;
      } else {
        // If loSelect attribute has no value assigned, parse the options collection name from ng-options
        options = iAttrs.ngOptions.split('in').pop().trim();
      }

      var initSelectpicker = function(){
        element.selectpicker('refresh');
      };

      if (!iAttrs.loSelect) {
        scope.$watchCollection(options, function () {
          initSelectpicker();
        });
      }

      $timeout(initSelectpicker, 0, false);
    }
  };
});

loDirectives.directive('loHttpPrefix', function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attrs, controller) {
      function ensureHttpPrefix(value) {
        // Need to add prefix if we don't have http:// prefix already AND we don't have part of it
        if(value && !/^(https?):\/\//i.test(value) && ('http://'.indexOf(value) === -1 && 'https://'.indexOf(value) === -1)) {
          controller.$setViewValue('http://' + value);
          controller.$render();
          return 'http://' + value;
        }
        else {
          return value;
        }
      }
      controller.$formatters.push(ensureHttpPrefix);
      controller.$parsers.splice(0, 0, ensureHttpPrefix);
    }
  };
});

loDirectives.directive('loAutofocus', function($timeout) {
  return {
    priority: 1,
    link: function (scope, element) {
      $timeout(function () {
        var elems = element.find('[autofocus]');
        if (elems && elems.length > 0) {
          elems[0].focus();
        }
      }, 150);
    }
  };
});