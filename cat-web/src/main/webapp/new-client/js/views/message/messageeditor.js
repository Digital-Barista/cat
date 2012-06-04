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
      var view = this;
      
      this.model.bind('change', this.updateMessage, this);

      // Initialize HTML editor
      tinyMCE.init({
          theme : "advanced",
          mode: "none",
          plugins : "fullscreen,autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,advlist",

          // Theme options
          theme_advanced_buttons1 : "bold,italic,underline,|,justifyleft,justifycenter,justifyright,formatselect,fontselect,fontsizeselect",
          theme_advanced_buttons2 : "cut,copy,paste,pasteword,|,bullist,numlist,|,outdent,indent,|,undo,redo,|,link,unlink,image,help,code,|,forecolor,backcolor,|,media",
          theme_advanced_buttons3 : "",
          theme_advanced_toolbar_location : "top",
          theme_advanced_toolbar_align : "left",
          theme_advanced_statusbar_location : "bottom",
          
          handle_event_callback : $.proxy(view.handleEditorEvent, view)
      });
      tinyMCE.onAddEditor.add($.proxy(view.handleAddControl, this));
    },

    clean: function(){
      this.model.unbind('change', this.updateMessage);
      tinyMCE.onAddEditor.remove(this.handleAddControl);
      
      // Remove any existing editor
      if (this.mceEditor){
        tinyMCE.remove(this.mceEditor);
      }
    },
    
    change: function(event){
      var model = this.model
      
      model.set('message', this.mceEditor.getContent());
    },
    
    updateMessage: function(event) {
      var modelMessage = this.model.get('message');
      
      if (modelMessage != this.mceEditor.getContent()){
        this.mceEditor.setContent(modelMessage);
      }
    },
    
    render: function () {
      var view = this;
      
      this.editorId = 'editor_' + new Date().getTime();
      $(this.el).html(this.template(_.extend({editorId: this.editorId}, this.model.toJSON())));
      
      // Add a new editor
      tinyMCE.execCommand("mceAddControl", false, this.editorId);
    },
    
    handleAddControl: function(mgr, ed){
      this.mceEditor = ed;
    },
    
    handleEditorEvent: function(event) {
      if (event.type === 'keyup') {
        this.change(event);
      }
    }
  });

  return Editor;

});
