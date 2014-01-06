window.oauth = (function () {
    var oauth = {};

    var params = window.location.search.substring(1).split('&');
    for (var i = 0; i < params.length; i++) {
        var p = params[i].split('=');
        switch (decodeURIComponent(p[0])) {
            case 'code':
                oauth.code = p[1];
                break;
            case 'error':
                oauth.error = p[1];
                break;
            case 'state':
                oauth.state = decodeURIComponent(p[1]);
                break;
        }
    }

    if (oauth.state && oauth.state == sessionStorage.oauthState) {
        oauth.callback = true;
        delete sessionStorage.oauthState;
    } else {
        oauth.callback = false;
    }

    if (oauth.callback) {
        if (oauth.state.indexOf('#') != -1) {
            var stateParams = atob(oauth.state.split('#')[1]).split('&');
            for (var i = 0; i < stateParams.length; i++) {
                var p = stateParams[i].split('=');
                switch (p[0]) {
                    case 'hash':
                        oauth.fragment = p[1];
                        break;
                    case 'prompt' :
                        oauth.prompt = p[1];
                        break;
                }
            }
        }

        window.history.replaceState({}, null, location.protocol + '//' + location.host + location.pathname + (oauth.fragment ? oauth.fragment : ''));
    }

    return oauth;
}());

var Keycloak = function (options) {
    options = options || {};
    var instance = this;

    var baseUrl = (options.secure ? 'https://' : 'http://') + options.host;
    if (options.port) {
        baseUrl += ':' + options.port;
    }

    if (!options.realm) {
        options.realm = 'default';
    }

    this.init = function () {
        if (window.oauth.callback) {
            delete sessionStorage.oauthToken;
            processCallback();
        } else if (options.token) {
            setToken(options.token);
        } else if (sessionStorage.oauthToken) {
            setToken(sessionStorage.oauthToken);
        } else if (options.onload) {
            switch (options.onload) {
                case 'login-required' :
                    instance.login();
                    break;
                case 'check-sso' :
                    instance.login(false);
                    break;
            }
        }
    }

    this.login = function (prompt) {
        window.location.href = createLoginUrl(prompt);
    }

    this.logout = function () {
        setToken(undefined);
        window.location.href = createLogoutUrl();
    }

    this.hasRealmRole = function (role) {
        var access = this.realmAccess;
        return access && access.roles.indexOf(role) >= 0 || false;
    }

    this.hasResourceRole = function (role, resource) {
        var access = this.resourceAccess[resource || options.clientId];
        return access && access.roles.indexOf(role) >= 0 || false;
    }

    this.getUserProfile = function (options) {
        var url = getRealmUrl() + '/account';
        var req = new XMLHttpRequest();
        req.open('GET', url, true);
        req.setRequestHeader('Accept', 'application/json');
        req.setRequestHeader('Authorization', 'bearer ' + this.token);

        req.onreadystatechange = function () {
            if (req.readyState == 4) {
                if (req.status == 200) {
                    var profile = JSON.parse(req.responseText);
                    if (options.success) {
                        options.success(profile);
                    }
                } else {
                    if (options.error) {
                        var response = { status: req.status, statusText: req.status };
                        if (req.responseText) {
                            response.data = JSON.parse(req.responseText);
                        }
                        options.error(response);
                    }
                }
            }
        }

        req.send();
    }

    function getRealmUrl() {
        return baseUrl + '/auth-server/rest/realms/' + encodeURIComponent(options.realm);
    }

    function processCallback() {
        var code = window.oauth.code;
        var error = window.oauth.error;
        var prompt = window.oauth.prompt;

        if (code) {
            var params = 'code=' + code + '&client_id=' + encodeURIComponent(options.clientId) + '&password=' + encodeURIComponent(options.clientSecret);
            var url = getRealmUrl() + '/tokens/access/codes';

            var req = new XMLHttpRequest();
            req.open('POST', url, true);
            req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

            req.onreadystatechange = function () {
                if (req.readyState == 4) {
                    if (req.status == 200) {
                        setToken(JSON.parse(req.responseText)['access_token']);
                    } else {
                        if (options.error) {
                            options.error({  authenticated: false, status: req.status, statusText: req.statusText });
                        }
                    }
                }
            };

            req.send(params);
        } else if (error) {
            if (prompt != 'none') {
                if (options.error) {
                    options.error({  authenticated: false, error: window.oauth.error });
                }
            }
        }
    }

    function setToken(token) {
        if (token) {
            sessionStorage.oauthToken = token;
            window.oauth.token = token;
            instance.token = token;

            instance.tokenParsed = JSON.parse(atob(token.split('.')[1]));
            instance.authenticated = true;
            instance.username = instance.tokenParsed.prn;
            instance.realmAccess = instance.tokenParsed.realm_access;
            instance.resourceAccess = instance.tokenParsed.resource_access;

            if (options.success) {
                options.success({ authenticated: instance.authenticated, username: instance.username });
            }
        } else {
            delete sessionStorage.oauthToken;
            delete window.oauth.token;
            delete instance.token;
        }
    }

    function createLoginUrl(prompt) {
        var state = createUUID() + '#' + btoa('hash=' + location.hash + '&prompt=' + (prompt == false ? 'none' : ''));

        sessionStorage.oauthState = state;
        var url = getRealmUrl()
            + '/tokens/login'
            + '?client_id=' + encodeURIComponent(options.clientId)
            + '&redirect_uri=' + getEncodedRedirectUri()
            + '&state=' + encodeURIComponent(state)
            + '&response_type=code';

        if (prompt == false) {
            url += '&prompt=none';
        }

        return url;
    }

    function createLogoutUrl() {
        var url = getRealmUrl()
            + '/tokens/logout'
            + '?redirect_uri=' + getEncodedRedirectUri();
        return url;
    }

    function getEncodedRedirectUri() {
        var url = options.redirectUri || (location.protocol + '//' + location.hostname + (location.port && (':' + location.port)) + location.pathname);
        return encodeURI(url);
    }

    function createUUID() {
        var s = [];
        var hexDigits = '0123456789abcdef';
        for (var i = 0; i < 36; i++) {
            s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
        }
        s[14] = '4';
        s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
        s[8] = s[13] = s[18] = s[23] = '-';
        var uuid = s.join('');
        return uuid;
    }
}
