'use strict';

/* Directives */
var loDirectives = angular.module('loApp.directives', []);

loDirectives.directive('loNavbar', function () {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-navbar.html'
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

loDirectives.directive('loClientSummary', function () {
  return {
    scope: {
      app: '=loApp',
      client: '=loClient',
      created: '='
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-client-summary.html'
  };
});

loDirectives.directive('loCollectionSecuritySummary', function () {
  return {
    scope: {
      app: '=loApp',
      storage: '=loStorage',
      collection: '=loCollection',
      created: '='
    },
    restrict: 'E',
    replace: true,
    templateUrl: '/admin/console/templates/lo-collection-security-summary.html'
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

loDirectives.directive('loSelect', function($timeout) {
  return {
    restrict: 'A',
    require: '?ngModel',
    link: function (scope, element, attrs, ngModel) {
      element.selectpicker();

      ngModel.$render = function() {
        element.val(ngModel.$viewValue || '');
        $timeout(function() {
          element.selectpicker('refresh');
        },0,false);
      };

      if (attrs.ngOptions){
        var optionCollectionList = attrs.ngOptions.split('in ');
        var optionCollection = optionCollectionList[optionCollectionList.length - 1];

        scope.$watch(optionCollection, function() {
          element.selectpicker('refresh');
        });
      }

      attrs.$observe('disabled', function() {
        element.selectpicker('refresh');
      });
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

loDirectives.directive('loFocused', function($timeout) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      scope.$watch(attrs.loFocused, function(newValue) {
        $timeout(function () {
          if (newValue) {
            if (element[0].select) {
              element[0].select();
            }
            else {
              element[0].focus();
            }
          }
        });
      });
    }
  };
});

/*
  Directive for fields to be validated. It follows the Patternfly design pattern (setting the error class to relevant
  elements. A function can be set as an optional parameter. If the function is set, it's used as a basic validation
  function. You can turn on and off the validation with the loValidationEnabled attribute.
*/
loDirectives.directive('loValidation', function($timeout) {
  return {
    restrict: 'A',
    require: 'ngModel',
    scope: {
      loValidation: '&',
      loValidationEnabled: '='
    },
    link: function (scope, element, attrs, ctrl) {
      scope.inputCtrl = ctrl;
      scope.valEnabled = attrs.loValidationEnabled;

      scope.$watch('loValidationEnabled', function(newVal){
        scope.valEnabled = newVal;
        if (!newVal) {
          scope.inputCtrl.$setValidity('loValidation',true);
          toggleErrorClass(false);
        } else {
          validate();
        }
      });

      // If validation function is set
      if(attrs.loValidation) {
        // using $timeout(0) to get the actual $modelValue
        $timeout(function () {
          validate();
        }, 0);
      } else if (!scope.inputCtrl.$valid && scope.inputCtrl.$dirty){
        toggleErrorClass(true);
      }

      scope.$watch('inputCtrl.$valid', function(isValid){
        if (scope.inputCtrl.$dirty) {
          if (isValid) {
            toggleErrorClass(false);
          } else {
            toggleErrorClass(true);
          }
        }
      });

      function validate() {
        var val = scope.inputCtrl.$modelValue;

        var valid = !val || scope.loValidation({'input':val})  || val === '';
        if (scope.valEnabled && !valid){
          toggleErrorClass(true);
        }

        scope.$watch('inputCtrl.$modelValue', function(newVal){
          var valid = !newVal || scope.loValidation( {'input': newVal} ) || newVal === '';
          if (!valid){
            scope.inputCtrl.$setValidity('loValidation',false);
          } else {
            scope.inputCtrl.$setValidity('loValidation',true);
          }
        });
      }

      function toggleErrorClass(add) {
        var messageElement = element.next();
        var parentElement = element.parent();

        if (add && !parentElement.hasClass('has-error')){
          parentElement.addClass('has-error');
          messageElement.removeClass('ng-hide');
        }

        if (!add && parentElement.hasClass('has-error')){
          parentElement.removeClass('has-error');
          messageElement.addClass('ng-hide');
        }
      }
    }
  };
});