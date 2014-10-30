define([
  'backbone',
  'jquery',
  'views/message/messageeditor',
  'text!templates/message/sendcoupon.html',
  'vm',
  'jqueryui'
],

function(Backbone, $, MessageEditor, sendCouponTemplate, Vm) {

  var SendCoupon = Backbone.View.extend({
    
    el: '.page',
    
    template: _.template(sendCouponTemplate),
    
    initialize: function(){
      this.messageModel = new Backbone.Model();
      this.messageModel.set('message', this.replaceCouponToken(this.model.get('availableMessage')));
      this.messageModel.bind('change', this.messageUpdate, this);
    },

    events: {
      "change input": "change",
      "click #send-coupon": "sendCoupon",
      "click #new-coupon": "newCoupon"
    },
    
    render: function () {
      var $el = this.$el,
          model = this.model,
          infiniteCoupons = model.get('infiniteCoupons'),
          infiniteRedemptions = model.get('infiniteRedemptions'),
          useRandomCode = model.get('useRandomCode');
      
      // Populate template
      $el.html(this.template(this.model.toJSON()));

      
      // Add date picker
      $el.find('.date-picker').datepicker();
      
      // Set input values that are bitch to do in a template
      $el.find('#infinite-coupons').prop('checked', infiniteCoupons);
      $el.find('#infinite-redemptions').prop('checked', infiniteRedemptions);
      $el.find('#random-code').prop('checked', useRandomCode);
      $el.find('#static-code').prop('checked', !useRandomCode);
      
      this.updateControlVisibility();
      
      // Render the message editor with correct message
      this.editor = Vm.create(this.options.appView, 'MessageEditor', MessageEditor, {model: this.messageModel});
      this.editor.render();
    },
    
    messageUpdate: function(event){
      var message = this.messageModel.get('message');
      var updateMessage = this.replaceCouponToken(message);

      // Set the coupon model message
      if ($('#available').prop('checked') ) {
        this.model.set('availableMessage', updateMessage);
      }
      else {
        this.model.set('unavailableMessage', updateMessage);
      }
      
      // Update the editor message only if it has changed (it will fire this again)
      if (message != updateMessage){
        this.messageModel.set('message', updateMessage);
      }
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
      
      $el.find('input[name="expiration"]').prop('checked', false);
      $el.find('input[id="expire-' + model.get('expire') + '"]').prop('checked', true);

      $el.find('.expire-control').hide();
      $el.find('#expire-' + model.get('expire') + '-control').show();
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
        unavailableDate: $('#unavailable-date').val(),
        expire: $('input[name="expiration"]:checked').val(),
        expireDays: $('#expire-days').val(),
        expireDate: $('#expire-date').val()
      });
      
      // Show correct message
      if ($('#available').prop('checked') ) {
        this.messageModel.set('message', this.replaceCouponToken(this.model.get('availableMessage')));
      }
      else {
        this.messageModel.set('message', this.model.get('unavailableMessage'));
      }

      this.updateControlVisibility();
    },
    
    replaceCouponToken: function(text){
      var model = this.model;
      var code = model.START_COUPON_CODE;
      
      if (model.get('useRandomCode')){
        code += model.RANDOM_CODE_LENGTH + '_CHAR_CODE';
      }
      else {
        code += model.get('staticCode');
      } 
      
      code += model.END_COUPON_CODE;
      
      return text.replace(/{.*?}/g, code);
    },
    
    sendCoupon: function(event){
      event.preventDefault();
      
      console.log(this.model.toJSON());
    },
    
    newCoupon: function(event){
      event.preventDefault();
      
      this.model.set(this.model.defaults);
      this.model.initialize();
      this.messageModel.set('message', this.replaceCouponToken(this.model.get('availableMessage')));
      this.render();
    }
    
  });

  return SendCoupon;

});
