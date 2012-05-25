define([
  "backbone",
  "views/message/messageeditor",
  'text!templates/message/sendcoupon.html'
],

function(Backbone, MessageEditor, sendCouponTemplate) {

  var SendCoupon = Backbone.View.extend({
    
    el: '.page',
    
    template: _.template(sendCouponTemplate),
    
    render: function () {
      var $el = this.$el,
          editor,
          model = this.model,
          message,
          infiniteCoupons = model.get('infiniteCoupons'),
          infiniteRedemptions = model.get('infiniteRedemptions'),
          useRandomCode = model.get('useRandomCode');
      
      // Populate template
      $el.html(this.template(this.model.toJSON()));
      
      // Set input values that are bitch to do in a template
      $el.find('#infinite-coupons').prop('checked', infiniteCoupons);
      $el.find('#infinite-redemptions').prop('checked', infiniteRedemptions);
      $el.find('#random-code').prop('checked', useRandomCode);
      $el.find('#static-code').prop('checked', !useRandomCode);
      
      this.updateControlVisibility();
      
      // Render the message editor with correct message
      message = $el.find('#available').prop('checked') ? 
          model.get('availableMessage') : model.get('unavailableMessage');
          
      editor = new MessageEditor({model: new Backbone.Model({message: message})});
      editor.render();
    },
    
    /**
     * Update controls visibility according to model data
     */
    updateControlVisibility: function(){
      var $el = this.$el,
          model = this.model,
          infiniteCoupons = model.get('infiniteCoupons'),
          infiniteRedemptions = model.get('infiniteRedemptions'),
          useRandomCode = model.get('useRandomCode');
    
      $el.find('#static-code-value').toggle(!useRandomCode);
      $el.find('#max-redemptions').toggle(!infiniteRedemptions);
      $el.find('#max-coupons').toggle(!infiniteCoupons);
    },

    events: {
      "change input": "change",
      "click #send-coupon": "sendCoupon",
      "click #new-coupon": "newCoupon"
    },
    
    change: function(event){
      var model = this.model;
      
      model.set({
        name: $('#title').val(),
        infiniteCoupons: $('#infinite-coupons').prop('checked'),
        maxCoupons: $('#max-coupons').val(),
        infiniteRedemptions: $('#infinite-redemptions').prop('checked'),
        maxRedemptions: $('#max-redemptions').val(),
        useRandomCode: $('#random-code').prop('checked'),
        staticCode: $('#static-code-value').val(),
        offerCode: $('#offer-code').val(),
        unavailableDate: $('#unavailable-date').val()
      });

      this.updateControlVisibility();
    },
    
    sendCoupon: function(event){
      event.preventDefault();
      event.stopPropagation();
      
      console.log(this.model.toJSON());
    },
    
    newCoupon: function(event){
      event.preventDefault();
      event.stopPropagation();
      
      this.model.set(this.model.defaults);
      this.render();
    }
    
  });

  return SendCoupon;

});
