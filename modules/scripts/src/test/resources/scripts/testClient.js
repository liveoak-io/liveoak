function preRead(request, libraries) {
  var test = request.context.parameters.test;
  
  var client = libraries.client;
  try {
    if (test == "testDefaultRead") {
      runTest("/testApp/mock/test", {}, "testDefaultRead", client);
    } else if (test == "testFields") {
      runTest("/testApp/mock/test", {'fields': 'type,members(type)'}, "testFields", client);
    } else if (test == "testSort") {
      runTest("/testApp/mock/test", { 'sort' : 'value'}, "testSort", client);
    } else if (test == "testOffset") {
      runTest("/testApp/mock/test", { 'offset' : 1}, "testOffset", client);
    } else if (test == "testLimit") {
      runTest("/testApp/mock/test", { 'limit': 1}, "testLimit", client);
    } else {
     throw new Error("Unknown Test : " + test);
    } 
  } catch (err) {
    // Note: returning a NotAcceptableError so that the error message gets propagated back to the
    // client (eg the testsuite). Otherwise the error would be a generic 'scripting error' with the
    // actual error written to the logs
    print("ERROR: " + err);
    throw new liveoak.NotAcceptableError(err.message);
  }
}

function runTest(uri, properties, name, client) {
  var resource = client.read(uri, properties);

  var testData = new liveoak.Resource(name);
  testData.properties = {
    "id" : resource.id,
    "uri": resource.uri,
    "properties" : resource.properties
  };

  for (var i = 0; i < resource.members.length; i++) {
    var member = resource.members[i];
    testData.properties["member_" + i] = {
      "id" : member.id,
      "uri" : member.uri,
      "properties" : member.properties
    }
  }

  client.create("/testApp/mock/data", testData);
}
