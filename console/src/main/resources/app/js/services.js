'use strict';

var loMod = angular.module('loApp');

/* Services */
loMod.factory('Current', function() {
  var appName = 'My App';

  return {
    setApp: function(app){
      appName = app;
    },
    name: appName
  };
});

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('loApp.services', []).
  value('version', '0.1');

angular.module('services.breadcrumbs', []);
angular.module('services.breadcrumbs').factory('breadcrumbs', ['$rootScope', '$location', function($rootScope, $location){

  var breadcrumbs = [];
  var breadcrumbsService = {};

  //we want to update breadcrumbs only when a route is actually changed
  //as $location.path() will get updated immediately (even if route change fails!)
  $rootScope.$on('$routeChangeSuccess', function(){

    var pathElements = $location.path().split('/'), result = [], i;
    var breadcrumbPath = function (index) {
      return '/' + (pathElements.slice(0, index + 1)).join('/');
    };

    pathElements.shift();
    for (i = 0; i < pathElements.length; i++) {
      result.push({name: pathElements[i], path: breadcrumbPath(i)});
    }

    breadcrumbs = result;
  });

  breadcrumbsService.getAll = function() {
    return breadcrumbs;
  };

  breadcrumbsService.getFirst = function() {
    return breadcrumbs[0] || {};
  };

  return breadcrumbsService;
}]);

loMod.factory('Notifications', function($rootScope, $timeout) {
  // time (in ms) the notifications are shown
  var delay = 5000;

  var notifications = {};

  var scheduled = null;
  var schedulePop = function() {
    if (scheduled) {
      $timeout.cancel(scheduled);
    }

    scheduled = $timeout(function() {
      $rootScope.notification = null;
      scheduled = null;
    }, delay);
  };

  if (!$rootScope.notifications) {
    $rootScope.notifications = [];
  }

  notifications.message = function(type, header, message) {
    $rootScope.notification = {
      type : type,
      header: header,
      message : message
    };

    schedulePop();
  };

  notifications.info = function(message) {
    notifications.message('info', 'Info!', message);
  };

  notifications.success = function(message) {
    notifications.message('success', 'Success!', message);
  };

  notifications.error = function(message) {
    notifications.message('danger', 'Error!', message);
  };

  notifications.warn = function(message) {
    notifications.message('warning', 'Warning!', message);
  };

  return notifications;
});

/* TODO - This is only a mock service. The actual implementation would be created once the REST endpoints are known.*/
loMod.factory('LoStorage', function() {
//loMod.factory('LoStorage', function($resource) {
  return {'get': function(){return {dbtype:'mongo', name:'asd', url:'http://www.url.com/path', host:'localhost', port: 1234.8};}};
  /*
  return $resource('/admin/applications/:appId/resources/storage', {
    id : '@realm'
  }, {
    update : {
      method : 'PUT'
    },
    create : {
      method : 'POST',
      params : { appId : ''}
    }
  });
  */
});

/* Loaders - Loaders are used in the route configuration as resolve parameters */
loMod.factory('Loader', function($q) {
  var loader = {};
  loader.get = function(service, id) {
    return function() {
      /* TODO - This is only a mock method. Update with actual data loading from REST endpoints. */
      return service.get(id);
      /*
      var i = id && id();
      var delay = $q.defer();
      service.get(i, function(entry) {
        delay.resolve(entry);
      }, function() {
        delay.reject('Unable to fetch ' + i);
      });
      return delay.promise;
      */
    };
  };
  loader.query = function(service, id) {
    return function() {
      var i = id && id();
      var delay = $q.defer();
      service.query(i, function(entry) {
        delay.resolve(entry);
      }, function() {
        delay.reject('Unable to fetch ' + i);
      });
      return delay.promise;
    };
  };
  return loader;
});

loMod.factory('LoStorageLoader', function(Loader, LoStorage) {
  return Loader.get(LoStorage);
});