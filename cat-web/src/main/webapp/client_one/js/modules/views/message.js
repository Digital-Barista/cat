define([
  "namespace",

  // Libs
  "use!backbone"

  // Modules

  // Plugins
],

function(namespace, Backbone) {

  // Create a new module
  var Message = namespace.module({Models:{}});

  
  /***********************************************
   * 
   * MODELS
   * 
   ***********************************************/
  Message.Models.Coupon = Backbone.Model.extend({
    
    INFINITE_REDEMPTION_COUNT: -1,
    INFINITE_COUPONS_COUNT: -1,
      
    START_COUPON_CODE: '{',
    END_COUPON_CODE: '}',
    
    RANDOM_CODE_LENGTH: 6,
    
    
    initialize: function(){
      var model = this;
      
      model.set({
        availableMessage: model.START_COUPON_CODE + model.END_COUPON_CODE,
        maxCoupons: model.INFINITE_COUPONS_COUNT,
        maxRedemptions: model.INFINITE_REDEMPTION_COUNT
      });
    }
  });
  
  
  /***********************************************
   * 
   * VIEWS
   * 
   ***********************************************/
  Message.Views.SendCoupon = Backbone.View.extend({
    template: "/templates/message/sendcoupon.html",

    render: function(done) {
      var view = this;
      var editor = new Message.Views.MessageEditor();

      namespace.fetchTemplate(this.template, function(tmpl) {
        view.el.innerHTML = tmpl();
        
        editor.render(function(el){
          view.$el.find('.content').html(el);
        });
        
        if (_.isFunction(done)) {
          done(view.el);
        }
      });
    }
  });

  Message.Views.SendMessage = Backbone.View.extend({
    template: "/templates/message/sendmessage.html",

    render: function(done) {
      var view = this;
      var editor = new Message.Views.MessageEditor();

      namespace.fetchTemplate(this.template, function(tmpl) {
        view.el.innerHTML = tmpl();
        
        editor.render(function(el){
          view.$el.find('.content').html(el);
        });
        
        if (_.isFunction(done)) {
          done(view.el);
        }
      });
    }
  });

  Message.Views.Sent = Backbone.View.extend({
    template: "/templates/message/sent.html",

    render: function(done) {
      var view = this;

      namespace.fetchTemplate(this.template, function(tmpl) {
        view.el.innerHTML = tmpl();
        if (_.isFunction(done)) {
          done(view.el);
        }
      });
    }
  });

  Message.Views.MessageEditor = Backbone.View.extend({
    template: "/templates/message/message-editor.html",

    render: function(done) {
      var view = this;

      namespace.fetchTemplate(this.template, function(tmpl) {
        view.el.innerHTML = tmpl();
        if (_.isFunction(done)) {
          done(view.el);
        }
      });
    }
  });
  
  // Required, return the module for AMD compliance
  return Message;

});
