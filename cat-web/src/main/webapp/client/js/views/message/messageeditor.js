define([
  "backbone",
  'text!templates/message/messageeditor.html'
],

function(Backbone, editorTemplate) {

  var Editor = Backbone.View.extend({
    el: '.editor',
    render: function () {
      $(this.el).html(editorTemplate);
    }
  });

  return Editor;

});
