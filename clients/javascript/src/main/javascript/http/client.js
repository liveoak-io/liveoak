var Http = function (options) {
    options = options || {};

    var baseUrl = (options.secure ? 'https://' : 'http://') + options.host;
    if (options.port) {
        baseUrl += ':' + options.port;
    }

    var instance = this;

    this.create = function (path, data, options) {
        var url;
        var method;

        if ( typeof( data.id ) === 'undefined' ) {
          url = createUrl( path, {} );
          method = 'POST';
        } else {
          url = createUrl( path + '/' + data.id, {} );
          method = 'PUT';
        }
        request(method, url, data, function (data) {
            options.success(data);
        }, options.error);
    }

    this.read = function (path, options) {
        var url = createUrl(path, { query: options.query });
        request('GET', url, null, function (data) {
            options.success(data);
        }, options.error);
    }

    this.readMembers = function (path, options) {
        var url = createUrl(path, { expand: '*',  query: options.query, sort: options.sort });
        request('GET', url, null, function (data) {
            var members = data._members || [];
            options.success(members);
        }, options.error);
    }

    this.save = function (path, data, options) {
        var url = createUrl(path, {});
        request('POST', url, data, function (data) {
            options.success(data);
        }, options.error);
    }

    this.update = function (path, data, options) {
        var url = createUrl(path + '/' + data.id, {});
        request('PUT', url, data, function (data) {
            options.success(data);
        }, options.error);
    }

    this.remove = function (path, data, options) {
        var url = createUrl(path + '/' + data.id, {});
        request('DELETE', url, null, function (data) {
            options.success(data);
        }, options.error);
    }

    var request = function (method, url, data, success, error) {
        var req = new XMLHttpRequest();
        req.open(method, url, true);

        req.setRequestHeader('Content-type', 'application/json');
        req.setRequestHeader('Accept', 'application/json');

        var token = instance.getToken && instance.getToken();
        if (token) {
            req.setRequestHeader('Authorization', 'bearer ' + token);
        }

        req.onreadystatechange = function () {
            if (req.readyState == 4) {
                if (req.status == 200 || req.status == 201) {
                    if (success) {
                        var response;
                        if (req.responseText) {
                            response = JSON.parse(req.responseText);
                        } else {
                            response = {};
                        }
                        success(response);
                    }
                } else {
                    if (error) {
                        var response = { status: req.status, statusText: req.statusText };
                        if (req.responseText) {
                            response.data = JSON.parse(req.responseText);
                        }
                        error(response);
                    }
                }
            }
        }

        if (data) {
            req.send(JSON.stringify(data, jsonReplacer));
        } else {
            req.send();
        }
    }

    var createUrl = function (path, params) {
        var url = baseUrl;
        url += path;

        var query = '';

        if (params.expand) {
            if (query) {
                query += '&';
            }
            query += 'expand=' + params.expand;
        }
        if (params.fields) {
            if (query) {
                query += '&';
            }
            query += 'fields=' + params.fields;
        }
        if (params.query) {
            if (query) {
                query += '&';
            }
            query += 'q=' + encodeURIComponent(JSON.stringify(params.query));
        }
        if (params.sort) {
            if (query) {
                query += '&';
            }
            query += 'sort=' + params.sort;
        }

        if (query != '') {
            url += '?' + query;
        }

        return url;
    }

    var jsonReplacer = function (key, value) {
        switch (key) {
            case '_members': return undefined;
            case 'self': return undefined;
            default: return value;
        }
    }

}
