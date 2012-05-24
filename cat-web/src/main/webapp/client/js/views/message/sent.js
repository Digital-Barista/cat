define([
  "backbone",
  'text!templates/message/sent.html'
],

function(Backbone, sentTemplate) {

  var Sent = Backbone.View.extend({
    el: '.page',
    render: function () {
      $(this.el).html(sentTemplate);
    }
  });

  return Sent;

});
