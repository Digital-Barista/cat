define([
  'underscore',
  'backbone'
], function(_, Backbone) {
  var coupon = Backbone.Model.extend({
    
    START_COUPON_CODE: '<',
    END_COUPON_CODE: '>',
    
    RANDOM_CODE_LENGTH: 6,
    
    
    initialize: function(){
      var model = this;
      
      model.set({
        availableMessage: model.START_COUPON_CODE + model.END_COUPON_CODE,
        maxCoupons: 10,
        maxRedemptions: 10
      });
    }

  });
  
  return coupon;

});
