function preCreate(request, libraries) {
  var properties = request.resource.properties;
  properties.parrot = "Pete";
  properties.cat = properties.dog;
  delete properties.rabbit;
}

function postCreate(request, libraries) {
  var properties = request.resource.properties;
  delete properties.dog;
  properties.pig = "Percy";
  properties.rabbit = properties.parrot;
}

function postRead(response, libraries) {
   var properties = response.resource.properties;
   properties.rabbit = properties.cat;
   properties.porcupine = "Porky";
   delete properties.urchin;
}

function preUpdate(request, libraries) {
   var properties = request.resource.properties;
   properties.cat = "Claude";
   properties.dog = properties.rabbit;
   properties.penguin = "Pat";
   delete properties.rabbit;
}

function postUpdate(response, libraries) {
  var properties = response.resource.properties;
  properties.cat = properties.dog;
  delete properties.dog;
  properties.platypus = "Patricia";
}

function postDelete(response, libraries) {
  var properties = response.resource.properties;
  properties.dog = properties.cat;
  properties.parrot = "Polly";
  delete properties.cat;
}
