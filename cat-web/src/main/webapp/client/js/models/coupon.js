define([
  'underscore',
  'backbone'
], function(_, Backbone) {
  var coupon = Backbone.Model.extend({
    
    START_COUPON_CODE: '{',
    END_COUPON_CODE: '}',
    
    RANDOM_CODE_LENGTH: 6,
    
    defaults: {
      name: '',
      infiniteCoupons: true,
      infiniteRedemptions: true,
      maxCoupons: 0,
      maxRedemptions: 0,
      useRandomCode: true,
      staticCode: '',
      couponCode: '',
      offerCode: '',
      unavailableDate: '',
      expireDate: '',
      expireDays: '',
      expire: 'never',
      unavailableMessage: ''
    },
    
    initialize: function(){
      var model = this;
      
      model.set({
        availableMessage: model.START_COUPON_CODE + model.END_COUPON_CODE
      });
    }

  });
  
  return coupon;

});
