define([
  "backbone",
  'hbs!templates/message/sent'
],

function(Backbone, sentTemplate) {

  var Sent = Backbone.View.extend({
    el: '.page',
    
    render: function () {
      var broadcasts = {broadcasts: this.collection.toJSON()};
      $(this.el).html(sentTemplate(broadcasts));
    }
  });

  return Sent;

});
