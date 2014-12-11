function preRead(request, libraries) {
  preOperation(request, libraries, "executed");
}

function preOperation (request, libraries, name) {
  var client = libraries.client;

  var resource = new liveoak.Resource(name);
  resource.properties = {
    "id" : request.id,
    "path" : request.path,
    "type" : request.type
  }

  client.create("/testApp/mock", resource);
}

