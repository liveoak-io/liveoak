function preRead(request, libraries) {
  var test = request.context.parameters.test;

  try {
    if (test == "testTimeout") {
      testTimeout(request);
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

function testTimeout(request) {
  var intervalTime = 500;
  var start = Date.now();
  while (true) {
    var current = Date.now();
    if (current - start >= intervalTime) {
      start = current
      //print("HELLO WORLD");
    }
  }
}
