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

/* Collapsible forms - Apply the lo-collapse attribute the the <legend> element. Apply lo-collapse='true' to auto-hide
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
        // TODO: vrockai, review.. trying to make it usable outside forms (see applications / examples)
        element.nextAll().toggleClass('hidden');
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

loDirectives.directive('ngFileSelect', function() {
  return {
    link: function($scope,el){
      el.bind('change', function(e){
        $scope.file = (e.srcElement || e.target).files[0];
        $scope.getFile();
      });
    }
  };
});

/* Directive used to autoclose the advanced search pop-up when clicked outside the pop-up. */
loDirectives.directive('loAutoclose', function($document, $compile) {
  return {
    link: function(scope, e, att){

      var body = $document.find('body').eq(0);

      scope.close = function() {
        scope[att.ngShow] = false;
      };

      var backdrop = $compile('<div ng-if="' + att.ngShow + '" id="advancedBackdrop" ng-click="close()"></div>')(scope);

      body.append(backdrop);
    }
  };
});