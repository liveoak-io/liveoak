function preRead(request, libraries) {
  var properties = request.properties;
  properties.foo = "This is added by the script"
}
