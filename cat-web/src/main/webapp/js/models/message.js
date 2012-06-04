define([
  'underscore',
  'backbone'
], function(_, Backbone) {
  var message = Backbone.Model.extend({
    
    
    defaults: {
      name: '',
      message: ''
    }

  });
  
  return message;

});
