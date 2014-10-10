'use strict';

/* jasmine specs for services go here */

describe('service', function() {
  beforeEach(module('loApp.services'));

  describe('version', function() {
    it('should return current version', inject(function(version) {
      expect(version).toEqual('0.1');
    }));
  });

  describe('loJSON service', function () {

    var stringSource = {
      stringValue: '\"stringValue\"',
      stringWithQuotes: '\"my\"quotes\"',
      stringWithSingleQuotes: '\"my\'single\"'
    };

    var stringJson = {
      stringValue: "stringValue",
        stringWithQuotes: "my\"quotes",
        stringWithSingleQuotes: "my\'single"
    };

    var numberSource = {
      floats: '123.321',
      integers: '123',
      integerWithZeroPrefix: '0123'
    };

    var numberSourceDecoded = {
      floats: '123.321',
      integers: '123',
      integerWithZeroPrefix: '123'
    };

    var numberJson = {
      floats: 123.321,
      integers: 123,
      integerWithZeroPrefix: 123
    };

    var booleanObject = {
      boolTrue: 'true',
      boolFalse: 'false'
    };

    var booleanJson = {
      boolTrue: true,
      boolFalse: false
    };

    var nullObject = {
      nullValue: 'null'
    };

    var nullJson = {
      nullValue: null
    };

    describe('parseJSON method', function () {

      it('should convert values in quotes into strings',  inject(function(loJSON) {
        expect(loJSON.parseJSON(stringSource)).toEqual(stringJson);
      }));

      it('should convert number strings into numbers',  inject(function(loJSON) {
        expect(loJSON.parseJSON(numberSource)).toEqual(numberJson);
      }));

      it('should convert boolean strings into booleans',  inject(function(loJSON) {
        expect(loJSON.parseJSON(booleanObject)).toEqual(booleanJson);
      }));

      it('should convert null strings into nulls',  inject(function(loJSON) {
        expect(loJSON.parseJSON(nullObject)).toEqual(nullJson);
      }));

      it('should ignore empty strings',  inject(function(loJSON) {
        var emptyStringObject = {
          emptyString: ''
        };

        expect(loJSON.parseJSON(emptyStringObject)).toEqual({});
      }));
    });

    describe('toString method', function () {
      it('should convert strings into values in quotes',  inject(function(loJSON) {
        expect(loJSON.toStringObject(stringJson)).toEqual(stringSource);
      }));

      it('should convert numbers into number strings',  inject(function(loJSON) {
        expect(loJSON.toStringObject(numberJson)).toEqual(numberSourceDecoded);
      }));

      it('should convert booleans into boolean strings',  inject(function(loJSON) {
        expect(loJSON.toStringObject(booleanJson)).toEqual(booleanObject);
      }));

      it('should convert nulls into null strings',  inject(function(loJSON) {
        expect(loJSON.toStringObject(nullJson)).toEqual(nullObject);
      }));
    });

    describe('isValidJSON method', function () {
      it('should be true for valid JSON strings',  inject(function(loJSON) {
        expect(loJSON.isValidJSON('{}')).toBe(true);
        expect(loJSON.isValidJSON('true')).toBe(true);
        expect(loJSON.isValidJSON('"foo"')).toBe(true);
        expect(loJSON.isValidJSON('[1, 5, "false"]')).toBe(true);
        expect(loJSON.isValidJSON('null')).toBe(true);
        expect(loJSON.isValidJSON('{"1": 1, "2": 2,"3": {"4": 4, "5": {"6": 6}}}')).toBe(true);
      }));

      it('should be false for invalid JSON objects',  inject(function(loJSON) {
        expect(loJSON.isValidJSON('')).toBe(false);
        expect(loJSON.isValidJSON('scorpius')).toBe(false);
        expect(loJSON.isValidJSON('7^2')).toBe(false);
        expect(loJSON.isValidJSON('{a:b}')).toBe(false);
      }));
    });

  });

});
