function postRead(response, libraries) {
  var test = response.request.context.parameters.test;
  if (test != null) {
    try {
      if (test == "setType") {
        testSetType(response, libraries);
      } else if (test == "setResource") {
        testSetResource(response, libraries);
      } else if (test == "setRequest") {
        testSetRequest(response, libraries);
      }
    } catch (err) {
      // Note: returning a NotAcceptableError so that the error message gets propagated back to the
      // client (eg the testsuite). Otherwise the error would be a generic 'scripting error' with the
      // actual error written to the logs
      throw new liveoak.NotAcceptableError(err.message);
    }
  } else {
    postOperation(response, libraries, "postRead");
  }
}

function postCreate(response, libraries) {
  postOperation(response, libraries, "postCreate");
}

function postUpdate(response, libraries) {
  postOperation(response, libraries, "postUpdate");
}

function postDelete(response, libraries) {
  postOperation(response, libraries, "postDelete");
}

function onError(response, libraries) {
  postOperation(response, libraries, "onError");
}

function postOperation (response, libraries, name) {
  var client = libraries.client;

  var resource = new liveoak.Resource(name);
  resource.properties = {
    "type": response.type,
    "resource.id": response.resource.id,
    "resource.uri" : response.resource.uri,
    "resource.properties" : response.resource.properties
  }

  if (response.resource.members != null && response.resource.members.length > 0) {
    resource.properties["resource.member.0.id"] = response.resource.members[0].id;
  }

  client.create("/testApp/mock", resource);
}

function testSetType(response, libraries) {
  response.type = "test";
}

function testSetResource(response, libraries) {
  response.resource = new liveoak.Resource("foo");
}

function testSetRequest(response, libraries) {
  response.request = null;
}
