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
      $(this.el).html(this.template(this.model.toJSON()));
      
      var editor = new MessageEditor();
      editor.render();
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
        maxCoupons: $('#infinite-coupons').is(':checked') ? undefined : ($('#max-coupons').val() ? $('#max-coupons').val() : 0),
        maxRedemptions: $('#infinite-redemptions').is(':checked') ? undefined : ($('#max-redemptions').val() ? $('#max-redemptions').val() : 0)
      });
      
      this.render();
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
