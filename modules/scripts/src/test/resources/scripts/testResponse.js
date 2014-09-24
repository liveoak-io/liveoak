function postRead(response, libraries) {
  postOperation(response, libraries, "postRead");
}

function postCreate(response, libraries) {
  postOperation(response, libraries, "postCreate");
}

function postUpdate(response, libraries) {
  postOperation(response, libraries, "postUpdate");
}

function postDelete(request, libraries) {
  postOperation(request, libraries, "postDelete");
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

