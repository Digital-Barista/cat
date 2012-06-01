define([
  "backbone",
  "views/message/messageeditor",
  'text!templates/message/sendmessage.html',
  'vm'
],

function(Backbone, MessageEditor, sendMessageTemplate, Vm) {

  var SendMessage = Backbone.View.extend({
    el: '.page',
    render: function () {
      
      $(this.el).html(sendMessageTemplate);

      this.editor = Vm.create(this.options.appView, 'MessageEditor', MessageEditor, {model: this.model});
      this.editor.render();
    }
  });

  return SendMessage;

});
