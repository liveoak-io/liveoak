'use strict';

var loMod = angular.module('loApp.services', []).value('version', '0.1');

loMod.factory('Notifications', function($rootScope, $timeout, $log) {
  // time (in ms) the notifications are shown
  var delay = 5000;

  var notifications = {};

  $rootScope.notifications = {};
  $rootScope.notifications.data = [];

  $rootScope.notifications.remove = function(index){
    $rootScope.notifications.data.splice(index,1);
  };

  var scheduleMessagePop = function() {
    $timeout(function() {
      $rootScope.notifications.data.splice(0,1);
    }, delay);
  };

  if (!$rootScope.notifications) {
    $rootScope.notifications.data = [];
  }

  notifications.message = function(type, header, message) {
    $rootScope.notifications.data.push({
      type : type,
      header: header,
      message : message
    });

    scheduleMessagePop();
  };

  notifications.info = function(message) {
    notifications.message('info', 'Info!', message);
    $log.info(message);
  };

  notifications.success = function(message) {
    notifications.message('success', 'Success!', message);
    $log.info(message);
  };

  notifications.error = function(message) {
    notifications.message('danger', 'Error!', message);
    $log.error(message);
  };

  notifications.warn = function(message) {
    notifications.message('warning', 'Warning!', message);
    $log.warn(message);
  };

  notifications.httpError = function(message, httpResponse) {
    message += ' (' + (httpResponse.data.message || httpResponse.data.cause || httpResponse.data.cause || httpResponse.data.errorMessage) + ')';
    notifications.message('danger', 'Error!', message);
    $log.error(message);
  };

  return notifications;
});

/*
  FileReader service, taken from:
  http://odetocode.com/blogs/scott/archive/2013/07/03/building-a-filereader-service-for-angularjs-the-service.aspx
 */
loMod.factory('FileReader', function($q) {
  var onLoad = function(reader, deferred, scope) {
    return function () {
      scope.$apply(function () {
        deferred.resolve(reader.result);
      });
    };
  };

  var onError = function (reader, deferred, scope) {
    return function () {
      scope.$apply(function () {
        deferred.reject(reader.result);
      });
    };
  };

  var onProgress = function(reader, scope) {
    return function (event) {
      scope.$broadcast('fileProgress',
        {
          total: event.total,
          loaded: event.loaded
        });
    };
  };

  var getReader = function(deferred, scope) {
    var reader = new FileReader();
    reader.onload = onLoad(reader, deferred, scope);
    reader.onerror = onError(reader, deferred, scope);
    reader.onprogress = onProgress(reader, scope);
    return reader;
  };

  var readAsDataURL = function (file, scope) {
    var deferred = $q.defer();

    var reader = getReader(deferred, scope);
    reader.readAsText(file);

    return deferred.promise;
  };

  return {
    readAsDataUrl: readAsDataURL
  };
});

