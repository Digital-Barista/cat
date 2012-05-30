define([
  "backbone",
  'text!templates/message/messageeditor.html'
],

function(Backbone, editorTemplate) {

  var Editor = Backbone.View.extend({
    
    el: '.editor',
    
    template: _.template(editorTemplate),

    events: {
      "keyup textarea": "change",
    },
    
    initialize: function(){
      this.model.set('editorId', 'editor_' + this.model.cid);
      this.model.bind('change', this.updateMessage, this);
    },

    change: function(event){
      var model = this.model;
      
      model.set('message', tinyMCE.activeEditor.getContent());
    },
    
    updateMessage: function(event) {
      var modelMessage = this.model.get('message');
      
      if (modelMessage != tinyMCE.activeEditor.getContent()){
        tinyMCE.activeEditor.setContent(modelMessage);
      }
    },
    
    render: function () {
      var view = this;
      
      $(this.el).html(this.template(this.model.toJSON()));

      // Initialize HTML editor
      tinyMCE.init({
          theme : "advanced",
          mode: "textareas",
          plugins : "fullscreen,autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,advlist",

          // Theme options
          theme_advanced_buttons1 : "bold,italic,underline,|,justifyleft,justifycenter,justifyright,formatselect,fontselect,fontsizeselect",
          theme_advanced_buttons2 : "cut,copy,paste,pasteword,|,bullist,numlist,|,outdent,indent,|,undo,redo,|,link,unlink,image,help,code,|,forecolor,backcolor,|,media",
          theme_advanced_buttons3 : "",
          theme_advanced_toolbar_location : "top",
          theme_advanced_toolbar_align : "left",
          theme_advanced_statusbar_location : "bottom",
          
          handle_event_callback : $.proxy(view.handleEditorEvent, this)
      });
      
      tinyMCE.execCommand("mceAddControl", false, this.model.get('editorId'));
    },
    
    handleEditorEvent: function(event) {
      if (event.type === 'keyup') {
        this.change(event);
      }
    }
  });

  return Editor;

});
