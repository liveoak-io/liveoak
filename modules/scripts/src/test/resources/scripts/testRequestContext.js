function preRead(request, libraries) {
  preOperation(request, libraries, "preRead");
}

function preCreate(request, libraries) {
  preOperation(request, libraries, "preCreate");
}

function preUpdate(request, libraries) {
  preOperation(request, libraries,"preUpdate");
}

function preDelete(request, libraries) {
  preOperation(request, libraries, "preDelete");
}


function preOperation (request, libraries, name) {
  var client = libraries.client;

  //check that the values are read only  
  request.context = null;
  request.context.attributes = null;
  request.context.parameteres = null;
  request.securityContext = null;

  //check that we can add and modify values to parameters
  request.context.parameters.baz = "bat";
  request.context.parameters.limit = "4";
 
  var context = request.context;
  var securityContext = context.securityContext;

  var resource = new liveoak.Resource(name);
  resource.properties = {
    "attributes" : context.attributes,
    "parameters" : context.parameters,
    "securityContext" : {
       "authenticated" : securityContext.authenticated,
       "realm" : securityContext.realm,
       "roles" : securityContext.roles,
       "subject" : securityContext.subject,
       "lastVerified" : securityContext.lastVerified,
       "token" : securityContext.token
    }
  }

  client.create("/testApp/mock", resource);
}