/* Loaders - Loaders are used in the route configuration as resolve parameters */
loMod.factory('Loader', function($q) {
  var loader = {};
  loader.get = function(service, id) {
    return function() {
      var i = id && id();
      var delay = $q.defer();
      service.get(i, function(entry) {
        delay.resolve(entry);
      }, function() {
        delay.reject('Unable to fetch ' + i);
      });
      return delay.promise;
    };
  };
  loader.getList = function(service, id) {
    return function() {
      var i = id && id();
      var delay = $q.defer();
      service.getList(i, function(entry) {
        delay.resolve(entry);
      }, function() {
        delay.reject('Unable to fetch ' + i);
      });
      return delay.promise;
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

loMod.factory('LoStorage', function($resource) {
  return $resource('/admin/applications/:appId/resources/:storageId', {
    appId : '@appId',
    storageId : '@storageId'
  }, {
    get : {
      method : 'GET'
    },
    getList : {
      method : 'GET',
      params: { expand : 'members' }
    },
    create : {
      method : 'POST',
      params : { appId : '@appId'}
    },
    update : {
      method : 'PUT',
      params : { appId : '@appId', storageId : '@storageId'}
    },
    delete : {
      method : 'DELETE',
      params : { appId : '@appId', storageId : '@storageId'}
    }
  });
});

loMod.factory('LoCollection', function($resource) {
  return $resource('/:appId/:storageId/:collectionId?expand=members', {
    appId : '@appId',
    storageId : '@storageId',
    collectionId : '@collectionId'
  }, {
    get : {
      method : 'GET',
      params: { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId'}
    },
    getList : {
      method : 'GET',
      params: { expand : 'members' }
    },
    create : {
      method : 'POST',
      params : { appId : '@appId', storageId : '@storageId'}
    },
    update : {
      method : 'PUT',
      params : { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId'}
    },
    delete : {
      method : 'DELETE',
      params : { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId'}
    }
  });
});

loMod.factory('LoCollectionItem', function($resource) {
  return $resource('/:appId/:storageId/:collectionId/:itemId', {
    appId : '@appId',
    storageId : '@storageId',
    collectionId : '@collectionId'
  }, {
    get : {
      method : 'GET',
      params: { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId', itemId: '@itemId' }
    },
    getList : {
      method : 'GET',
      params: { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId', expand : 'members' }
    },
    create : {
      method : 'POST',
      params : { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId'}
    },
    update : {
      method : 'PUT',
      params : { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId', itemId: '@itemId'}
    },
    delete : {
      method : 'DELETE',
      params : { appId : '@appId', storageId : '@storageId', collectionId: '@collectionId', itemId: '@itemid'}
    }
  });
});

loMod.factory('LoApp', function($resource) {
  return $resource('/admin/applications/:appId', {
    appId : '@appId'
  }, {
    get : {
      method : 'GET'
    },
    getList : {
      method : 'GET',
      params: { expand : 'members' }
    },
    create : {
      method : 'POST',
      url: '/admin/applications/'
    },
    save : {
      method : 'PUT',
      url: '/admin/applications/:appId'
    },
    addResource : {
      method : 'PUT',
      url: '/admin/applications/:appId/resources/:resourceId'
    }
  });
});

loMod.factory('LoStorageLoader', function(Loader, LoStorage, $route) {
  return Loader.get(LoStorage, function() {
    return {
      appId : $route.current.params.appId,
      storageId: $route.current.params.storageId
    };
  });
});

loMod.factory('LoStorageListLoader', function(Loader, LoStorage, $route) {
  return Loader.getList(LoStorage, function() {
    return {
      appId : $route.current.params.appId
    };
  });
});

loMod.factory('LoCollectionListLoader', function(Loader, LoCollection, $route) {
  return Loader.get(LoCollection, function() {
    return {
      appId : $route.current.params.appId,
      storageId : $route.current.params.storageId
    };
  });
});

loMod.factory('LoPushLoader', function(Loader, LoPush, $route) {
  return Loader.get(LoPush, function() {
      return {
        appId : $route.current.params.appId
      };
    },
    function(httpResponse) {
      console.log(httpResponse);
      return {
        appId : $route.current.params.appId
      };
    }
  );
});

loMod.factory('LoAppLoader', function(Loader, LoApp, $route) {
  return Loader.get(LoApp, function() {
    return {
      appId : $route.current.params.appId
    };
  });
});

loMod.factory('LoAppListLoader', function(Loader, LoApp) {
  return Loader.getList(LoApp);
});

loMod.factory('LoCollectionLoader', function(Loader, LoCollection) {
  return Loader.get(LoCollection);
});

loMod.factory('LoPush', function($resource) {
  return $resource('/admin/applications/:appId/resources/push', {
    appId : '@appId'
  }, {
    get : {
      method : 'GET',
      params : { appId : '@appId'}
    },
    update : {
      method : 'PUT',
      params : { appId : '@appId'}
    },
    create: {
      method : 'POST',
      url: '/admin/applications/:appId/resources/',
      params : { appId : '@appId'}
    },
    delete : {
      method : 'DELETE',
      params : { appId : '@appId'}
    }
  });
});

loMod.factory('LoRealmApp', function($resource) {
  return $resource('/auth/admin/realms/:realmId/applications/:appId', {
    realmId : 'liveoak-apps',
    appId: '@appId'
  }, {
    save: {
      method: 'PUT'
    },
    create: {
      method: 'POST'
    }
  });
});

loMod.factory('LoRealmAppRoles', function($resource) {
  return $resource('/auth/admin/realms/:realmId/applications/:appId/roles/:roleName', {
    realmId : 'liveoak-apps',
    appId: '@appId',
    roleName: '@roleName'
  });
});

loMod.factory('LoRealmRoles', function($resource) {
  return $resource('/auth/admin/realms/:realmId/roles', {
    realmId : 'liveoak-apps'
  });
});

loMod.factory('LoRealmClientRoles', function($resource) {
  return $resource('/auth/admin/realms/:realmId/applications/:appId/scope-mappings/realm', {
    realmId: 'liveoak-apps',
    appId: '@appId'
  });
});

loMod.factory('LoRealmAppClientScopeMapping', function($resource) {
  return $resource('/auth/admin/realms/:realmId/applications/:clientId/scope-mappings/applications/:appId', {
    realmId: 'liveoak-apps',
    appId : '@appId',
    clientId : '@clientId'
  });
});

loMod.factory('LoRealmAppClientScopeMappingLoader', function(Loader, LoRealmAppClientScopeMapping, $route) {
  return Loader.query(LoRealmAppClientScopeMapping, function() {
    return {
      realmId: 'liveoak-apps',
      appId: $route.current.params.appId,
      clientId: $route.current.params.clientId
    };
  });
});

loMod.factory('LoRealmAppLoader', function(Loader, LoRealmApp, $route) {
  return Loader.get(LoRealmApp, function() {
    return {
      realmId: 'liveoak-apps',
      appId : $route.current.params.appId
    };
  });
});

loMod.factory('LoRealmRolesLoader', function(Loader, LoRealmRoles) {
  return Loader.query(LoRealmRoles, function() {
    return {
      realmId: 'liveoak-apps'
    };
  });
});

loMod.factory('LoRealmAppListLoader', function(Loader, LoRealmApp) {
  return Loader.query(LoRealmApp, function() {
    return {
      realmId: 'liveoak-apps'
    };
  });
});

loMod.factory('LoRealmAppRolesLoader', function(Loader, LoRealmAppRoles, $route) {
  return Loader.query(LoRealmAppRoles, function() {
    return {
      realmId: 'liveoak-apps',
      appId : $route.current.params.appId
    };
  });
});

loMod.factory('LoRealmClientRolesLoader', function(Loader, LoRealmClientRoles, $route) {
  return Loader.query(LoRealmClientRoles, function() {
    return {
      realmId: 'liveoak-apps',
      appId : $route.current.params.appId
    };
  });
});


loMod.factory('LoSecurityCollections', function($resource) {
  return $resource('/:appId', {
    appId : '@appId'
  }, {
    get : {
      method: 'GET',
      params: { expand : 'members' }
    }
  });
});

loMod.factory('LoSecurityCollectionsLoader', function(Loader, LoSecurityCollections, $route) {
  return Loader.get(LoSecurityCollections, function() {
    return {
      appId : $route.current.params.appId
    };
  });
});

loMod.factory('LoSecurity', function($resource) {
  return $resource('/admin/applications/:appId/resources/uri-policy', {
    appId : '@appId'
  }, {
    create : {
      method: 'PUT'
    },
    save : {
      method: 'PUT'
    }
  });
});

loMod.factory('LoSecurityLoader', function(Loader, LoSecurity, $route) {
  return Loader.get(LoSecurity, function() {
    return {
      appId : $route.current.params.appId
    };
  });
});

loMod.factory('LoACL', function($resource) {
  return $resource('/admin/applications/:appId/resources/acl-policy', {
    appId : '@appId'
  }, {
    create : {
      method: 'PUT'
    },
    save : {
      method: 'PUT'
    }
  });
});

loMod.factory('LoACLLoader', function(Loader, LoACL, $route) {
  return Loader.get(LoACL, function() {
    return {
      appId : $route.current.params.appId
    };
  });
});
