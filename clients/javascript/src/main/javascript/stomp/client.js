/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

// Stilts stomp-client.js ${project.version}
// Some parts (c) 2010 Jeff Mesnil -- http://jmesnil.net/
// ${project.version}

var Stomp = {

    Headers: {
        HOST: 'host',
        CONTENT_LENGTH: 'content-length',
        CONTENT_TYPE: 'content-type',
        ACCEPT_VERSION: 'accept-version',
        VERSION: 'version'
    },

    Transport: {

    },

    Transports: [],

    unmarshal: function (data) {
        var divider = data.search(/\n\n/);
        var headerLines = data.substring(0, divider).split('\n');
        var command = headerLines.shift(), headers = {}, body = '';

        // Parse headers
        var line = idx = null;
        for (var i = 0; i < headerLines.length; i++) {
            line = '' + headerLines[i];
            idx = line.indexOf(':');
            headers[line.substring(0, idx).trim()] = line.substring(idx + 1).trim();
        }
        try {
            if (headers[Stomp.Headers.CONTENT_LENGTH]) {
                var len = parseInt(headers[Stomp.Headers.CONTENT_LENGTH]);
                var start = divider + 2;
                // content-length is bytes, substring operates on characters
                body = Stomp.bytes_to_chars(Stomp.chars_to_bytes('' + data).substring(start, start + len))
            } else {
                // Parse body, stopping at the first \0 found.
                var chr = null;
                for (var i = divider + 2; i < data.length; i++) {
                    chr = data.charAt(i);
                    if (chr === '\0') {
                        break;
                    }
                    body += chr;
                }
            }
            return Stomp.frame(command, headers, body);
        } catch (err) {
            return Stomp.frame('ERROR', headers, "Error parsing frame: " + err.description);
        }
    },

    marshal: function (command, headers, body) {
        var frame = Stomp.frame(command, headers, body);
        return frame.toString() + '\0';
    },

    frame: function (command, headers, body) {
        return {
            command: command,
            headers: headers,
            body: body,
            toString: function () {
                var out = command + '\n';
                if (headers) {
                    for (header in headers) {
                        if (header != 'content-length' && headers.hasOwnProperty(header)) {
                            out = out + header + ':' + headers[header] + '\n';
                        }
                    }
                }
                if (body) {
                    out = out + 'content-length:' + Stomp.chars_to_bytes(body).length + '\n';
                }
                out = out + '\n';
                if (body) {
                    out = out + body;
                }
                return out;
            }
        }
    },

    chars_to_bytes: function (chars) {
        return unescape(encodeURIComponent(chars));
    },

    bytes_to_chars: function (bytes) {
        return decodeURIComponent(escape(bytes));
    },

    logger: (function () {
        if (typeof(console) == 'undefined') {
            return { log: function () {
            }, debug: function () {
            } };
        } else {
            return console;
        }
    })()

};

Stomp


Stomp.Client = function (host, port, secure) {
    this._host = host || Stomp.DEFAULT_HOST;
    this._port = port || Stomp.DEFAULT_PORT || 8080;
    this._secure = secure || Stomp.DEFAULT_SECURE_FLAG || false;
}

Stomp.Client.prototype = {

    Versions: {
        VERSION_1_0: "1.0",
        VERSION_1_1: "1.1"

    },

    supportedVersions: function () {
        return "1.0,1.1";
    },

    connect: function () {
        if (arguments.length == 1) {
            this._connectCallback = arguments[0];
        }
        if (arguments.length == 2) {
            this._connectCallback = arguments[0];
            this._errorCallback = arguments[1];
        }
        if (arguments.length == 3) {
            this._login = arguments[0];
            this._passcode = arguments[1];
            this._connectCallback = arguments[2];
        }
        if (arguments.length == 4) {
            this._login = arguments[0];
            this._passcode = arguments[1];
            this._connectCallback = arguments[2];
            this._errorCallback = arguments[3];
        }

        if (this._transport === undefined) {
            this._connectTransport(this._connectCallback);
        } else {
            this._connectCallback();
        }

    },

    _connectTransport: function (callback) {
        var transports = [];
        for (i = 0; i < Stomp.Transports.length; ++i) {
            var t = new Stomp.Transports[i](this._host, this._port, this._secure);
            t.client = this;
            if (this._login && this._passcode) {
              t.setAuth(this._login, this._passcode);
            }
            transports.push(t);
        }

        this._buildConnector(transports, 0, callback)();
    },


    _buildConnector: function (transports, i, callback) {
        var client = this;
        if (i + 1 < transports.length) {
            return function () {
                var fallback = client._buildConnector(transports, i + 1, callback);
                try {
                    transports[i].connect(function () {
                        client._transport = transports[i];
                        callback();
                    }, fallback);
                } catch (err) {
                    fallback();
                }
            };
        } else if (i < transports.length) {
            return function () {
                var fallback = client.connectionFailed.bind(this);
                try {
                    transports[i].connect(function () {
                        client._transport = transports[i];
                        callback();
                    }, fallback);
                } catch (err) {
                    fallback();
                }
            };
        } else {
            return function () {
                client.connectionFailed(this);
            };
        }
    },

    connectionFailed: function () {
        Stomp.logger.log("error: unable to connect");
    },

    disconnect: function (disconnectCallback) {
        this._transmit("DISCONNECT");
        this._transport.close();
        if (disconnectCallback) {
            disconnectCallback();
        }
    },

    send: function (destination, headers, body) {
        var headers = headers || {};
        headers.destination = destination;
        this._transmit("SEND", headers, body);
    },

    subscribe: function (destination, callback, headers) {
        var headers = headers || {};
        var subscription_id = "sub-" + this._counter++;
        headers.destination = destination;
        headers.id = subscription_id;
        this._subscriptions['' + subscription_id] = callback;
        this._transmit("SUBSCRIBE", headers);
        return subscription_id;
    },

    unsubscribe: function (id, headers) {
        var headers = headers || {};
        headers.id = id;
        delete this._subscriptions[id];
        this._transmit("UNSUBSCRIBE", headers);
    },

    begin: function (transaction, headers) {
        var headers = headers || {};
        headers.transaction = transaction;
        this._transmit("BEGIN", headers);
    },

    commit: function (transaction, headers) {
        var headers = headers || {};
        headers.transaction = transaction;
        this._transmit("COMMIT", headers);
    },

    abort: function (transaction, headers) {
        var headers = headers || {};
        headers.transaction = transaction;
        this._transmit("ABORT", headers);
    },

    ack: function (message_id, headers) {
        var headers = headers || {};
        headers["message-id"] = message_id;
        this._transmit("ACK", headers);
    },

    nack: function (message_id, headers) {
        // TODO: Add nack functionality.
    },

    // ----------------------------------------
    processMessage: function (message) {
        if (message.command == "MESSAGE") {
            var subId = message.headers['subscription'];
            var callback = this._subscriptions[ subId ];
            callback(message);
        } else if (message.command == "RECEIPT" && this.onreceipt) {
            this.onreceipt(message);
        } else if (message.command == "ERROR" && this.onerror) {
            this.onerror(message);
        }
    },
    // ----------------------------------------

    _login: undefined,
    _passcode: undefined,
    _connectCallback: undefined,
    _errorCallback: undefined,
    _webSocketEnabled: true,
    _longPollEnabled: true,

    _transport: undefined,
    _subscriptions: {},
    _counter: 0,

    _transmit: function (command, headers, body) {
        this._transport.transmit(command, headers, body);
    }

}
