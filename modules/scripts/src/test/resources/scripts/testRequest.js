function preRead(request, libraries) {
  preOperation(request, libraries, "preRead");
}

function postRead(response, libraries) {
  postOperation(response, libraries, "postRead");
}

function preCreate(request, libraries) {
  preOperation(request, libraries, "preCreate");
}

function postCreate(response, libraries) {
  postOperation(response, libraries, "postCreate");
}

function preUpdate(request, libraries) {
  preOperation(request, libraries,"preUpdate");
}

function postUpdate(response, libraries) {
  postOperation(response, libraries, "postUpdate");
}

function preDelete(request, libraries) {
  preOperation(request, libraries, "preDelete");
}

function postDelete(request, libraries) {
  postOperation(request, libraries, "postDelete");
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

function postOperation (response, libraries, name) {
  var request = response.request;
  preOperation(request, libraries, name);
}
