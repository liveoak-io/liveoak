function preRead(request) {
  preOperation(request, "executed");
}

function preOperation (request, name) {
  var client = liveoak.Client;

  var resource = new liveoak.Resource(name);
  resource.properties = {
    "id" : request.id,
    "path" : request.path,
    "type" : request.type
  }

  client.create("/testApp/mock", resource);
}

