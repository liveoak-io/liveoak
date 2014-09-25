function postRead(response, libraries) {
  var test = response.request.context.parameters.test;

  var resource = response.resource;
  
  try {
    if (test == "setId") {
      testSetId(resource);
    } else if (test == "setPath") {
      testSetPath(resource);
    } else if (test == "setMembers") {
      testSetMembers(resource);
    } else if (test == "addMember") {
      testAddMember(resource);
    } else if (test == "setProperties") {
      testSetProperties(resource);
    } else {
     throw new Error("Unknown Test : " + test);
    } 
  } catch (err) {
    print("ERROR: " + err);
    // Note: returning a NotAcceptableError so that the error message gets propagated back to the
    // client (eg the testsuite). Otherwise the error would be a generic 'scripting error' with the
    // actual error written to the logs
    throw new liveoak.NotAcceptableError(err.message);
  }
}

function testSetId(resource) {
  resource.id = "bar";  
}

function testSetPath(resource) {
  resource.uri = "foobar";
}

function testSetMembers(resource) {
  resource.members = null;
}

function testAddMember(resource) {
  resource.members.push= new liveoak.Resource("baz");
}

function testSetProperties(resource) {
  resource.properties = { "testing": "123"};
}
