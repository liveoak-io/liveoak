'use strict';

/* jasmine specs for filters go here */

describe('filter', function() {
  beforeEach(module('loApp.filters'));

  describe('interpolate', function() {
    beforeEach(module(function($provide) {
      $provide.value('version', 'TEST_VER');
    }));

    it('should replace VERSION', inject(function(interpolateFilter) {
      expect(interpolateFilter('before %VERSION% after')).toEqual('before TEST_VER after');
    }));
  });

  describe('Conditional filter', function() {
    it('should return input if no conditions are specified', inject(function(conditionFilter) {
      var input = [1,2,3];
      expect(conditionFilter(input)).toEqual(input);
      expect(conditionFilter(input), {}).toEqual(input);
    }));

    it('should return only fields specified by equals condition', inject(function(conditionFilter) {
      var input = [{id: 1},{id: 123}, {id: 3}];

      var conditions1 = [{column:'id', type:'E', text:123}];
      expect(conditionFilter(input, conditions1)).toEqual([{id:123}]);

      var conditions2 = [{column:'id', type:'E', text:1}];
      expect(conditionFilter(input, conditions2)).toEqual([{id:1}]);
    }));

    it('should return only fields specified by not equals condition', inject(function(conditionFilter) {
      var input = [{id: 1},{id: 123}, {id: 3}];

      var conditions1 = [{column:'id', type:'N', text:123}];
      expect(conditionFilter(input, conditions1)).toEqual([{id:1}, {id:3}]);

      var conditions2 = [{column:'id', type:'N', text:1}];
      expect(conditionFilter(input, conditions2)).toEqual([{id:123}, {id:3}]);
    }));

    it('should return only fields specified by both equals and not equals conditions', inject(function(conditionFilter) {
      var input = [
        {id: 123, name:'viliam', data: 'data1'},
        {id: 123, name:'alex', data: 'data2'},
        {id: 321, name:'alex', data: 'data3'},
        {id: 321, name: 'viliam', data: 'data4'}];

      var conditions = [{column:'name', type:'N', text:'viliam'}, {column:'id', type:'E', text:321}];
      expect(conditionFilter(input, conditions)).toEqual([{id: 321, name:'alex', data: 'data3'}]);
    }));
  });

  describe('DataContains filter', function() {
    it('should return only fields specified by the query', inject(function(dataContainsFilter) {
      var input = [
        {id: 123, name:'viliam', data: 'data1'},
        {id: 123, name:'alex', data: 'data2'},
        {id: 321, name:'alex', data: 'data3'},
        {id: 321, name: 'viliam', data: 'data4'}];

      var query = 'alex';
      expect(dataContainsFilter(input, query)).toEqual([{id: 123, name:'alex', data: 'data2'}, {id: 321, name:'alex', data: 'data3'}]);
    }));
  });
});
