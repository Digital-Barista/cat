define([
  "backbone",
  'text!templates/message/messageeditor.html'
],

function(Backbone, editorTemplate) {

  var Editor = Backbone.View.extend({
    
    el: '.editor',
    
    template: _.template(editorTemplate),
    
    render: function () {
      $(this.el).html(this.template(this.model.toJSON()));
    }
  });

  return Editor;

});
