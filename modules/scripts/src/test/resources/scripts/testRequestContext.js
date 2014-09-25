function preRead(request, libraries) {
  var test = request.context.parameters.test;
  if (test != null) {
    try {
      if (test == "setParameters") {
        testSetParameters(request, libraries);    
      } else if (test == "setAttributes") {
        testSetAttributes(request, libraries);
      } else if (test == "setSecurityContext") {
        testSetSecurityContext(request, libraries);
      }
    } catch (err) {
       // Note: returning a NotAcceptableError so that the error message gets propagated back to the
       // client (eg the testsuite). Otherwise the error would be a generic 'scripting error' with the
       // actual error written to the logs
       throw new liveoak.NotAcceptableError(err.message);
    }
     
  } else {
    preOperation(request, libraries, "preRead");
  }
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

function testSetParameters(request, libraries) {
  request.context.parameters = { "foo" : "bar"};
}

function testSetAttributes(request, libraries) {
  request.context.attributes = { "foo" : "bar"};
}

function testSetSecurityContext(request, libraries) {
  request.context.securityContext = null;
}
