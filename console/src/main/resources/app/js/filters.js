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