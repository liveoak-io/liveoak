function preRead(request, libraries) {
  var test = request.context.parameters.test;
  
  try {
    if (test == "setId") {
      testSetId(request);
    } else if (test == "setPath") {
      testSetPath(request);
    } else if (test == "setType") {
      testSetType(request);
    } else if (test == "setResource") {
      testSetResource(request);
    } else {
     throw new Error("Unknown Test : " + test);
    } 
  } catch (err) {
    // Note: returning a NotAcceptableError so that the error message gets propagated back to the
    // client (eg the testsuite). Otherwise the error would be a generic 'scripting error' with the
    // actual error written to the logs
    throw new liveoak.NotAcceptableError(err.message);
  }
}

function testSetId(request) {
  request.id = "fooBar";
}

function testSetPath(request) {
  request.path = null;
}

function testSetType(request) {
  request.type = "foo";
}

function testSetResource(request) {
  request.resource = new liveoak.Resource("testResource");
}
