/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
/*
 example:
 var options = { host: 'http://127.0.0.1', port: 8080 };
 var liveOak = LiveOak( options );
*/
var LiveOak = function( options ) {
    options = options || {};

    // Allow instantiation without using new
    if(!(this instanceof LiveOak)) {
        return new LiveOak( options );
    }

    // Use address of liveoak.js if server address not specified
    if (!options.host) {
        var server = parseScriptUrl();
        options.host = server.host;
        options.port = server.port;
        options.secure = server.secure;
    }

    var http = new Http(options);
    var auth;
    var stomp_client = new Stomp.Client( options.host, options.port, options.secure );

    this.connect = function( callback ) {
      // TODO: Better way to do this...
      if (arguments.length == 1) {
         stomp_client.connect( arguments[0] );
      }
      if (arguments.length == 2) {
        stomp_client.connect( arguments[0], arguments[1] );
      }
      if (arguments.length == 3) {
        stomp_client.connect( arguments[0], arguments[1], arguments[2] );
      }
      if (arguments.length == 4) {
        stomp_client.connect( arguments[0], arguments[1], arguments[2], arguments[3] );
      }
    };

    this.onStompError = function( callback ) {
        stomp_client.onerror = callback;
    }

    this.create = http.create;
    this.read = http.read;
    this.readMembers = http.readMembers;
    this.save = http.save;
    this.update = http.update;
    this.remove = http.remove;

    this.subscribe = function( path, callback ) {
        var id = stomp_client.subscribe( path, function(msg) {
            var data = JSON.parse( msg.body );
            callback( data, msg.headers.action );
        });
        return id;
    };

    this.unsubscribe = function( id, headers ) {
        stomp_client.unsubscribe( id, headers );
    };

    if (options.clientId) {
        options.auth = options.auth || {};
        options.auth.clientId = options.clientId;
    }

    if (options.auth) {
        if (!options.auth.realm) {
            options.auth.realm = 'liveoak-apps';
        }

        if (!options.auth.url) {
            options.auth.url = (options.secure ? 'https://' : 'http://') + options.host + (options.port ? ':' + options.port : '') + '/auth';
        }

        auth = new Keycloak(options.auth);
        this.auth = auth;

        http.getToken = function() {
            return auth.token;
        }

        this.getAuthServerUrl = function() {
            return auth.authServerUrl;
        }
    }

    function parseScriptUrl() {
        var scripts = document.getElementsByTagName('script');
        for (var i = 0; i < scripts.length; i++)  {
            if (scripts[i].src.match(/.*liveoak\.js/)) {
                var parts = scripts[i].src.split('/');
                var server = {};
                if (parts[2].indexOf(':') == -1) {
                    server.host = parts[2];
                } else {
                    server.host = parts[2].substring(0, parts[2].indexOf(':'));
                    server.port = parseInt(parts[2].substring(parts[2].indexOf(':') + 1));
                }
                if (parts[0] == 'https') {
                    server.secure = true;
                }
                return server;
            }
        }
    }
};
