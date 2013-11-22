/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
var LiveOak = function (host, port, secure, callback) {
    this._host = host;
    this._port = port;
    this._secure = secure;
    this.stomp_client = new Stomp.Client(host, port, secure);

}

LiveOak.prototype = {

    connect: function (callback) {
        this.stomp_client.connect(callback);
    },

    create: function (path, data) {
        console.debug("INSIDE CREATE");
        $.ajax(path, {
            type: "POST",
            data: JSON.stringify(data),
            contentType: 'application/json',
            dataType: 'json',
        });
    },

    read: function (path, callback) {
        $.ajax(path, {
            type: "GET",
            dataType: 'json',
            success: callback,
        });
    },

    update: function (path, data) {
    },

    delete: function (path) {
    },

    subscribe: function (path, callback) {
        this.stomp_client.subscribe(path, function (msg) {
            var data = JSON.parse(msg.body);
            callback(data);
        });
    }

}
