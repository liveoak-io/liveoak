'use strict';

/* Filters */

var loFilters = angular.module('loApp.filters', []);

loFilters.filter('interpolate', ['version', function(version) {
  return function(text) {
    return String(text).replace(/\%VERSION\%/mg, version);
  };
}]);

// based on https://github.com/sparkalow/angular-truncate/blob/master/src/truncate.js

loFilters.filter('characters', function () {
  return function (input, chars, breakOnWord) {
    if (isNaN(chars)) { return input; }
    if (chars <= 0) { return ''; }
    if (input && input.length > chars) {
      input = input.substring(0, chars);

      if (!breakOnWord) {
        var lastspace = input.lastIndexOf(' ');
        //get last space
        if (lastspace !== -1) {
          input = input.substr(0, lastspace);
        }
      }
      else {
        while(input.charAt(input.length-1) === ' '){
          input = input.substr(0, input.length -1);
        }
      }
      return input + '...';
    }
    return input;
  };
});

loFilters.filter('words', function () {
  return function (input, words) {
    if (isNaN(words)) { return input; }
    if (words <= 0) { return ''; }
    if (input) {
      var inputWords = input.split(/\s+/);
      if (inputWords.length > words) {
        input = inputWords.slice(0, words).join(' ') + '...';
      }
    }
    return input;
  };
});

loFilters.filter('conditional', function () {
  return function (input, query, columns, conditions) {

    var defaultFilter = function(q, data) {

      var result = [];
      angular.forEach(data, function (line) {
        angular.forEach(line, function (cell) {
          if (cell.indexOf && cell.indexOf(q) > -1) {
            if (result.indexOf(line) === -1) {
              result.push(line);
            }
          }
        });
      });
      return result;
    };

    // If columns to filter or filtering conditions are missing, don't filter anything and return the original list
    if (!columns || columns.length === 0) {
      if (query) {
        return defaultFilter(query, input);
      } else {
        return input;
      }
    }

    var output = [];
    var includes = [];
    var excludes = [];

    // Generate includes & excludes sets from conditions
    angular.forEach(conditions, function(condition){
      if (condition.type === 'E' && condition.value !== '') {
        includes.push(condition.text);
      } else if (condition.value !== ''){
        excludes.push(condition.text);
      }
    });

    // Actual filtering
    angular.forEach(input, function(item) {
      var pass = false;
      angular.forEach(columns, function(column) {

        if (item[column]) {
          if ( query && query !== '' && item[column].indexOf(query) > -1) {
            pass = true;
            return;
          }
          if ( includes && ( includes.length > 0 && includes.indexOf(item[column]) > -1) ) {
            pass = true;
            return;
          }
          if ( excludes && ( excludes.length > 0 && excludes.indexOf(item[column]) < 0) ) {
            pass = true;
            return;
          }
        }
      });

      if (pass && output.indexOf(item) < 0){
        output.push(item);
      }

    });

    return output;
  };
});

loFilters.filter('clientname', function() {
  return function (clientName, appName) {
    return clientName ? clientName.replace(new RegExp('^liveoak.client.' + appName + '.'), '') : clientName;
  }
});

loFilters.filter('ures', function () {
  return function(item) {
    if (item) {
      var uris = [];
      for (var i = 0; i < item.length; i++) {
        for (var j = 0; j < item[i].rules.length; j++) {
          if(item[i].rules[j].uriPattern !== '*') {
            uris.push(item[i].rules[j].uriPattern.replace(/\/\*$/,''));
          }
        }
      }
      return uris;
    }
  }
});


/* taken from http://angular-ui.github.io/ui-utils/dist/ui-utils.js */
loFilters.filter('unique', ['$parse', function ($parse) {

  return function (items, filterOn) {

    if (filterOn === false) {
      return items;
    }

    if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
      var newItems = [],
        get = angular.isString(filterOn) ? $parse(filterOn) : function (item) { return item; };

      var extractValueToCompare = function (item) {
        return angular.isObject(item) ? get(item) : item;
      };

      angular.forEach(items, function (item) {
        var isDuplicate = false;

        for (var i = 0; i < newItems.length; i++) {
          if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
            isDuplicate = true;
            break;
          }
        }
        if (!isDuplicate) {
          newItems.push(item);
        }

      });
      items = newItems;
    }
    return items;
  };
}]);