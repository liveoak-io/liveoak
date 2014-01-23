var callback = arguments[arguments.length - 1];
window.liveoak = arguments.length == 1 ? LiveOak() : LiveOak(JSON.parse(arguments[0]));
callback();