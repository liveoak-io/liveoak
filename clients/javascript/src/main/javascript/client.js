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
    if(!this instanceof LiveOak) {
        return LiveOak( options );
    }
    var stomp_client = new Stomp.Client( options.host, options.port, options.secure );

    this.connect = function( callback ) {
        stomp_client.connect( callback );
    };

    this.create = function( path, data, options ) {
        options = options || {};
        $.ajax( path, {
            type: 'POST',
            data: JSON.stringify( data ),
            contentType: 'application/json',
            dataType: 'json',
            successs: options.success,
            error: options.error
        });
    };

    this.read = function( path, options ) {
        options = options || {};
        $.ajax( path, {
            type: 'GET',
            dataType: 'json',
            success: options.success,
            error: options.error
        });
    };

    this.subscribe = function( path, callback ) {
        stomp_client.subscribe( path, function(msg) {
            var data = JSON.parse( msg.body );
            callback( data );
        });
    };
};
