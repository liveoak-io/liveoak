
var mBoss = function(host, port, secure, callback) {
  this._host = host;
  this._port = port;
  this._secure = secure;
  this.stomp_client = new Stomp.Client( host, port, secure );

}

mBoss.prototype = {

  connect: function(callback) {
    this.stomp_client.connect( callback );
  },

  create: function(path, data) {
    console.debug( "INSIDE CREATE" );
    $.ajax( path, {
      type: "POST",
      data: JSON.stringify( data ),
      contentType: 'application/json',
      dataType: 'json',
    });
  },

  read: function(path, callback) {
    $.ajax( path, {
      type: "GET", 
      dataType: 'json',
      success: callback,
    } );
  },

  update: function(path, data) {
  },

  delete: function(path) {
  },

  subscribe: function(path, callback) {
    this.stomp_client.subscribe( path, function(msg) {
      var data = JSON.parse( msg.body );
      callback( data );
    } );
  }

}
