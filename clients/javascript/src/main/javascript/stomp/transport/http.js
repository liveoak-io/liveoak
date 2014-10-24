/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

// ----------------------------------------
// HTTP Transport
// ----------------------------------------

Stomp.Transport.HTTP = function (host, port, secure) {
    this._host = host;
    this._port = port;
    this._secure = secure;
}

Stomp.Transport.HTTP.prototype = {

    _receiverRequest: undefined,
    _disconnectReceiver: false,

    connect: function (callback, errorCallback) {
        var headers = {};
        if (this._login) {
            headers.login = this._login;
        }
        if (this._passcode) {
            headers.passcode = this._passcode;
        }
        if (this._appId) {
            headers[Stomp.Headers.APPLICATION_ID] = this._appId;
        }

        headers[Stomp.Headers.ACCEPT_VERSION] = this.client.supportedVersions();

        var transport = this;

        var request = new XMLHttpRequest();
        request.open("POST", this._url(), true);
        request.withCredentials = true;

        var timeoutHandle = setTimeout(function () {
            if (request.readyState != 0 && request.readyState != 4) {
                request.abort();
                errorCallback();
            }
        }, 5000);

        request.onerror = errorCallback;

        request.onload = function () {
            clearTimeout(timeoutHandle);
            transport.connectMessageReceiver();
            callback();
        }
        request.setRequestHeader("Content-type", "text/stomp");

        var data = Stomp.marshal("CONNECT", headers);
        request.send(data);
    },

    connectMessageReceiver: function () {
        var transport = this;
        try {
            this._eventSource = new EventSource(this._url(), { withCredentials: true });
            this._eventSource.withCredentials = true;
            this._eventSource.onerror = function () {
                transport._eventSource.close();
                transport._eventSource = undefined;
                transport.connectLongPoll();
            };
            this._eventSource.onopen = function () {
                transport._eventSource.onerror = undefined;
            };
            this._eventSource.onmessage = function (e) {
                var message = Stomp.unmarshal(e.data);
                transport.client.processMessage(message);
            };
        } catch (err) {
            Stomp.logger.debug(err);
            this._eventSource.close();
            this._eventSource = undefined;
            this.connectLongPoll();
        }
    },

    connectLongPoll: function () {
        var transport = this;

        var request = new XMLHttpRequest();
        request.open("GET", this._url(), true);
        request.onload = function () {
            var message = Stomp.unmarshal(request.response);
            transport.client.processMessage(message);
        }

        request.onloadend = function () {
            if (transport._disconnectReceiver) {
                return;
            }
            transport.connectLongPoll();
        }

        setTimeout(function () {
            if (request.readyState != 0 && request.readyState != 4) {
                request.abort();
            }
        }, 10000);

        request.setRequestHeader("Accept", "text/stomp-poll");
        request.withCredentials = true;
        request.send();
        this._receiverRequest = request;
    },

    disconnectMessageReceiver: function () {
        this._disconnectReceiver = true;
        if (this._eventSource) {
            this._eventSource.close();
            this._eventSource = undefined;
        }
        if (this._receiverRequest) {
            this._receiverRequest.abort();
            this._receiverRequest = undefined;
        }
    },

    close: function () {
        this.disconnectMessageReceiver();
    },

    transmit: function (command, headers, body, callbacks, timeoutMs) {
        var data = Stomp.marshal(command, headers, body);
        this.send(data, callbacks, timeoutMs);
    },

    send: function (data, callbacks) {
        callbacks = callbacks || {};
        var request = new XMLHttpRequest();
        request.open("POST", this._url(), true);
        request.withCredentials = true;
        Stomp.logger.debug(request);
        if (callbacks['load']) {
            request.onload = function () {
                callbacks['load'](request);
            }
        }
        if (callbacks['error']) {
            request.onerror = function () {
                callbacks['error'](request);
            }
        }
        request.setRequestHeader("Content-type", "text/stomp");
        request.send(data);
    },

    setAuth: function(login, passcode) {
      this._login = login;
      this._passcode = passcode;
    },

    setApplication: function(appId) {
        this._appId = appId;
    },

    _url: function () {
        if (this._secure) {
            return "https://" + this._host + ":" + this._port + "/";
        }
        return "http://" + this._host + ":" + this._port + "/";
    }

};

Stomp.Transports.push(Stomp.Transport.HTTP);
