define([
  "backbone",
  "views/message/messageeditor",
  'text!templates/message/sendmessage.html'
],

function(Backbone, MessageEditor, sendMessageTemplate) {

  var SendMessage = Backbone.View.extend({
    el: '.page',
    render: function () {
      
      $(this.el).html(sendMessageTemplate);
      
      var editor = new MessageEditor({model: this.model});
      editor.render();
    }
  });

  return SendMessage;

});
