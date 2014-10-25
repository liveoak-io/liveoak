'use strict';

angular.module('rhqm.directives', ['ui.bootstrap', 'd3']);
angular.module('rhqm.services', ['ngStorage']);

var loMod = angular.module('loApp.controllers.dashboard', ['rhqm.directives', 'rhqm.services']);

loMod.controller('DashboardCtrl', function($scope, $rootScope, $routeParams, $filter, $location, $http, currentApp, loStorageList) {

  $rootScope.curApp = currentApp;

  $scope.appUrl = $location.protocol() + '://' + $location.host() + ':' + $location.port() + '/' + currentApp.id + '/';

  $scope.storageList = [];

  /* jshint unused: false */
  angular.forEach(loStorageList.members, function(value, key) {
    if (value.hasOwnProperty('db')) {
      this.push(value.id);
    }
  }, $scope.storageList);
  /* jshint unused: true */

  var pushEntries = $filter('filter')(loStorageList.members, {'id': 'push'});
  $scope.pushConfig = pushEntries && pushEntries.length > 0 ? pushEntries[0] : undefined;

  $scope.formatDate = function(millis) {
    return new Date(millis);
  };

  $scope.breadcrumbs = [
    {'label': 'Applications',  'href':'#/applications'},
    {'label': currentApp.name, 'href':'#/applications/' + currentApp.id},
    {'label': 'Dashboard',     'href':'#/applications/' + currentApp.id + '/dashboard'}
  ];

  $scope.dataPeriods = [ 'Hour', 'Day', 'Week', 'Month' ];
  $scope.range = 'Hour';


  var millisForRange = {};
  millisForRange.Hour = 60 * 60 * 1000;
  millisForRange.Day = 24 * millisForRange.Hour;
  millisForRange.Week = 7 * millisForRange.Day;
  millisForRange.Month = 30 * millisForRange.Week;


  // FIXME: Fetch from rest. Mock data.
  var rawData = {
    metrics: {
      'Hour': {requests: 0, users: 0, notifications: 0, storage: 0.0},
      'Day': {requests: 0, users: 0, notifications: 0, storage: 0.0},
      'Week': {requests: 0, users: 0, notifications: 0, storage: 0.0},
      'Month': {requests: 0, users: 0, notifications: 0, storage: 0.0}
    },
    units: {
      requests: { unit: 'API Requests', per: 'second' },
      users: { unit: 'Active Users', per: 'second' },
      notifications: { unit: 'Notifications', per: 'second' },
      storage: { unit: 'KB', per: 'second' }
    }
  };

  $scope.displayData = {
    'Hour': {
      'requests': { value: 0, unit: 'API Requests', per: 'hour' },
      'users': { value: 0, unit: 'Active Users', per: 'hour' },
      'notifications': { value: 0, unit: 'Notifications', per: 'hour' },
      'storage': { value: 0, unit: 'KB', per: 'hour' }
    },
    'Day': {
      'requests': { value: 0, unit: 'API Requests', per: 'hour' },
      'users': { value: 0, unit: 'Active Users', per: 'hour' },
      'notifications': { value: 0, unit: 'Notifications', per: 'hour' },
      'storage': { value: 0, unit: 'KB', per: 'hour' }
    },
    'Week': {
      'requests': { value: 0, unit: 'API Requests', per: 'hour' },
      'users': { value: 0, unit: 'Active Users', per: 'hour' },
      'notifications': { value: 0, unit: 'Notifications', per: 'hour' },
      'storage': { value: 0, unit: 'KB', per: 'hour' }
    },
    'Month': {
      'requests': { value: 0, unit: 'API Requests', per: 'hour' },
      'users': { value: 0, unit: 'Active Users', per: 'hour' },
      'notifications': { value: 0, unit: 'Notifications', per: 'hour' },
      'storage': { value: 0, unit: 'KB', per: 'hour' }
    }
  };


  $scope.rawToChartData = function(range, metricId) {
    if (!$scope.chartData[range]) {
      throw 'Invalid range: ' + range;
    }
    if (!$scope.chartData[range][metricId]) {
      throw 'Unknown metric for range (' + range + '): ' + metricId;
    }

    var rawData = $scope.chartData[range][metricId].rawData;
    rawData = rawData._default;

    var chartData = [];
    for (var i in rawData) {
      var bucket = rawData[i];
      var data = {};
      data.timestamp = bucket.timestamp;
      data.value = 0;
      data.id = bucket.id;
      data.tags = null;
      data.min = 0;
      if (metricId === 'bandwidth') {
        data.max = bucket.sum;
        data.avg = bucket.sum;
      } else {
        data.max = bucket.count;
        data.avg = bucket.count;
      }
      chartData.push(data);
    }
    $scope.chartData[range][metricId].chartData = chartData;
  };

  var metricLabels = ['requests', 'notifications', 'bandwidth'];


  $scope.chartData = {};


  // initChartData
  for (var i in $scope.dataPeriods) {  // 'Hour', 'Day', 'Week', 'Month'
    var range = $scope.dataPeriods[i];
    var rangeObj = {};
    $scope.chartData[range] = rangeObj;

    for (var j in metricLabels) { // "requests", "notifications", "bandwidth"
      var metric = metricLabels[j];
      var metricObj = {};
      rangeObj[metric] = metricObj;

      metricObj.chartType = 'bar'; // line, bar, area, scatterline, some day also pie?

      // example what rhq-metrics/event-log endpoint returns
      // { '_default': [
      //  {'timestamp':1422450651739,'value':0,'id':'requests','min':0,'max':1,'avg':1,'duration':60000,'count':2,'speed':0.3333333333333333,'sum':2},
      //  ]
      // }
      metricObj.rawData = {};

      // example what rhqm-chart expects
      // [
      //   {'timestamp':1422270926503,'value':0.0,'id':'mem','tags':null,'min':0.0,'max':9.0,'avg':8.0},
      //   {'timestamp':1422272366503,'value':0.0,'id':'mem','tags':null,'min':0.0,'max':15.0,'avg':12.0}
      // ]
      metricObj.chartData = [];
    }
  }



  $scope.logData = {};


  $scope.rangeSelected = function(newRange) {
    console.log('current range: ' + $scope.range);
    console.log('new range: ' + newRange);
    if ($scope.range !== newRange) {
      initMetrics(newRange);
    }
  };

  var timeConversions = [
    { per: 'second', factor: 1 },
    { per: 'minute', factor: 60 },
    { per: 'hour', factor: 60 },
    { per: 'day', factor: 24}
  ];

/*
  function addChartData(metricId, start, end) {
    $scope.chartData[metricId] = {
      id: metricId,
      start: start,
      end: end,
      dataPoints: fetchMetrics({
          start: start,
          end: end,
          id: metricId,
          buckets: 1
        },
        function(data) {
          rawData.metricsSeries[metricId][range] = data._default[0].speed;
          $scope.displayData[range].notifications = bestValueWithUnitForMetric(metricId, range);
        }
      )
    }
  }
*/

  function valueForMetric(metric, period) {
    var val = rawData;
    val = val ? val.metrics : null;
    val = val ? val[period] : null;
    val = val ? val[metric] : null;

    if (!val) {
      return 0;
    }

    return val;
  }

  function bestValueWithUnitForMetric(metric, period) {
    var val = valueForMetric(metric, period);

    //
    // rules:
    //
    //  if the value is less than 1 we try to increase the unit:
    //    request / s -> requests / min -> request / hour -> request / day
    //                  x60               x60               x24
    //
    //  we round by 2 decimal places
    //
    var unitName = rawData.units[metric].unit;
    var unitPer = rawData.units[metric].per;
    var found;

    if (val < 1 && val > 0) {
      for (var i = 0; i < timeConversions.length; i++) {
        var el = timeConversions[i];
        if (found) {
          val *= el.factor;
          unitPer = el.per;
          if (val >= 1) {
            break;
          }
        } else if (el.per === unitPer) {
          found = el.per;
        }
      }
    }

    //if (val >= 1) {
      val = Math.round(val * 100) / 100;
      return { 'value': val, 'unit': unitName, 'per': unitPer };
    //}
  }

  /*
  var calculateForDisplay = function() {
    displayData.Hour.requests = bestValueWithUnitForMetric('requests', 'Hour');
    displayData.Hour.users = bestValueWithUnitForMetric('users', 'Hour');
    displayData.Hour.notifications = bestValueWithUnitForMetric('notifications', 'Hour');
    displayData.Hour.storage = bestValueWithUnitForMetric('storage', 'Hour');
  }
  */

  function fetchMetrics(query, success) {
    var q = 'tag=path:/' + currentApp.id;
    if (query.start) {
      q += '&start=' + query.start;
    }
    if (query.end) {
      q += '&end=' + query.end;
    }
    if (query.buckets) {
      q += '&buckets=' + query.buckets;
    }
    if (query.id) {
      q += '&id=' + query.id;
    }
    if (query.desc) {
      q += '&desc';
    }
    if (query.max) {
      q += '&limit=' + query.max;
    }

    console.log('GET /rhq-metrics/event-log?' + q);

    $http( {
      method: 'GET',
      url: '/rhq-metrics/event-log?' + q
    })
    .success(success)
    .error(function (httpResponse, status) {
      console.error('Failed to retreive metrics for ' + currentApp.id + ' (query: ' + query + ')');
    });
  }

  function fetchTime(success) {
    $http( {
      method: 'GET',
      url: '/rhq-metrics/time'
    })
    .success(function(data) {
      $scope.timediff = data.time - new Date().getTime();
      success.apply();
    })
    .error(function() {
      console.log('Failed to fetch server time!');
    });
  }

  function initMetrics(range) {
    // fetch Dashboard metrics
    var now = new Date().getTime() + $scope.timediff;
    var start = now - millisForRange[range];

    fetchMetrics({
          start: start,
          end: now,
          id: 'requests',
          buckets: 1
        },
        function(data) {
          rawData.metrics[range].requests = data._default[0].speed;
          $scope.displayData[range].requests = bestValueWithUnitForMetric('requests', range);
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'requests',
          buckets: 60
        },
        function(data) {
          $scope.chartData[range].requests.rawData = data;
          $scope.rawToChartData(range, 'requests');
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'unique-users',
          buckets: 1
        },
        function(data) {
          rawData.metrics[range].users = data._default[0].speed;
          $scope.displayData[range].users = bestValueWithUnitForMetric('users', range);
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'bandwidth',
          buckets: 1
        },
        function(data) {
          rawData.metrics[range].storage = data._default[0].speed / 1024;
          $scope.displayData[range].storage = bestValueWithUnitForMetric('storage', range);
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'bandwidth',
          buckets: 60
        },
        function(data) {
          $scope.chartData[range].bandwidth.rawData = data;
          $scope.rawToChartData(range, 'bandwidth');
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'notifications',
          buckets: 1
        },
        function(data) {
          rawData.metrics[range].notifications = data._default[0].speed;
          $scope.displayData[range].notifications = bestValueWithUnitForMetric('notifications', range);
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'notifications',
          buckets: 60
        },
        function(data) {
          $scope.chartData[range].notifications.rawData = data;
          $scope.rawToChartData(range, 'notifications');
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'errors',
          desc: '',
          limit: 5
        },
        function(data) {
          $scope.logData.errors = data;
        }
    );

    fetchMetrics({
          start: start,
          end: now,
          id: 'requests-by-path',
          buckets: 1,
          limit: 5
        },
        function(data) {
          $scope.logData.topPathsRequested = data;
        }
    );
  }

  // init time diff
  fetchTime(function() {
    initMetrics($scope.range);
  });

});