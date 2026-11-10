function fn() {
  var baseUrl = karate.properties['baseUrl'] || 'http://prices:8080';
  return { baseUrl: baseUrl };
}
